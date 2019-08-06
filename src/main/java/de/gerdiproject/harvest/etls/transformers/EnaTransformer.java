/*
 *  Copyright © 2018 Robin Weiss (http://www.gerdi-project.de/)
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
package de.gerdiproject.harvest.etls.transformers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.RelatedIdentifier;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.RelatedIdentifierType;
import de.gerdiproject.json.datacite.enums.RelationType;
import de.gerdiproject.json.datacite.extension.generic.ResearchData;
import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;

/**
 * Transforms entries from the ENA database to {@linkplain DataCiteJson} objects.
 *
 * @author Jan Frömberg, Robin Weiss
 */
public class EnaTransformer extends AbstractIteratorTransformer<Element, DataCiteJson>
{
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd");


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        // nothing to retrieve from the ETL

    }


    @Override
    protected DataCiteJson transformElement(Element entry) throws TransformerException
    {
        // get attributes
        Elements children = entry.children();
        Attributes attributes = entry.attributes();
        String version = attributes.get(EnaConstants.VERSION);
        String accession = attributes.get(EnaConstants.ACCESSION);

        DataCiteJson document = new DataCiteJson(accession);
        document.setVersion(version);
        document.setPublisher(EnaConstants.PROVIDER);
        document.addFormats(EnaConstants.FORMATS);
        document.setResourceType(EnaConstants.RESOURCE_TYPE);
        document.addResearchDisciplines(EnaConstants.DISCIPLINES);

        // get size
        String sequenceLength = EnaConstants.SIZE_PREFIX + attributes.get(EnaConstants.SEQUENCE_LENGTH);
        document.addSizes(Arrays.asList(sequenceLength));

        // get titles
        Title mainTitle = new Title(String.format(EnaConstants.TITLE, accession, version));
        document.addTitles(Arrays.asList(mainTitle));

        // get source ; TODO: what to do? include it in a further release
        //Source source = new Source(String.format(VIEW_URL_XML, accession), PROVIDER);
        //source.setProviderURI(PROVIDER_URL);
        //document.setSources(source);

        List<AbstractDate> dates = new LinkedList<>();
        Calendar cal = Calendar.getInstance();

        // get publication date
        try {
            cal.setTime(dateFormat.parse(attributes.get(EnaConstants.FIRST_PUBLIC)));
            document.setPublicationYear(cal.get(Calendar.YEAR));

            Date publicationDate = new Date(attributes.get(EnaConstants.FIRST_PUBLIC), DateType.Available);
            dates.add(publicationDate);
        } catch (ParseException e) { //NOPMD do nothing. just do not add the date if it does not exist
        }

        Date updatedDate = new Date(attributes.get(EnaConstants.LAST_UPDATED), DateType.Updated);
        dates.add(updatedDate);

        // get web links
        List<WebLink> links = new LinkedList<>();
        WebLink viewLink = new WebLink(String.format(EnaUrlConstants.VIEW_URL, accession));
        viewLink.setName(EnaUrlConstants.VIEW_URL_NAME);
        viewLink.setType(WebLinkType.ViewURL);
        links.add(viewLink);

        WebLink viewLinkText = new WebLink(String.format(EnaUrlConstants.VIEW_URL_TEXT, accession));
        viewLinkText.setName(EnaUrlConstants.VIEW_URL_TXT_NAME);
        viewLinkText.setType(WebLinkType.ViewURL);
        links.add(viewLinkText);

        WebLink viewLinkXml = new WebLink(String.format(EnaUrlConstants.VIEW_URL_XML, accession));
        viewLinkXml.setName(EnaUrlConstants.VIEW_URL_XML_NAME);
        viewLinkXml.setType(WebLinkType.ViewURL);
        links.add(viewLinkXml);

        WebLink viewLinkFasta = new WebLink(String.format(EnaUrlConstants.VIEW_URL_FASTA, accession));
        viewLinkFasta.setName(EnaUrlConstants.VIEW_URL_FASTA_NAME);
        viewLinkFasta.setType(WebLinkType.ViewURL);
        links.add(viewLinkFasta);

        WebLink versionHistoryLink = new WebLink(String.format(EnaUrlConstants.VERSION_HISTORY_URL, accession));
        versionHistoryLink.setName(EnaUrlConstants.VERSION_HISTORY_URL_NAME);
        versionHistoryLink.setType(WebLinkType.Related);
        links.add(versionHistoryLink);

        WebLink previewImage = new WebLink(
            String.format(EnaUrlConstants.THUMBNAIL_URL, accession, attributes.get(EnaConstants.SEQUENCE_LENGTH)));
        previewImage.setName(EnaUrlConstants.PREVIEW_IMAGE_NAME);
        previewImage.setType(WebLinkType.ThumbnailURL);
        links.add(previewImage);

        WebLink logoLink = new WebLink(EnaUrlConstants.LOGO_URL);
        logoLink.setName(EnaUrlConstants.LOGO_URL_NAME);
        logoLink.setType(WebLinkType.ProviderLogoURL);
        links.add(logoLink);

        document.addWebLinks(links);

        // get downloads
        List<ResearchData> files = new LinkedList<>();

        ResearchData downloadLinkText = new ResearchData(
            String.format(EnaUrlConstants.DOWNLOAD_URL_TEXT, accession, accession),
            EnaConstants.TXT);
        files.add(downloadLinkText);

        ResearchData downloadLinkXml = new ResearchData(
            String.format(EnaUrlConstants.DOWNLOAD_URL_XML, accession, accession),
            EnaConstants.XML);
        files.add(downloadLinkXml);

        ResearchData downloadLinkFasta = new ResearchData(
            String.format(EnaUrlConstants.DOWNLOAD_URL_FASTA, accession, accession),
            EnaConstants.FASTA);
        files.add(downloadLinkFasta);

        document.addResearchData(files);

        // get descriptions
        List<Description> descriptions = new LinkedList<>();

        Elements descriptionElements = children.select(EnaConstants.DESCRIPTION);

        for (Element descElement : descriptionElements) {
            Description description = new Description(descElement.text(), DescriptionType.Abstract);
            descriptions.add(description);
        }

        Elements commentElements = children.select(EnaConstants.COMMENT);

        for (Element commentElement : commentElements) {
            Description description = new Description(commentElement.text(), DescriptionType.Other);
            descriptions.add(description);
        }

        document.addDescriptions(descriptions);

        // get keyword subjects
        List<Subject> subjects = new LinkedList<>();
        Elements keywordElements = children.select(EnaConstants.KEYWORD);

        for (Element keywordElement : keywordElements) {
            Subject subject = new Subject(keywordElement.text());
            subjects.add(subject);
        }

        // get attribute subjects
        subjects.add(new Subject(attributes.get(EnaConstants.DATACLASS)));
        subjects.add(new Subject(attributes.get(EnaConstants.TAX_DIVISION)));
        subjects.add(new Subject(attributes.get(EnaConstants.MOLECULETYPE)));

        document.addSubjects(subjects);

        List<RelatedIdentifier> relatedIdentifiers = new LinkedList<>();

        // parse references
        Elements referenceElements = children.select(EnaConstants.REFERENCE);

        for (Element refElement : referenceElements) {
            String type = refElement.attr(EnaConstants.REF_TYPE);

            switch (type) {
                default:
                    break;

                case EnaConstants.REF_ARTICLE:

                    // get DOIs
                    Elements doiRefs = refElement.getElementsByAttributeValue("db", "DOI");

                    for (Element doiRef : doiRefs) {
                        relatedIdentifiers.add(
                            new RelatedIdentifier(
                                doiRef.attr(EnaConstants.REF_ATTR_ID),
                                RelatedIdentifierType.DOI,
                                RelationType.IsReferencedBy));
                    }

                    // get PMIDs
                    Elements pmidRefs = refElement.getElementsByAttributeValue("db", "PUBMED");

                    for (Element pmidRef : pmidRefs) {
                        relatedIdentifiers.add(
                            new RelatedIdentifier(
                                pmidRef.attr(EnaConstants.REF_ATTR_ID),
                                RelatedIdentifierType.PMID,
                                RelationType.IsReferencedBy));
                    }

                    break;

                case EnaConstants.REF_SUBMISSION:

                    // get submission date
                    try {
                        Date submissionDate = new Date(
                            refElement.children().select(EnaConstants.REF_SUBMISSION_DATE).get(0).text(),
                            DateType.Submitted);
                        dates.add(submissionDate);
                    } catch (NullPointerException e) { //NOPMD skip this date, if it does not exist or is malformed
                    }

                    break;
            }
        }

        // parse features
        Elements taxonElements = children.select(EnaConstants.TAXON);

        for (Element taxonElement : taxonElements) {

            String taxonName = taxonElement.attr(EnaConstants.TAX_SCIENTIFIC_NAME);
            // add taxon link
            String taxId = taxonElement.attr(EnaConstants.TAX_ID);

            if (!taxId.isEmpty()) {
                WebLink taxonLink =
                    new WebLink(String.format(EnaUrlConstants.TAXON_VIEW_URL, taxonElement.attr(EnaConstants.TAX_ID)));
                taxonLink.setName(EnaUrlConstants.TAXON_URL_NAME + taxonName);
                taxonLink.setType(WebLinkType.Related);
                links.add(taxonLink);
            }

            // add name and common name to subjects
            subjects.add(new Subject(taxonName));

            String commonName = taxonElement.attr(EnaConstants.TAX_COMMON_NAME);

            if (!commonName.isEmpty())
                subjects.add(new Subject(commonName));
        }

        document.addDates(dates);
        document.addRelatedIdentifiers(relatedIdentifiers);

        return document;
    }


    @Override
    public void clear()
    {
        // nothing to clean up
    }

}
