/**
 * Copyright © 2019 Komal Ahir (http://www.gerdi-project.de)
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
package de.gerdiproject.harvest.etls.transformers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaFastqConstants;
import de.gerdiproject.harvest.ena.constants.EnaTaxonConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaFastqVO;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.Identifier;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.enums.DateType;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.TitleType;
import de.gerdiproject.json.datacite.extension.generic.ResearchData;
import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;

public class EnaFastqTransformer extends AbstractIteratorTransformer<EnaFastqVO, DataCiteJson>
{
    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        // nothing to retrieve from the ETL
    }


    @Override
    protected DataCiteJson transformElement(final EnaFastqVO vo) throws TransformerException
    {
        // create the document
        final String identifierString = vo.getRunAccession();
        final DataCiteJson document = new DataCiteJson(identifierString);

        // add all possible metadata to the document
        document.setIdentifier(new Identifier(identifierString));
        document.setPublisher(EnaConstants.PUBLISHER);
        document.addTitles(getTitles(vo));
        document.addWebLinks(getWebLinks(vo));
        document.addDescriptions(getDescriptions(vo));
        document.addSubjects(getSubjects(vo));

        document.addResearchData(getResearchData(vo.getFastqFtp()));
        document.addResearchData(getResearchData(vo.getSubmittedFtp()));
        document.addResearchData(getResearchData(vo.getSraFtp()));
        document.addResearchData(getResearchData(vo.getCramIndexFtp()));

        final Date creationDate = getDate(vo.getFirstCreated(), DateType.Created);
        final Date publicationDate = getDate(vo.getFirstPublic(), DateType.Available);
        final Date updateDate = getDate(vo.getLastUpdated(), DateType.Updated);
        document.addDates(Arrays.asList(creationDate, publicationDate, updateDate));

        if (publicationDate != null) {
            final int publicationYear = publicationDate.getValueAsDateTime().getYear();
            document.setPublicationYear(publicationYear);
        }

        return document;
    }


    private List<Description> getDescriptions(final EnaFastqVO vo)
    {
        final List<Description> descriptionList = new LinkedList<>();

        final String sampleTitle = vo.getSampleTitle();

        if (sampleTitle != null && !sampleTitle.isEmpty()) {
            descriptionList.add(new Description(
                                    sampleTitle,
                                    DescriptionType.Abstract,
                                    EnaFastqConstants.LANGUAGE));
        }

        final String sampleAlias = vo.getSampleAlias();

        if (sampleAlias != null && !sampleAlias.isEmpty()) {
            descriptionList.add(new Description(
                                    sampleAlias,
                                    DescriptionType.Other,
                                    EnaFastqConstants.LANGUAGE));
        }

        return descriptionList;
    }


    private List<Subject> getSubjects(final EnaFastqVO vo)
    {
        final List<Subject> subjectList = new LinkedList<>();

        final String[] subjectStrings = {
            vo.getLibrarySource(),
            vo.getLibraryStrategy(),
            vo.getExperimentAlias(),
            vo.getRunAlias(),
            vo.getInstrumentPlatform(),
            vo.getScientificName()
        };

        for (final String s : subjectStrings) {
            if (s != null && !s.isEmpty())
                subjectList.add(new Subject(s));
        }

        subjectList.add(EnaFastqConstants.SUBJECT_FASTQ);

        return subjectList;
    }


    private List<WebLink> getWebLinks(final EnaFastqVO vo)
    {
        final List<WebLink> webLinkList = new LinkedList<>();

        webLinkList.add(parseRelatedWebLink(
                            vo.getStudyAccession(),
                            EnaFastqConstants.VIEW_URL_STUDY_NAME));

        webLinkList.add(parseRelatedWebLink(
                            vo.getSecondaryStudyAccession(),
                            EnaFastqConstants.VIEW_URL_SECOND_STUDY_NAME));

        webLinkList.add(parseRelatedWebLink(
                            vo.getSampleAccession(),
                            EnaFastqConstants.VIEW_URL_SAMPLE_NAME));

        webLinkList.add(parseRelatedWebLink(
                            vo.getSecondarySampleAccession(),
                            EnaFastqConstants.VIEW_URL_SECOND_SAMPLE_NAME));

        webLinkList.add(parseRelatedWebLink(
                            vo.getExperimentAccession(),
                            EnaFastqConstants.VIEW_URL_EXPERIMENT_NAME));

        webLinkList.add(parseRelatedWebLink(
                            vo.getSubmissionAccession(),
                            EnaFastqConstants.VIEW_URL_SUBMISSION_NAME));

        webLinkList.add(EnaUrlConstants.LOGO_LINK);

        final String taxId = vo.getTaxId();

        if (taxId != null && !taxId.isEmpty()) {
            webLinkList.add(new WebLink(
                                String.format(EnaTaxonConstants.VIEW_URL, taxId),
                                EnaFastqConstants.VIEW_URL_TAXON_NAME,
                                WebLinkType.Related));
        }

        final String runAccession = vo.getRunAccession();

        if (runAccession != null && !runAccession.isEmpty()) {

            webLinkList.add(new WebLink(
                                String.format(EnaUrlConstants.VIEW_URL, runAccession),
                                EnaFastqConstants.VIEW_URL_FASTQ_NAME,
                                WebLinkType.ViewURL));

            webLinkList.add(new WebLink(
                                String.format(EnaFastqConstants.FASTQ_SOURCE_URL, runAccession),
                                EnaFastqConstants.SOURCE_URL_NAME,
                                WebLinkType.SourceURL));
        }

        return webLinkList;
    }


    private WebLink parseRelatedWebLink(final String accession, final String title)
    {
        WebLink webLink = null;

        if (accession != null && !accession.isEmpty()) {
            final String url = String.format(EnaUrlConstants.VIEW_URL, accession);
            webLink = new WebLink(url, title, WebLinkType.Related);
        }

        return webLink;
    }


    private Date getDate(final String dateString, final DateType dateType)
    {
        return dateString == null || dateString.isEmpty()
               ? null
               : new Date(dateString, dateType);
    }


    private List<Title> getTitles(final EnaFastqVO vo)
    {
        final List<Title> titleList = new LinkedList<>();

        final String mainTitle = vo.getExperimentTitle();
        final String subTitle = vo.getStudyTitle();

        if (mainTitle != null && !mainTitle.isEmpty())
            titleList.add(new Title(mainTitle, null, EnaFastqConstants.LANGUAGE));

        if (subTitle != null && !subTitle.isEmpty())
            titleList.add(new Title(subTitle, TitleType.Subtitle, EnaFastqConstants.LANGUAGE));

        return titleList;
    }


    private List<ResearchData> getResearchData(final String downloadString)
    {
        final List<ResearchData> files = new LinkedList<>();

        if (downloadString != null && !downloadString.isEmpty()) {
            final String[] ftpUrls = downloadString.split(";");

            for (final String ftpUrl : ftpUrls) {
                final String fileName = ftpUrl.substring(ftpUrl.lastIndexOf('/') + 1);
                files.add(new ResearchData(EnaFastqConstants.FTP_URL_PREFIX + ftpUrl, fileName));
            }
        }

        return files;
    }


    @Override
    public void clear()
    {
        // nothing to clean up
    }
}