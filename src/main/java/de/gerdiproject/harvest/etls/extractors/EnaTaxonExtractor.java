/*
 *  Copyright © 2019 Robin Weiss (http://www.gerdi-project.de/)
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.application.MainContextUtils;
import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaTaxonConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaReferenceVO;
import de.gerdiproject.harvest.etls.extractors.vos.EnaTaxonVO;
import de.gerdiproject.harvest.utils.DiskCollection;
import de.gerdiproject.harvest.utils.data.HttpRequester;


/**
 * This extractor extracts all taxon metadata from ENA. Taxa are stored
 * in a tree structure, which is traversed non-deterministically by this
 * extractor.
 *
 * @author Robin Weiss
 */
public class EnaTaxonExtractor extends AbstractIteratorExtractor<EnaTaxonVO>
{
    protected final DiskCollection taxonIDs = new DiskCollection(
        new File(
            MainContextUtils.getCacheDirectory(EnaTaxonExtractor.class),
            EnaTaxonConstants.QUEUE_FOLDER));
    protected final HttpRequester httpRequester = new HttpRequester();


    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.httpRequester.setCharset(etl.getCharset());

        try {
            this.taxonIDs.clear();
        } catch (IOException e) {
            throw new ExtractorException(e);
        }
    }


    @Override
    public int size()
    {
        return -1;
    }


    @Override
    public String getUniqueVersionString()
    {
        // it's not feasible to calculate the hash, because there is no overall version
        return null;
    }


    @Override
    protected Iterator<EnaTaxonVO> extractAll() throws ExtractorException
    {
        return new EnaTaxonIterator();
    }


    @Override
    public void clear()
    {
        try {
            this.taxonIDs.clear();
        } catch (IOException ignored) {
            // do nothing
        }
    }


    /**
     * This iterator iterates through all taxa, starting at the root element whith
     * taxID 1.
     *
     * @author Robin Weiss
     */
    private class EnaTaxonIterator implements Iterator<EnaTaxonVO>
    {
        private String currentTaxonId = EnaTaxonConstants.TAXON_ROOT_ID;


        @Override
        public boolean hasNext()
        {
            return currentTaxonId != null;
        }


        @Override
        public EnaTaxonVO next()
        {
            final String xmlUrl = String.format(EnaTaxonConstants.XML_URL, currentTaxonId);
            final Document taxonXml =  httpRequester.getHtmlFromUrl(xmlUrl);

            try {
                // enqueue all child taxa to be extracted later
                final Element children = taxonXml.selectFirst(EnaTaxonConstants.CHILDREN_ELEMENT);

                if (children != null) {
                    for (final Element taxon : children.children())
                        taxonIDs.add(taxon.attr(EnaTaxonConstants.TAXON_ID_ATTRIBUTE));
                }

                // retrieve next taxon from the queue
                this.currentTaxonId = taxonIDs.get();
            } catch (IOException e) {
                throw new ExtractorException(e);
            }

            // get references
            final String refUrl = String.format(EnaTaxonConstants.REFERENCE_URL, currentTaxonId);
            final List<EnaReferenceVO> references = httpRequester.getObjectFromUrl(refUrl, EnaConstants.REFERENCE_LIST_TYPE);

            return new EnaTaxonVO(taxonXml, references);
        }
    }
}
