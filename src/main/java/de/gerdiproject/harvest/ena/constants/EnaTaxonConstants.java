/**
 * Copyright © 2019 Robin Weiß (http://www.gerdi-project.de)
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
package de.gerdiproject.harvest.ena.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class offers constants for harvesting ENA taxa.
 *
 * @author Robin Weiß
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnaTaxonConstants
{
    // URLs
    public static final String CSV_URL = "ftp://ftp.ebi.ac.uk/pub/databases/ena/taxonomy/sdwca/ENA_120913vsCoL_150813_xmapping_result.csv";
    public static final String VIEW_URL = "https://www.ebi.ac.uk/ena/browser/view/Taxon:%s";
    public static final String XML_URL = "https://www.ebi.ac.uk/ena/browser/api/xml/Taxon:%s";
    public static final String REFERENCE_URL = "https://www.ebi.ac.uk/ena/xref/rest/json/search?accession=%s&expanded=true";
    public static final String DOWNLOAD_XML_URL = XML_URL + "?download=true";


    // Extraction
    public static final String QUEUE_FOLDER = "taxonQueue";
    public static final String CHILDREN_ELEMENT = "children";
    public static final String TAXON_ID_ATTRIBUTE = "taxId";
    public static final String TAXON_ROOT_ID = "1";


    // Transformation
    public static final String VIEW_URL_NAME = "View Taxon";
    public static final String DESCRIPTION = "%s [%s]";
    public static final String ID_TITLE = "Taxon: %s";
    public static final String NAME_LANGUAGE = "en";

    public static final String TAXON_ELEMENT = "taxon";
    public static final String SYNONYM_ELEMENT = "synonym";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String SCIENTIFIC_NAME_ATTRIBUTE = "scientificName";
    public static final String TAX_DIVISION_ATTRIBUTE = "taxonomicDivision";
    public static final String RANK_ATTRIBUTE = "rank";
    public static final String CITATION_REF_TYPE = "Citation";
    public static final List<String> FORMATS = Collections.unmodifiableList(Arrays.asList("XML"));
}
