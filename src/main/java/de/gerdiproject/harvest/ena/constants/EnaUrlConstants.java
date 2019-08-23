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

import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A static collection of constant parameters for assembling ENA URLs.
 *
 * @author Jan Frömberg
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnaUrlConstants
{
    // URLs
    //private static final String PROVIDER_URL = "https://www.ebi.ac.uk/ena";
    public final static String RANGE_ACCESSION_URL = "https://www.ebi.ac.uk/ena/data/view/%s-%s&display=xml&header=true";
    public final static String SINGLE_ACCESSION_URL = "https://www.ebi.ac.uk/ena/data/view/%s&display=xml&header=true";

    public final static String TAXON_CSV_URL = "ftp://ftp.ebi.ac.uk/pub/databases/ena/taxonomy/sdwca/ENA_120913vsCoL_150813_xmapping_result.csv";
    public static final String TAXON_VIEW_URL = "https://www.ebi.ac.uk/ena/data/view/Taxon:%s";
    public final static String TAXON_URL = "https://www.ebi.ac.uk/ena/data/view/Taxon:%s&portal=sequence_update&display=xml&header=true&offset=%d&length=%3$d&limit=%3$d&display=xml";
    public final static String TAXON_SIZE_URL = "https://www.ebi.ac.uk/ena/data/view/Taxon:%s&result=sequence_update&header=false&offset=%d&length=2&display=txt";

    public static final String VIEW_URL = "https://www.ebi.ac.uk/ena/data/view/%s";
    public static final String VIEW_URL_TEXT = "https://www.ebi.ac.uk/ena/data/view/%s&display=text";
    public static final String VIEW_URL_XML = "https://www.ebi.ac.uk/ena/data/view/%s&display=xml";
    public static final String VIEW_URL_FASTA = "https://www.ebi.ac.uk/ena/data/view/%s&display=fasta";

    public static final String DOWNLOAD_URL_TEXT = VIEW_URL_TEXT + "&download=txt&filename=%s.txt";
    public static final String DOWNLOAD_URL_XML = VIEW_URL_XML + "&download=xml&filename=%s.xml";
    public static final String DOWNLOAD_URL_FASTA = VIEW_URL_FASTA + "&download=fasta&filename=%s.fasta";

    public static final String VERSION_HISTORY_URL = "https://www.ebi.ac.uk/cgi-bin/sva/sva.pl?search=Go&amp;query=%s";

    public static final String THUMBNAIL_URL = "https://www.ebi.ac.uk/ena/data/view/graphics/%s&showSequence=false&featureRange=1-%s";

    public static final String VIEW_URL_FASTQ = "https://www.ebi.ac.uk/ena/data/view/ERR%06d&display=xml";
    public static final String DOWNLOAD_URL_FASTQ = "https://www.ebi.ac.uk/ena/data/warehouse/filereport?accession=ERR%06d&result=read_run&fields=fastq_ftp";

    //NAMES
    public final static String VIEW_URL_NAME = "View website";
    public final static String TAXON_URL_NAME = "View Taxon";
    public final static String VIEW_URL_TXT_NAME = "View plain text";
    public final static String VIEW_URL_XML_NAME = "View XML";
    public final static String VIEW_URL_FASTA_NAME = "View FASTA";
    public final static String VERSION_HISTORY_URL_NAME = "Version History";
    public final static String PREVIEW_IMAGE_NAME = "Overview";

    public static final WebLink LOGO_LINK = new WebLink(
        "https://www.ebi.ac.uk/web_guidelines/images/logos/ena/ENA-logo.png",
        "Logo",
        WebLinkType.ProviderLogoURL);
    public final static String LOGO_URL_NAME = "Logo";
    public final static String VIEW_URL_FASTQ_NAME = "FASTQ File";
}
