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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.utils.HtmlUtils;
import de.gerdiproject.json.DateUtils;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.RelatedIdentifier;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.constants.DataCiteDateConstants;
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

    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        // nothing to retrieve from the ETL
    }


    @Override
    protected DataCiteJson transformElement(final Element entry) throws TransformerException
    {
        // get some shared attributes
        final String version = HtmlUtils.getAttribute(entry, EnaConstants.VERSION);
        final String accession = HtmlUtils.getAttribute(entry, EnaConstants.ACCESSION);
        final String sequenceLength = HtmlUtils.getAttribute(entry, EnaConstants.SEQUENCE_LENGTH);

        final DataCiteJson document = new DataCiteJson(accession);
        document.setVersion(version);
        document.setPublisher(EnaConstants.PROVIDER);
        document.addFormats(EnaConstants.FORMATS);
        document.setResourceType(EnaConstants.RESOURCE_TYPE);
        document.addResearchDisciplines(EnaConstants.DISCIPLINES);

        document.setPublicationYear(getPublicationYear(entry));
        document.addSizes(getSizes(sequenceLength));
        document.addTitles(getTitles(accession, version));
        document.addResearchData(getResearchData(accession));

        document.addDates(getDates(entry));
        document.addDates(HtmlUtils.getObjects(entry, EnaConstants.SUBMISSION_REF_SELECTION, this::parseSubmissionRef));

        document.addWebLinks(getWebLinks(accession, sequenceLength));
        document.addWebLinks(HtmlUtils.getObjects(entry, EnaConstants.TAXON, this::parseTaxonLink));

        document.addDescriptions(HtmlUtils.getObjects(entry, EnaConstants.DESCRIPTION, this::parseDescription));
        document.addDescriptions(HtmlUtils.getObjects(entry, EnaConstants.ENTRY_COMMENT, this::parseComment));

        document.addSubjects(HtmlUtils.getObjects(entry, EnaConstants.KEYWORD, this::parseKeyword));
        document.addSubjects(HtmlUtils.getObjects(entry, EnaConstants.TAXON, this::parseCommonTaxonName));
        document.addSubjects(HtmlUtils.getObjects(entry, EnaConstants.TAXON, this::parseScientificTaxonName));
        document.addSubjects(getSubjects(entry));

        document.addRelatedIdentifiers(HtmlUtils.getObjects(entry, EnaConstants.PUBMED_REF_SELECTION, this::parseMedPubRef));
        document.addRelatedIdentifiers(HtmlUtils.getObjects(entry, EnaConstants.DOI_REF_SELECTION, this::parseDoiRef));

        return document;
    }


    private Collection<Subject> getSubjects(final Element entry)
    {
        final List<Subject> subjects = new LinkedList<>();

        // get attribute subjects
        subjects.add(new Subject(HtmlUtils.getAttribute(entry, EnaConstants.DATACLASS)));
        subjects.add(new Subject(HtmlUtils.getAttribute(entry, EnaConstants.TAX_DIVISION)));
        subjects.add(new Subject(HtmlUtils.getAttribute(entry, EnaConstants.MOLECULETYPE)));

        return subjects;
    }


    private Collection<Title> getTitles(final String accession, final String version)
    {
        return Arrays.asList(
                   new Title(String.format(EnaConstants.TITLE, accession, version))
               );
    }


    private Collection<AbstractDate> getDates(final Element entry)
    {
        final List<AbstractDate> dates = new LinkedList<>();

        // get publication date
        dates.add(new Date(
                      HtmlUtils.getAttribute(entry, EnaConstants.FIRST_PUBLIC),
                      DateType.Available));

        // get update date
        dates.add(new Date(
                      HtmlUtils.getAttribute(entry, EnaConstants.LAST_UPDATED),
                      DateType.Updated));

        return dates;
    }


    private Integer getPublicationYear(final Element entry)
    {
        final Instant publicationDate =
            DateUtils.parseDate(HtmlUtils.getAttribute(entry, EnaConstants.FIRST_PUBLIC));

        Integer publicationYear;

        if (publicationDate == null)
            publicationYear = null;
        else {
            try {
                publicationYear = ZonedDateTime.ofInstant(publicationDate, DataCiteDateConstants.Z_ZONE_ID)
                                  .getYear();
            } catch (final DateTimeException e) {
                publicationYear = null;
            }
        }

        return publicationYear;
    }


    private Collection<String> getSizes(final String sequenceLength)
    {
        return Arrays.asList(EnaConstants.SIZE_PREFIX + sequenceLength);
    }


    private Collection<ResearchData> getResearchData(final String accession)
    {
        final List<ResearchData> files = new LinkedList<>();

        // downloadLink: Text
        files.add(new ResearchData(
                      String.format(EnaUrlConstants.DOWNLOAD_URL_TEXT, accession, accession),
                      EnaConstants.TXT));

        // downloadLink: Xml
        files.add(new ResearchData(
                      String.format(EnaUrlConstants.DOWNLOAD_URL_XML, accession, accession),
                      EnaConstants.XML));

        // downloadLink: Fasta
        files.add(new ResearchData(
                      String.format(EnaUrlConstants.DOWNLOAD_URL_FASTA, accession, accession),
                      EnaConstants.FASTA));

        return files;
    }


    private Collection<WebLink> getWebLinks(final String accession, final String sequenceLength)
    {
        final List<WebLink> links = new LinkedList<>();

        links.add(new WebLink(
                      String.format(EnaUrlConstants.VIEW_URL, accession),
                      EnaUrlConstants.VIEW_URL_NAME,
                      WebLinkType.ViewURL));

        links.add(new WebLink(
                      String.format(EnaUrlConstants.VIEW_URL_TEXT, accession),
                      EnaUrlConstants.VIEW_URL_TXT_NAME,
                      WebLinkType.ViewURL));

        links.add(new WebLink(
                      String.format(EnaUrlConstants.VIEW_URL_XML, accession),
                      EnaUrlConstants.VIEW_URL_XML_NAME,
                      WebLinkType.ViewURL));

        links.add(new WebLink(
                      String.format(EnaUrlConstants.VIEW_URL_FASTA, accession),
                      EnaUrlConstants.VIEW_URL_FASTA_NAME,
                      WebLinkType.ViewURL));

        links.add(new WebLink(
                      String.format(EnaUrlConstants.VERSION_HISTORY_URL, accession),
                      EnaUrlConstants.VERSION_HISTORY_URL_NAME,
                      WebLinkType.Related));

        links.add(new WebLink(
                      String.format(EnaUrlConstants.THUMBNAIL_URL, accession, sequenceLength),
                      EnaUrlConstants.PREVIEW_IMAGE_NAME,
                      WebLinkType.ThumbnailURL));

        links.add(EnaUrlConstants.LOGO_LINK);

        return links;
    }


    private Date parseSubmissionRef(final Element ele)
    {
        final String dateString = HtmlUtils.getString(ele, EnaConstants.REF_SUBMISSION_DATE);
        final String comment = HtmlUtils.getString(ele, EnaConstants.REF_SUBMISSION_COMMENT);

        final Date submissionDate = new Date(dateString, DateType.Submitted);
        submissionDate.setDateInformation(comment);

        return submissionDate;
    }


    private Description parseDescription(final Element ele)
    {
        return new Description(ele.text(), DescriptionType.Abstract);
    }


    private Description parseComment(final Element ele)
    {
        return new Description(ele.text(), DescriptionType.Other);
    }


    private Subject parseCommonTaxonName(final Element element)
    {
        final String commonName = HtmlUtils.getAttribute(element, EnaConstants.TAX_COMMON_NAME);
        return commonName == null ? null : new Subject(commonName);
    }


    private Subject parseScientificTaxonName(final Element element)
    {
        final String scientificName = HtmlUtils.getAttribute(element, EnaConstants.TAX_SCIENTIFIC_NAME);
        return scientificName == null ? null : new Subject(scientificName);
    }


    private Subject parseKeyword(final Element element)
    {
        final String keyword = element.text();
        return keyword.isEmpty() ? null : new Subject(keyword);
    }


    private RelatedIdentifier parseMedPubRef(final Element ele)
    {
        return new RelatedIdentifier(
                   ele.attr(EnaConstants.REF_ATTR_ID),
                   RelatedIdentifierType.PMID,
                   RelationType.IsReferencedBy);
    }


    private RelatedIdentifier parseDoiRef(final Element ele)
    {
        return new RelatedIdentifier(
                   ele.attr(EnaConstants.REF_ATTR_ID),
                   RelatedIdentifierType.DOI,
                   RelationType.IsReferencedBy);
    }


    private WebLink parseTaxonLink(final Element ele)
    {
        final String taxonName = HtmlUtils.getAttribute(ele, EnaConstants.TAX_SCIENTIFIC_NAME);
        final String taxonId = HtmlUtils.getAttribute(ele, EnaConstants.TAX_ID);

        return taxonId == null
               ? null
               : new WebLink(
                   String.format(EnaUrlConstants.TAXON_VIEW_URL, taxonId),
                   EnaUrlConstants.TAXON_URL_NAME + taxonName,
                   WebLinkType.Related);
    }


    @Override
    public void clear()
    {
        // nothing to clean up
    }

}
