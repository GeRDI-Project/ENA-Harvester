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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;

import de.gerdiproject.harvest.ena.constants.EnaFastqConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaFastqVO;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.GsonUtils;

/**
 * This {@linkplain AbstractIteratorExtractor} implementation extracts all FASTQ
 * (meta-)data from ENA and bundles it into a {@linkplain EnaFastqVO}.
 *
 * @author Komal Ahir
 */
public class EnaFastqExtractor extends AbstractIteratorExtractor<EnaFastqVO>
{
    protected final Gson gson = GsonUtils.createGerdiDocumentGsonBuilder().create();
    protected final HttpRequester httpRequester = new HttpRequester(gson, StandardCharsets.UTF_8);
    protected final String accessionPrefix;


    /**
     * Constructor.
     * @param accessionPrefix accession prefix for getting FASTQ data
     */
    public EnaFastqExtractor(final String accessionPrefix)
    {
        super();
        this.accessionPrefix = accessionPrefix.toUpperCase(Locale.ENGLISH);
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
        return EnaFastqConstants.FASTQ_DOCUMENT_COUNT;
    }


    @Override
    protected Iterator<EnaFastqVO> extractAll() throws ExtractorException
    {
        return new EnaFastqIterator();
    }


    @Override
    public void clear()
    {
        // nothing to clean up
    }


    /**
     * This class represents an {@linkplain Iterator} that iterates through
     * {@linkplain EnaFastqVO}s used for harvesting Ena Fastq datasets by
     * trying out all IDs in a range of 000000 to 999999.
     *
     * @author Komal Ahir
     * @author Robin Weiss
     */
    private class EnaFastqIterator implements Iterator<EnaFastqVO>
    {
        private int id = 1;


        @Override
        public boolean hasNext()
        {
            return id <= size();
        }


        @Override
        public EnaFastqVO next()
        {
            final String url = String.format(EnaFastqConstants.FASTQ_JSON_URL, accessionPrefix, id);
            final List<EnaFastqVO> voList = httpRequester.getObjectFromUrl(url, EnaFastqConstants.JSON_TYPE);
            id++;

            return voList == null || voList.isEmpty() ? null : voList.get(0);
        }
    }
}