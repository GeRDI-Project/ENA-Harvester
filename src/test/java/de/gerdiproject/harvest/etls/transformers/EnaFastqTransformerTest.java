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

import de.gerdiproject.harvest.EnaContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.EnaFastqETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaFastqVO;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain EnaFastqTransformer}.
 *
 * @author Robin Weiss
 */
public class EnaFastqTransformerTest extends AbstractIteratorTransformerTest<EnaFastqVO, DataCiteJson>
{
    @Override
    protected ContextListener getContextListener()
    {
        return new EnaContextListener();
    }


    @Override
    protected AbstractIteratorETL<EnaFastqVO, DataCiteJson> getEtl()
    {
        return new EnaFastqETL("MOCK");
    }
}
