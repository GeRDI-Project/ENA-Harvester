/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
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
package de.gerdiproject.harvest.ena.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.gerdiproject.json.datacite.extension.abstr.AbstractResearch;
import de.gerdiproject.json.datacite.extension.constants.ResearchDisciplineConstants;

/**
 * A static collection of constant parameters for configuring the ENA Harvester.
 *
 * @author Jan Frömberg
 *
 */
public class ENAConstants
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
    public static final String COMMENT = "comment";
    public static final String KEYWORD = "keyword";
    public static final String DATACLASS = "dataClass";

    public static final String MOLECULETYPE = "moleculeType";

    public static final String REFERENCE = "reference";
    public static final String REF_TYPE = "type";
    public static final String REF_ARTICLE = "article";
    public static final String REF_SUBMISSION = "submission";
    public static final String REF_SUBMISSION_DATE = "submissionDate";
    public static final String REF_ATTR_ID = "id";

    public static final String TAXON = "taxon";
    public static final String TAX_DIVISION = "taxonomicDivision";
    public static final String TAX_SCIENTIFIC_NAME = "scientificName";
    public static final String TAX_ID = "taxId";
    public static final String TAX_COMMON_NAME = "commonName";

    /**
     * Private Constructor, because this is a static class.
     */
    private ENAConstants()
    {
    }

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
