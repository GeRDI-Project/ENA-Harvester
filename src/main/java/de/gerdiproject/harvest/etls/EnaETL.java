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
package de.gerdiproject.harvest.etls;

import java.util.function.Function;

import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.config.Configuration;
import de.gerdiproject.harvest.config.parameters.AbstractParameter;
import de.gerdiproject.harvest.config.parameters.StringParameter;
import de.gerdiproject.harvest.config.parameters.constants.ParameterMappingFunctions;
import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaParameterConstants;
import de.gerdiproject.harvest.etls.extractors.EnaExtractor;
import de.gerdiproject.harvest.etls.transformers.EnaTransformer;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * An ETL for the ENA Database There are parameters to setup a harvestable
 * range: accfrom and accto. See ENAParameterConstants
 * (https://www.ebi.ac.uk/ena/data/view/Taxon:Human,Taxon:Cat,Taxon:Mouse,Taxon:Zebrafish,Taxon:Bacillus%20Subtilis).
 * To harvest only house mouse taxon:
 * https://www.ebi.ac.uk/ena/data/view/Taxon:10090&portal=sequence_release&display=xml
 * usage hint: how many records are available for a single result set -> use the
 * "resultcount" parameter
 *
 * @author Jan Frömberg, Robin Weiss
 */
public class EnaETL extends StaticIteratorETL<Element, DataCiteJson>
{
    private StringParameter accFromParam;


    /**
     * Constructor.
     */
    public EnaETL()
    {
        super(new EnaExtractor(), new EnaTransformer());
    }


    @Override
    protected void registerParameters()
    {
        super.registerParameters();

        final Function<String, String> accessionNumberChecker =
            ParameterMappingFunctions.createMapperForETL(EnaETL::mapStringToAccessionNumber, this);

        this.accFromParam = Configuration.registerParameter(
                                new StringParameter(
                                    EnaParameterConstants.PROPERTY_FROM_KEY,
                                    getName(),
                                    EnaParameterConstants.ENTRY_DEFAULT_FROM,
                                    accessionNumberChecker));
    }


    /**
     * Helper function that validates a specified accession number.
     *
     * @param accessionNumber the value that is to be checked
     *
     * @throws RuntimeException if the value is not a valid accession number
     * @return a valid accession number
     */
    private static String mapStringToAccessionNumber(String accessionNumber) throws RuntimeException
    {
        for (String regex : EnaConstants.ACCESSION_NUMBER_FORMATS)
            if (accessionNumber.matches(regex))
                return accessionNumber;

        throw new IllegalArgumentException(String.format(EnaConstants.INVALID_ACCESSION_ERROR, accessionNumber));
    }


    /**
     * Returns the first accession number of the range that is to be harvested.
     *
     * @return the first accession number of the range that is to be harvested
     */
    public String getStartingAccessionNumber()
    {
        return accFromParam.getValue();
    }


    /**
     * Adds a specified number to the number of harvested documents.
     * In this case, the number refers to null-entries.
     *
     * @param addedDocumentCount the number that is added to the
     * number of harvested documents
     */
    public void increaseHarvestedDocuments(int addedDocumentCount)
    {
        harvestedCount.addAndGet(addedDocumentCount);
    }


    //////////////////////////////
    // Event Callback Functions //
    //////////////////////////////

    @Override
    protected void onParameterChanged(AbstractParameter<?> param)
    {
        super.onParameterChanged(param);

        final String paramKey = param.getCompositeKey();

        // if the accession number changed, re-init the extractor to recalculate the max
        // number of harvestable documents
        if (this.extractor != null && paramKey.equals(accFromParam.getCompositeKey()))
            this.extractor.init(this);
    }
}
