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
package de.gerdiproject.harvest.etls.extractors.vos;

import com.google.gson.annotations.SerializedName;

import lombok.Value;

/**
 * This class is a value object that contains the response of
 * a FASTQ filereport request.<br>
 *
 * e.g. https://www.ebi.ac.uk/ena/portal/api/filereport?result=read_run&accession=ERR000001&offset=0&limit=1000&format=json
 *
 * @author Robin Weiss
 */
@Value
public class EnaFastqVO
{
    @SerializedName("study_accession")
    private final String studyAccession;

    @SerializedName("secondary_study_accession")
    private final String secondaryStudyAccession;

    @SerializedName("sample_accession")
    private final String sampleAccession;

    @SerializedName("secondary_sample_accession")
    private final String secondarySampleAccession;

    @SerializedName("experiment_accession")
    private final String experimentAccession;

    @SerializedName("run_accession")
    private final String runAccession;

    @SerializedName("submission_accession")
    private final String submissionAccession;

    @SerializedName("tax_id")
    private final String taxId;

    @SerializedName("scientific_name")
    private final String scientificName;

    @SerializedName("instrument_platform")
    private final String instrumentPlatform;

    @SerializedName("instrument_model")
    private final String instrumentModel;

    @SerializedName("library_name")
    private final String libraryName;

    @SerializedName("nominal_length")
    private final String nominalLength;

    @SerializedName("library_layout")
    private final String libraryLayout;

    @SerializedName("library_strategy")
    private final String libraryStrategy;

    @SerializedName("library_source")
    private final String librarySource;

    @SerializedName("library_selection")
    private final String librarySelection;

    @SerializedName("read_count")
    private final String readCount;

    @SerializedName("base_count")
    private final String baseCount;

    @SerializedName("center_name")
    private final String centerName;

    @SerializedName("first_public")
    private final String firstPublic;

    @SerializedName("last_updated")
    private final String lastUpdated;

    @SerializedName("experiment_title")
    private final String experimentTitle;

    @SerializedName("study_title")
    private final String studyTitle;

    @SerializedName("study_alias")
    private final String studyAlias;

    @SerializedName("experiment_alias")
    private final String experimentAlias;

    @SerializedName("run_alias")
    private final String runAlias;

    @SerializedName("fastq_bytes")
    private final String fastqNytes;

    @SerializedName("fastq_md5")
    private final String fastqMd5;

    @SerializedName("fastq_ftp")
    private final String fastqFtp;

    @SerializedName("fastq_aspera")
    private final String fastqAspera;

    @SerializedName("fastq_galaxy")
    private final String fastqGalaxy;

    @SerializedName("submitted_bytes")
    private final String submittedBytes;

    @SerializedName("submitted_md5")
    private final String submittedMd5;

    @SerializedName("submitted_ftp")
    private final String submittedFtp;

    @SerializedName("submitted_aspera")
    private final String submittedAspera;

    @SerializedName("submitted_galaxy")
    private final String submittedGalaxy;

    @SerializedName("submitted_format")
    private final String submittedFormat;

    @SerializedName("sra_bytes")
    private final String sraBytes;

    @SerializedName("sra_md5")
    private final String sraMd5;

    @SerializedName("sra_ftp")
    private final String sraFtp;

    @SerializedName("sra_aspera")
    private final String sraAspera;

    @SerializedName("sra_galaxy")
    private final String sraGalaxy;

    @SerializedName("cram_index_ftp")
    private final String cramIndexFtp;

    @SerializedName("cram_index_aspera")
    private final String cramIndexAspera;

    @SerializedName("cram_index_galaxy")
    private final String cramIndexGalaxy;

    @SerializedName("sample_alias")
    private final String sampleAlias;

    @SerializedName("broker_name")
    private final String brokerName;

    @SerializedName("sample_title")
    private final String sampleTitle;

    @SerializedName("nominal_sdev")
    private final String nominalSdev;

    @SerializedName("first_created")
    private final String firstCreated;
}