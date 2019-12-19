/**
 * Copyright © 2019 Robin Weiss (http://www.gerdi-project.de)
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

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import de.gerdiproject.harvest.etls.extractors.vos.EnaFastqVO;
import de.gerdiproject.json.datacite.Subject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A static collection of constant parameters for assembling ENA URLs.
 *
 * @author Komal Ahir
 * @author Robin Weiss
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnaFastqConstants
{
    public static final String[] ACCESSION_PREFIXES = {"ERR", "DRR", "SRR"};
    public static final String ALL_FIELDS =
        "study_accession,secondary_study_accession,sample_accession,secondary_sample_accession,"
        + "experiment_accession,run_accession,submission_accession,tax_id,scientific_name,instrument_platform,"
        + "instrument_model,library_name,nominal_length,library_layout,library_strategy,library_source,"
        + "library_selection,read_count,base_count,center_name,first_public,last_updated,experiment_title,"
        + "study_title,study_alias,experiment_alias,run_alias,fastq_bytes,fastq_md5,fastq_ftp,fastq_aspera,"
        + "fastq_galaxy,submitted_bytes,submitted_md5,submitted_ftp,submitted_aspera,submitted_galaxy,"
        + "submitted_format,sra_bytes,sra_md5,sra_ftp,sra_aspera,sra_galaxy,cram_index_ftp,cram_index_aspera,"
        + "cram_index_galaxy,sample_alias,broker_name,sample_title,nominal_sdev,first_created";

    private static final String SELECTED_FIELDS =
        "fastq_ftp,"
        + "submitted_ftp,"
        + "submitted_format,"
        + "sra_ftp,"
        + "cram_index_ftp,"
        + "sample_alias,"
        + "sample_title,"
        + "first_created,"
        + "first_public,"
        + "last_updated,"
        + "experiment_title,"
        + "study_title,"
        + "library_strategy,"
        + "library_source,"
        + "experiment_alias,"
        + "run_alias,"
        + "instrument_platform,"
        + "scientific_name,"
        + "study_accession,"
        + "secondary_study_accession,"
        + "sample_accession,"
        + "secondary_sample_accession,"
        + "experiment_accession,"
        + "submission_accession,"
        + "run_accession,"
        + "tax_id";

    public static final String FASTQ_JSON_URL = "https://www.ebi.ac.uk/ena/portal/api/filereport?result=read_run&accession=%s%06d&offset=0&limit=1&format=json&fields=" + SELECTED_FIELDS;
    public static final String FASTQ_SOURCE_URL = "https://www.ebi.ac.uk/ena/portal/api/filereport?result=read_run&accession=%s&offset=0&limit=1&format=json&fields=" + ALL_FIELDS;

    public static final String ETL_NAME = "Ena%C%sFastqETL";
    public static final Type JSON_TYPE = new TypeToken<List<EnaFastqVO>>() {} .getType();

    public static final String FTP_URL_PREFIX = "http://";

    public static final String LANGUAGE = "en";

    public static final String VIEW_URL_FASTQ_NAME = "View FASTQ";
    public static final String SOURCE_URL_NAME = "View JSON";
    public static final String VIEW_URL_STUDY_NAME = "Study";
    public static final String VIEW_URL_SECOND_STUDY_NAME = "Secondary Study";
    public static final String VIEW_URL_SAMPLE_NAME = "Sample";
    public static final String VIEW_URL_SECOND_SAMPLE_NAME = "Secondary Sample";
    public static final String VIEW_URL_EXPERIMENT_NAME = "Experiment";
    public static final String VIEW_URL_SUBMISSION_NAME = "Submission";
    public static final String VIEW_URL_TAXON_NAME = "Taxon";


    public static final int FASTQ_DOCUMENT_COUNT = 999999;
    public static final Subject SUBJECT_FASTQ = new Subject("FASTQ");

}
