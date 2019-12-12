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

import de.gerdiproject.harvest.utils.HtmlUtils;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.harvest.utils.data.enums.RestRequestType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaFastqVO;

/**
 * This {@linkplain AbstractIteratorExtractor} implementation extracts all FASTQ
 * (meta-)data from ENA and bundles it into a {@linkplain EnaFastqVO}.
 *
 * @author Komal Ahir
 */
public class EnaFastqExtractor extends AbstractIteratorExtractor<EnaFastqVO>
{
    protected final HttpRequester httpRequester = new HttpRequester();
    protected final char enaEDS;

    /**
     * Constructor.
     * @param enaEDS character to extract fastq data for different ETLs with char 'E', 'D', 'S'
     */
    public EnaFastqExtractor(final char enaEDS)
    {
        super();
        this.enaEDS = enaEDS;
    }

    @Override
    public void init(final AbstractETL<?, ?> etl)
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
     * {@linkplain EnaFastqVO}s used for harvesting Ena Fastq datasets by
     * trying out all IDs in a range of 000000 to 999999.
     *
     * @author Komal Ahir
     */
    private class EnaFastqIterator implements Iterator<EnaFastqVO>
    {
        private int id = 0; // NOPMD field is intentionally initialized with 0

        @Override
        public boolean hasNext()
        {
            return id < size();
        }

        @Override
        public EnaFastqVO next()
        {
            id++;
            final String viewUrl = String.format(EnaUrlConstants.VIEW_URL_FASTQ, enaEDS, id);
            final Document viewPage;

            try {
                // suppress expected warning messages by retrieving the string response first
                final String viewResponse = httpRequester.getRestResponse(RestRequestType.GET, viewUrl, null);

                // parse HTML from String
                viewPage = Jsoup.parse(viewResponse);

                // check if the document is valid
                if (HtmlUtils.getString(viewPage, EnaConstants.ID) == null)
                    return null;
            } catch (IOException e) {
                // skip this page
                return null;
            }

            // attempt to retrieve the file report
            final String fileReportUrl = String.format(EnaUrlConstants.DOWNLOAD_URL_FASTQ, enaEDS, id);

            try {
                final String fileReport = httpRequester.getRestResponse(RestRequestType.GET, fileReportUrl, null);
                return new EnaFastqVO(viewPage, fileReport);

            } catch (IOException e) {
                return new EnaFastqVO(viewPage, null);
            }
        }
    }

    @Override
    public void clear()
    {
        // nothing to clean up
    }
}