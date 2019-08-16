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
package de.gerdiproject.harvest.ena.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.gerdiproject.json.datacite.ResourceType;
import de.gerdiproject.json.datacite.enums.ResourceTypeGeneral;
import de.gerdiproject.json.datacite.extension.generic.AbstractResearch;
import de.gerdiproject.json.datacite.extension.generic.constants.ResearchDisciplineConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A static collection of constant parameters for configuring the ENA Harvester.
 *
 * @author Jan Frömberg
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnaConstants
{
    public static final String PROVIDER = "European Nucleotide Archive (ENA)";
    public static final String TITLE = "Sequence: %s.%s";
    public static final String SIZE_PREFIX = "Sequence length: ";
    public static final String SEQ_DATA = "Sequencing Data";

    public static final String XML = "XML";
    public static final String FASTA = "FASTA";
    public static final String TXT = "TEXT";

    public static final List<String> FORMATS = Collections.unmodifiableList(Arrays.asList(XML, FASTA, TXT));
    public static final List<AbstractResearch> DISCIPLINES = createResearchDisciplines();

    public static final String VERSION = "version";
    public static final String ACCESSION = "accession";
    public static final String SEQUENCE_LENGTH = "sequenceLength";
    public static final String FIRST_PUBLIC = "firstPublic";
    public static final String LAST_UPDATED = "lastUpdated";
    public static final String DESCRIPTION = "description";
    public static final String ENTRY_COMMENT = "entry > comment";
    public static final String KEYWORD = "keyword";
    public static final String DATACLASS = "dataClass";

    public static final String MOLECULETYPE = "moleculeType";

    public static final String REFERENCE = "reference";
    public static final String REF_TYPE = "type";
    public static final String REF_ARTICLE = "article";
    public static final String REF_SUBMISSION = "submission";
    public static final String REF_SUBMISSION_DATE = "submissionDate";
    public static final String REF_SUBMISSION_COMMENT = "comment";
    public static final String REF_ATTR_ID = "id";

    public static final String TAXON = "taxon";
    public static final String TAX_DIVISION = "taxonomicDivision";
    public static final String TAX_SCIENTIFIC_NAME = "scientificName";
    public static final String TAX_ID = "taxId";
    public static final String TAX_COMMON_NAME = "commonName";

    public final static String LETTER_PREFIX_REGEX = "^[a-zA-Z]+";
    public static final String NUMBER_REGEX = "[0-9]";
    public static final String ACCESSION_FORMAT_BASE = "%s%%0%dd";
    public static final String URL_ERROR = "Could not retrieve entries from: %s";

    public static final String DOI_REF_SELECTION = "reference[type=article] > xref[db=DOI]";
    public static final String PUBMED_REF_SELECTION = "reference[type=article] > xref[db=PUBMED]";
    public static final String SUBMISSION_REF_SELECTION = "reference[type=submission]";
    public static final String USER_AGENT = "Mozilla/5.0";
    public static final String TITLE_FASTQ_FILE = "TITLE";
    public static final String RUN_ATTRIBUTE = "RUN_ATTRIBUTE";
    public static final String ENA_LAST_UPDATE = "ENA-LAST-UPDATE";
    public static final String ENA_FIRST_PUBLIC = "ENA-FIRST-PUBLIC";
    public static final String SUBJECT_FASTQ = "FASTQ";
    public static final String ALTERNATE_ID = "PRIMARY_ID";
    public static final String ERR_ID = "ERR";
    public static final int FASTQ_DOCUMENT_COUNT = 999999;

    public static final ResourceType RESOURCE_TYPE = new ResourceType(SEQ_DATA, ResourceTypeGeneral.Dataset);

    /**
     * Valid Accession Numbers<br>
     * Source: https://www.ebi.ac.uk/ena/submit/accession-number-formats
     */
    public static final List<String> ACCESSION_NUMBER_FORMATS = Collections.unmodifiableList(Arrays.asList(
                                                                    // Asssembled/Annotated sequences
                                                                    "[A-Z]{1}\\d{5}(?\\.\\d+)?",
                                                                    "[A-Z]{2}\\d{6}(?\\.\\d+)?",
                                                                    "[A-Z]{4}S?\\d{8,9}(?\\.\\d+)?",

                                                                    // Protein coding sequences
                                                                    "[A-Z]{3}\\d{5}(?\\.\\d+",

                                                                    // Traces
                                                                    "TI\\d+",

                                                                    // Studies
                                                                    "(E|D|S)RP\\d{6,}",
                                                                    "PRJ(E|D|N)\\d+",
                                                                    // Samples
                                                                    "ERS\\d{6,}",
                                                                    "SAM(E|D|N)[A-Z]?\\\\d+",

                                                                    // Experiments
                                                                    "(E|D|S)RX\\d{6,}",

                                                                    // Runs
                                                                    "(E|D|S)RR\\d{6,}",

                                                                    // Analyses
                                                                    "(E|D|S)RZ\\d{6,}",

                                                                    // Genome collections
                                                                    "GCA_\\d{9}(?\\.\\d+)?"
                                                                ));

    public static final String INVALID_ENTRY_RESPONSE = "Entry:  display type is either not supported or entry is not found.";

    public static final String INVALID_TAXON_ID_ERROR = "Invalid taxon id '%s'! It must contain only numbers!";
    public static final String INVALID_ACCESSION_ERROR = "Invalid accession number '%s'! Check valid values at "
                                                         + "https://www.ebi.ac.uk/ena/submit/accession-number-formats";
    public static final String TAXON_SIZE_ERROR = "Could not estimate the max number of documents for harvesting the taxon: %s";


    /**
    * Create a list of research disciplines.
    *
    * @return a list of research disciplines that fits ENA.
    */
    private static List<AbstractResearch> createResearchDisciplines()
    {
        return Collections.unmodifiableList(Arrays.asList(
                                                ResearchDisciplineConstants.STRUCTURAL_BIOLOGY,
                                                ResearchDisciplineConstants.BIOINFORMATICS_AND_THEORETICAL_BIOLOGY,
                                                ResearchDisciplineConstants.CELL_BIOLOGY
                                            ));
    }
}
