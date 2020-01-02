/**
 * Copyright © 2017 Jan Frömberg (http://www.gerdi-project.de)
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
package de.gerdiproject.harvest;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.annotation.WebListener;

import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.ena.constants.EnaFastqConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.EnaAccessionETL;
import de.gerdiproject.harvest.etls.EnaFastqETL;
import de.gerdiproject.harvest.etls.EnaTaxonETL;

/**
 * This class initializes the ENA harvester and a logger
 *
 * @author Jan Frömberg
 */
@WebListener
public class EnaContextListener extends ContextListener
{

    @Override
    protected List<? extends AbstractETL<?, ?>> createETLs()
    {
        final List<AbstractETL<?, ?>> etls = new LinkedList<>();
        etls.add(new EnaAccessionETL());
        etls.add(new EnaTaxonETL());

        for (final String accessionPrefix :  EnaFastqConstants.ACCESSION_PREFIXES)
            etls.add(new EnaFastqETL(accessionPrefix));

        return etls;
    }
}