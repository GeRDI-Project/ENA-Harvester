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
import java.util.Iterator;

import org.jsoup.nodes.Element;
import org.junit.Test;

import de.gerdiproject.harvest.EnaContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.EnaAccessionETL;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain EnaAccessionExtractor}.
 *
 * @author Robin Weiss
 */
public class EnaAccessionExtractorTest extends AbstractIteratorExtractorTest<Element>
{
    private static final String OUTPUT_RESOURCE = "output-%d.html";


    @Override
    protected ContextListener getContextListener()
    {
        return new EnaContextListener();
    }


    @Override
    protected AbstractIteratorETL<Element, DataCiteJson> getEtl()
    {
        return new EnaAccessionETL();
    }


    @Override
    protected Element getExpectedOutput()
    {
        return getExpectedOutput(1);
    }


    private Element getExpectedOutput(final int id)
    {
        final File resource = getResource(String.format(OUTPUT_RESOURCE, id));
        return diskIo.getHtml(resource.toString()).selectFirst(EnaConstants.ENTRY);
    }


    /**
     * Checks if the {@linkplain EnaAccessionExtractor}
     * extracts the expected output when extracting twice.
     */
    @Test
    public void testExtractSecondElement()
    {
        final Iterator<Element> iter = testedObject.extract();

        // skip first element
        iter.next();

        final Element actualOutput = iter.next();
        final Element expectedOutput =  getExpectedOutput(2);

        assertExpectedOutput(expectedOutput, actualOutput);
    }
}
