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
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import de.gerdiproject.harvest.EnaContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.ena.constants.EnaTaxonConstants;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.EnaTaxonETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaReferenceVO;
import de.gerdiproject.harvest.etls.extractors.vos.EnaTaxonVO;
import de.gerdiproject.harvest.utils.data.HttpRequesterUtils;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain EnaTaxonExtractor}.
 *
 * @author Robin Weiss
 */
public class EnaTaxonExtractorTest extends AbstractIteratorExtractorTest<EnaTaxonVO>
{
    private static final Type REF_LIST_TYPE = new TypeToken<List<EnaReferenceVO>>() {} .getType();


    @Override
    protected ContextListener getContextListener()
    {
        return new EnaContextListener();
    }


    @Override
    protected AbstractIteratorETL<EnaTaxonVO, DataCiteJson> getEtl()
    {
        return new EnaTaxonETL();
    }


    @Override
    protected EnaTaxonVO getExpectedOutput()
    {
        return getExpectedOutput(1);
    }


    private EnaTaxonVO getExpectedOutput(final int taxId)
    {
        final File xmlResource = HttpRequesterUtils.urlToFilePath(
                                     String.format(EnaTaxonConstants.XML_URL, taxId),
                                     getMockedHttpResponseFolder());
        final Document xml = diskIo.getHtml(xmlResource.toString());
        final Element taxonXml = xml.selectFirst(EnaTaxonConstants.SET_ELEMENT).child(0);

        final File referencesResource =  HttpRequesterUtils.urlToFilePath(
                                             String.format(EnaTaxonConstants.REFERENCE_URL, taxId),
                                             getMockedHttpResponseFolder());
        final List<EnaReferenceVO> references = diskIo.getObject(referencesResource.toString(), REF_LIST_TYPE);

        return new EnaTaxonVO(taxonXml, references);
    }


    /**
     * Checks if the {@linkplain EnaTaxonExtractor}
     * extracts the expected output when extracting twice.
     */
    @Test
    public void testExtractSecondElement()
    {
        final Iterator<EnaTaxonVO> iter = testedObject.extract();

        // skip first element
        iter.next();

        final EnaTaxonVO actualOutput = iter.next();
        final EnaTaxonVO expectedOutput =  getExpectedOutput(2);

        assertExpectedOutput(expectedOutput, actualOutput);
    }
}
