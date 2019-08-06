/*
 *  Copyright © 2018 Robin Weiss (http://www.gerdi-project.de/)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package de.gerdiproject.harvest.etls.extractors;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.ws.http.HTTPException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.EnaTaxonETL;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.harvest.utils.data.enums.RestRequestType;


/**
 * This extractor retrieves documents regarding a taxon in ENA.
 *
 * @author Robin Weiss, Jan Frömberg
 */
public class EnaTaxonExtractor extends AbstractIteratorExtractor<Element>
{
    /**
     * TODO iterate ftp://ftp.ebi.ac.uk/pub/databases/ena/taxonomy/sdwca/ENA_120913vsCoL_150813_xmapping_result.csv
     * to retrieve all TaxonIDs and harvest all taxa
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EnaTaxonExtractor.class);
    private final HttpRequester httpRequester = new HttpRequester();
    private EnaTaxonETL dedicatedEtl;
    private String taxonId;
    private int size = -1;


    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.httpRequester.setCharset(etl.getCharset());
        this.dedicatedEtl = (EnaTaxonETL)etl;

        this.taxonId = dedicatedEtl.getTaxonId();
        this.size = calculateSize(taxonId);
    }


    @Override
    public int size()
    {
        return size;
    }


    @Override
    public String getUniqueVersionString()
    {
        // it's not feasible to calculate the hash, because there is no overall version
        return null;
    }


    @Override
    protected Iterator<Element> extractAll() throws ExtractorException
    {
        return new EnaTaxonIterator(50);
    }


    /**
     * This method sends multiple GET requests to the ENA API, trying
     * to find the index offset of the last taxon that can be retrieved.
     *
     * @param taxonId the taxon identifier
     *
     * @return the number of harvestable taxon documents
     */
    private int calculateSize(final String taxonId)
    {
        /*
         * TODO Use https://www.ebi.ac.uk/ena/data/view/Taxon:10088&portal=sequence_update
         * in order to retrieve the size. This is not trivial, because ENA uses JS to populate
         * its website with data.
         *
         * For help, use:
         * https://stackoverflow.com/questions/7488872/page-content-is-loaded-with-javascript-and-jsoup-doesnt-see-it
         */
        if (taxonId == null)
            return 0;

        int minOffest = 1;
        int offset = 1;
        int maxOffset = Integer.MAX_VALUE;

        try {
            while (maxOffset - minOffest > 1) {
                final String url = String.format(EnaUrlConstants.TAXON_SIZE_URL, taxonId, offset);

                // check if the URL is valid and within the taxon range
                if (!httpRequester.getRestResponse(RestRequestType.GET, url, null).equals(EnaConstants.INVALID_ENTRY_RESPONSE)) {
                    minOffest = offset;
                    offset = Math.min(2 * offset, maxOffset / 2 + minOffest / 2);
                } else {
                    maxOffset = offset;
                    offset = (minOffest + maxOffset) / 2;
                }

            }
        } catch (HTTPException | IOException e) {
            LOGGER.error(String.format(EnaConstants.TAXON_SIZE_ERROR, taxonId), e);
            minOffest = 1;
            maxOffset = 1;
        }

        return maxOffset == minOffest ? 0 : minOffest;
    }


    /**
     * This iterator iterates through the range of taxa
     * while downloading the entries in batches.
     *
     * @author Robin Weiss
     */
    private class EnaTaxonIterator implements Iterator<Element>
    {
        private final int batchSize;

        private Iterator<Element> currentBatch;
        private int offset;


        /**
         * Constructor.
         * @param batchSize the maximum number of entries that may be extracted at any given time
         */
        public EnaTaxonIterator(final int batchSize)
        {
            this.offset = 1;
            this.batchSize = batchSize;
            retrieveNextBatch();
        }


        @Override
        public boolean hasNext()
        {
            return currentBatch.hasNext() || offset <= size;
        }


        @Override
        public Element next()
        {
            final Element nextElement =  currentBatch.next();

            // batches may be completely empty, thus this needs to be a while-loop
            while (!currentBatch.hasNext() && offset <= size)
                retrieveNextBatch();

            return nextElement;
        }


        /**
         * Retrieves the next batch of entries out of the specified
         * range of accession numbers.
         */
        private void retrieveNextBatch()
        {
            final int limit = offset + batchSize - 1;
            final String url = String.format(
                                   EnaUrlConstants.TAXON_URL,
                                   taxonId,
                                   offset,
                                   limit);
            final Document doc = httpRequester.getHtmlFromUrl(url);

            if (doc == null)
                throw new ExtractorException(String.format(EnaConstants.URL_ERROR, url));

            // retrieve all entries with fitting accession numbers
            final Elements entries = doc.select("entry");

            // set batch and next number
            this.currentBatch = entries.iterator();
            this.offset += batchSize;
        }
    }


    @Override
    public void clear()
    {
        // nothing to clean up
    }
}
