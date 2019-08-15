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
import de.gerdiproject.harvest.config.events.ParameterChangedEvent;
import de.gerdiproject.harvest.config.parameters.StringParameter;
import de.gerdiproject.harvest.config.parameters.constants.ParameterMappingFunctions;
import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaParameterConstants;
import de.gerdiproject.harvest.etls.extractors.EnaTaxonExtractor;
import de.gerdiproject.harvest.etls.transformers.EnaTransformer;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * An ETL for harvesting ENA taxa. The parameter 'taxon' sets the taxon identifier of the taxon that is to be harvested.<br>
 *
 * E.g. https://www.ebi.ac.uk/ena/data/view/Taxon:9606<br>
 *
 * @author Jan Frömberg, Robin Weiss
 */
public class EnaTaxonETL extends StaticIteratorETL<Element, DataCiteJson>
{
    private StringParameter taxonParam;


    /**
     * Constructor.
     */
    public EnaTaxonETL()
    {
        super(new EnaTaxonExtractor(), new EnaTransformer());
    }


    @Override
    protected void registerParameters()
    {
        super.registerParameters();

        final Function<String, String> accessionNumberChecker =
            ParameterMappingFunctions.createMapperForETL(EnaTaxonETL::mapStringToTaxonId, this);

        this.taxonParam = Configuration.registerParameter(
                              new StringParameter(
                                  EnaParameterConstants.PROPERTY_TAXON_KEY,
                                  getName(),
                                  EnaParameterConstants.ENTRY_DEFAULT_TAXON,
                                  accessionNumberChecker));
    }


    /**
     * Helper function that validates a specified taxon ID.
     *
     * @param taxonId the value that is to be checked
     *
     * @throws RuntimeException if the value is not a taxon ID
     * @return a valid taxon ID
     */
    private static String mapStringToTaxonId(final String taxonId) throws RuntimeException
    {
        if (taxonId == null || taxonId.isEmpty())
            return "";

        // check if the ID consists of only numbers
        try {
            Integer.parseInt(taxonId);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(String.format(EnaConstants.INVALID_TAXON_ID_ERROR, taxonId)); // NOPMD stack trace not needed
        }

        return taxonId;
    }


    /**
     * Returns the taxon ID of the taxon that is to be harvested.
     *
     * @return the taxon ID of the taxon that is to be harvested
     */
    public String getTaxonId()
    {
        return taxonParam.getValue().isEmpty() ? null : taxonParam.getValue();
    }


    //////////////////////////////
    // Event Callback Functions //
    //////////////////////////////

    @Override
    protected void onParameterChanged(final ParameterChangedEvent event)
    {
        super.onParameterChanged(event);

        final String paramKey = event.getParameter().getCompositeKey();

        // if the accession number changed, re-init the extractor to recalculate the max
        // number of harvestable documents
        if (this.extractor != null && paramKey.equals(taxonParam.getCompositeKey()))
            this.extractor.init(this);
    }
}
