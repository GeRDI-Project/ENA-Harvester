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

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.gerdiproject.harvest.EnaContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.application.MainContextUtils;
import de.gerdiproject.harvest.ena.constants.EnaFastqConstants;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.EnaFastqETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaFastqVO;
import de.gerdiproject.harvest.utils.data.HttpRequesterUtils;
import de.gerdiproject.harvest.utils.data.constants.DataOperationConstants;
import de.gerdiproject.harvest.utils.file.FileUtils;
import de.gerdiproject.json.datacite.DataCiteJson;
import lombok.RequiredArgsConstructor;

/**
 * This class provides Unit Tests for the {@linkplain EnaFastqExtractor}.
 *
 * @author Robin Weiss
 */
@RunWith(Parameterized.class) @RequiredArgsConstructor
public class EnaFastqExtractorTest extends AbstractIteratorExtractorTest<EnaFastqVO>
{
    private static final String INPUT_RESOURCE = "shortenedHttpRequests/%s.response";


    @Parameters(name = "accession: {0}000001")
    public static Object[] getParameters()
    {
        return EnaFastqConstants.ACCESSION_PREFIXES.toArray();
    }


    private final String accessionPrefix;

    @Override
    protected ContextListener getContextListener()
    {
        return new EnaContextListener();
    }


    @Override
    protected AbstractIteratorETL<EnaFastqVO, DataCiteJson> getEtl()
    {
        return new EnaFastqETL(accessionPrefix);
    }


    @Override
    protected File getMockedHttpResponseFolder()
    {
        // due to exessively long file paths, the mocked http responses cannot be copied in the usual way
        return null;
    }


    @Override
    protected AbstractIteratorExtractor<EnaFastqVO> setUpTestObjects()
    {
        // copy mocked http response
        final File httpResource = getResource(String.format(INPUT_RESOURCE, accessionPrefix));

        final File httpCacheFolder = new File(
            MainContextUtils.getCacheDirectory(getClass()),
            DataOperationConstants.CACHE_FOLDER_PATH);

        final File httTempResource = HttpRequesterUtils.urlToFilePath(
                                         String.format(EnaFastqConstants.FASTQ_JSON_URL, accessionPrefix, 1),
                                         httpCacheFolder);

        FileUtils.copyFile(httpResource, httTempResource);

        return super.setUpTestObjects();
    }
}
