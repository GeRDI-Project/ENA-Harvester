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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.EnaETL;
import de.gerdiproject.harvest.utils.data.HttpRequester;


/**
 * This extractor retrieves a specified range of entries from ENA,
 * in specified batches, in order to not run out of memory.
 *
 * @author Robin Weiss
 */
public class EnaExtractor extends AbstractIteratorExtractor<Element>
{
    private EnaETL dedicatedEtl;
    private String accFrom;


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.dedicatedEtl = (EnaETL)etl;

        this.accFrom = dedicatedEtl.getStartingAccessionNumber();
    }


    @Override
    public int size()
    {
        // remove the letter prefixes and calculate the max value
        try {
            return Integer.parseInt(accFrom.replaceAll(EnaConstants.LETTER_PREFIX_REGEX, "").replaceAll(EnaConstants.NUMBER_REGEX, "9"))
                   + 1;
        } catch (NumberFormatException e) {
            // if we cannot parse the accession numbers, we don't know the size
            return -1;
        }
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
        return new EnaIterator(accFrom, 50);
    }


    /**
     * This iterator iterates through the range of accession numbers
     * while downloading the entries in batches.
     *
     * @author Robin Weiss
     */
    private class EnaIterator implements Iterator<Element>
    {
        private final HttpRequester httpRequester;
        private final int batchSize;
        private final int maxNumber;
        private final String accessionNumberPattern;

        private Iterator<Element> currentBatch;
        private int currentNumber;


        /**
         * Constructor.
         * @param firstAccessionNumber the accession number of the first entry to be extracted
         * @param batchSize the maximum number of entries that may be extracted at any given time
         */
        public EnaIterator(String firstAccessionNumber, int batchSize)
        {
            final String accessionPrefix = firstAccessionNumber.replaceAll(EnaConstants.NUMBER_REGEX, "");
            this.currentNumber = Integer.parseInt(firstAccessionNumber.substring(accessionPrefix.length()));
            this.maxNumber = Integer.parseInt(firstAccessionNumber.substring(accessionPrefix.length()).replaceAll(EnaConstants.NUMBER_REGEX, "9"));
            this.accessionNumberPattern = String.format(
                                              EnaConstants.ACCESSION_FORMAT_BASE,
                                              accessionPrefix,
                                              firstAccessionNumber.length() - accessionPrefix.length());

            this.httpRequester = new HttpRequester(new Gson(), StandardCharsets.UTF_8);
            this.batchSize = batchSize;
            retrieveNextBatch();
        }


        @Override
        public boolean hasNext()
        {
            return currentBatch.hasNext() || currentNumber < maxNumber;
        }


        @Override
        public Element next()
        {
            final Element nextElement =  currentBatch.next();

            // batches may be completely empty, thus this needs to be a while-loop
            while (!currentBatch.hasNext() && currentNumber < maxNumber)
                retrieveNextBatch();

            return nextElement;
        }


        /**
         * Retrieves the next batch of entries out of the specified
         * range of accession numbers.
         */
        private void retrieveNextBatch()
        {
            final int nextNumber = Math.min(maxNumber, currentNumber + batchSize - 1);
            final String domainsUrl = String.format(
                                          EnaUrlConstants.RANGE_ACCESSION_URL,
                                          String.format(accessionNumberPattern, currentNumber),
                                          String.format(accessionNumberPattern, nextNumber));
            final Document doc = httpRequester.getHtmlFromUrl(domainsUrl);

            if (doc == null)
                throw new ExtractorException(String.format(EnaConstants.URL_ERROR, domainsUrl));

            // retrieve all entries with fitting accession numbers
            final Elements entries = doc.select("entry");

            // some accession numbers don't exist, so the entries may be less
            final int missingEntryCount = (1 + nextNumber - currentNumber) - entries.size();
            dedicatedEtl.increaseHarvestedDocuments(missingEntryCount);

            // set batch and next number
            this.currentBatch = entries.iterator();
            this.currentNumber += batchSize;
        }
    }
}
