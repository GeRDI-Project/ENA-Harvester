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

import de.gerdiproject.harvest.config.parameters.AbstractParameter;
import de.gerdiproject.harvest.config.parameters.StringParameter;
import de.gerdiproject.harvest.ena.constants.ENAParameterConstants;
import de.gerdiproject.harvest.harvester.EnaHarvester;

import java.util.Arrays;
import java.util.List;

import javax.servlet.annotation.WebListener;

/**
 * This class initializes the ENA harvester and a logger
 *
 * @author Jan Frömberg
 */
@WebListener
public class EnaContextListener extends ContextListener<EnaHarvester>
{
    @Override
    protected List<AbstractParameter<?>> getHarvesterSpecificParameters()
    {
        StringParameter propertyFrom = new StringParameter(
            ENAParameterConstants.PROPERTY_FROM_KEY,
            ENAParameterConstants.ENTRY_DEFAULT_FROM);

        StringParameter propertyTo = new StringParameter(
            ENAParameterConstants.PROPERTY_TO_KEY,
            ENAParameterConstants.ENTRY_DEFAULT_TO);

        StringParameter propertyTaxon = new StringParameter(
                ENAParameterConstants.PROPERTY_TAXON_KEY,
                ENAParameterConstants.ENTRY_DEFAULT_TAXON);
        
        return Arrays.asList(propertyFrom, propertyTo, propertyTaxon);
    }

}