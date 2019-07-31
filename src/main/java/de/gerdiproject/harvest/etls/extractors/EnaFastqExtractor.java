/**
 * Copyright © 2019 Komal Ahir (http://www.gerdi-project.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gerdiproject.harvest.etls.extractors;

import java.io.IOException;
import java.util.Iterator;

import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.harvest.utils.data.enums.RestRequestType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;



/**
 * This {@linkplain AbstractIteratorExtractor} implementation extracts all
 * (meta-)data from ClinicalTrials and bundles it into a {@linkplain ClinicalTrialsVO}.
 *
 * @author Komal Ahir
 */
public class EnaFastqExtractor extends AbstractIteratorExtractor<EnaFastqVO>
{
    private final HttpRequester httpRequester;

    /**
     * Simple constructor.
     */
    public EnaFastqExtractor()
    {
        this.httpRequester = new HttpRequester();
    }

    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);

        this.httpRequester.setCharset(etl.getCharset());
    }

    @Override
    public String getUniqueVersionString()
    {
        // it's not feasible to calculate the hash, because there is no overall version hence null
        return null;
    }

    @Override
    public int size()
    {
        return EnaConstants.FASTQ_DOCUMENT_COUNT;
    }

    @Override
    protected Iterator<EnaFastqVO> extractAll() throws ExtractorException
    {
        return new EnaFastqIterator();
    }

    /**
     * This class represents an {@linkplain Iterator} that iterates through
     * {@linkplain ClinicalTrialsVO}s used for harvesting Ena Fastq datasets by
     * trying out all IDs in a range of 000 to 999.
     *
     * @author Komal Ahir
     */
    private class EnaFastqIterator implements Iterator<EnaFastqVO>
    {
        int id = 0;

        @Override
        public boolean hasNext()
        {
            return id < size();
        }

        @Override
        public EnaFastqVO next()
        {
            final String url = String.format(EnaUrlConstants.VIEW_URL_FASTQ, id);
            id++;

            try {
                // suppress expected warning messages by retrieving the string response first
                final String response = httpRequester.getRestResponse(RestRequestType.GET, url, null);
                // parse HTML from String
                final Document viewPage = Jsoup.parse(response);
                return new EnaFastqVO(id, viewPage);
            } catch (IOException e) {  // skip this page
                return null;
            }
        }
    }
}