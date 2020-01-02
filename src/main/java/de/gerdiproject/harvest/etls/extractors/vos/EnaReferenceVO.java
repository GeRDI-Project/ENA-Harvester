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
package de.gerdiproject.harvest.etls.extractors.vos;

import com.google.gson.annotations.SerializedName;

import lombok.Value;

/**
 * This value object is part of a publications request.
 *
 * @author Robin Weiss
 */
@Value
public class EnaReferenceVO
{
    @SerializedName("Source")
    private final String source;

    @SerializedName("Source Primary Accession")
    private final String sourcePrimaryAccession;

    @SerializedName("Source Secondary Accession")
    private final String sourceSecondaryAccession;

    @SerializedName("Source URL")
    private final String sourceUrl;

    @SerializedName("Target")
    private final String target;

    @SerializedName("Target Primary Accession")
    private final String targetPrimaryAccession;

    @SerializedName("Target Secondary Accession")
    private final String targetSecondaryAccession;

    @SerializedName("Target URL")
    private final String targetUrl;

    @SerializedName("DOI")
    private final String doi;

    private final String reference;
}
