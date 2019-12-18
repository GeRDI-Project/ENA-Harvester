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
package de.gerdiproject.harvest.etls.transformers;

import java.io.File;
import java.util.List;

import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.EnaContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.EnaTaxonETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaReferenceVO;
import de.gerdiproject.harvest.etls.extractors.vos.EnaTaxonVO;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain EnaTaxonTransformer}.
 *
 * @author Robin Weiss
 */
public class EnaTaxonTransformerTest extends AbstractIteratorTransformerTest<EnaTaxonVO, DataCiteJson>
{
    private static final String INPUT_XML_RESOURCE = "input-xml.xml";
    private static final String INPUT_REF_RESOURCE = "input-references.json";


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
    protected EnaTaxonVO getMockedInput()
    {
        final File xmlResource = getResource(INPUT_XML_RESOURCE);
        final Element taxonXml = diskIo.getHtml(xmlResource.toString());

        final File referencesResource =  getResource(INPUT_REF_RESOURCE);
        final List<EnaReferenceVO> references = diskIo.getObject(referencesResource.toString(), EnaConstants.REFERENCE_LIST_TYPE);

        return new EnaTaxonVO(taxonXml, references);
    }
}
