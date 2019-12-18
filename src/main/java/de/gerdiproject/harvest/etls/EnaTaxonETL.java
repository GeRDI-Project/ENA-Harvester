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
package de.gerdiproject.harvest.etls;

import de.gerdiproject.harvest.config.Configuration;
import de.gerdiproject.harvest.config.parameters.IntegerParameter;
import de.gerdiproject.harvest.config.parameters.constants.ParameterMappingFunctions;
import de.gerdiproject.harvest.ena.constants.EnaParameterConstants;
import de.gerdiproject.harvest.etls.extractors.EnaTaxonExtractor;
import de.gerdiproject.harvest.etls.extractors.vos.EnaTaxonVO;
import de.gerdiproject.harvest.etls.transformers.EnaTaxonTransformer;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * An ETL for harvesting ENA taxa. The parameter 'taxon' sets the taxon identifier of the taxon that is to be harvested.<br>
 *
 * E.g. https://www.ebi.ac.uk/ena/data/view/Taxon:9606<br>
 *
 * @author Jan Frömberg, Robin Weiss
 */
public class EnaTaxonETL extends StaticIteratorETL<EnaTaxonVO, DataCiteJson>
{
    private IntegerParameter batchSize;

    /**
     * Constructor.
     */
    public EnaTaxonETL()
    {
        super(new EnaTaxonExtractor(), new EnaTaxonTransformer());
    }

    @Override
    protected void registerParameters()
    {
        super.registerParameters();

        this.batchSize = Configuration.registerParameter(new IntegerParameter(
                                                             EnaParameterConstants.TAXON_BATCH_SIZE_KEY,
                                                             getName(),
                                                             EnaParameterConstants.TAXON_BATCH_SIZE_DEFAULT_VALUE,
                                                             ParameterMappingFunctions.createMapperForETL(ParameterMappingFunctions::mapToUnsignedInteger, this)));
    }


    /**
     * Returns the number of taxon responses that may be processed at the same time.
     * Increasing the value will increase the harvesting speed, but could cause Out-of-Memory-Exceptions.
     *
     * @return the number of taxon responses that may be processed at the same time
     */
    public int getBatchSize()
    {
        return batchSize.getValue();
    }
}
