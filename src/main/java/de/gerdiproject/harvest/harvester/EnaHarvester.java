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
package de.gerdiproject.harvest.harvester;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.gerdiproject.harvest.IDocument;
import de.gerdiproject.harvest.ena.constants.ENAConstants;
import de.gerdiproject.harvest.ena.constants.ENAParameterConstants;
import de.gerdiproject.harvest.ena.constants.ENAUrlConstants;
import de.gerdiproject.harvest.utils.HashGenerator;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.RelatedIdentifier;
import de.gerdiproject.json.datacite.ResourceType;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.RelatedIdentifierType;
import de.gerdiproject.json.datacite.enums.RelationType;
import de.gerdiproject.json.datacite.enums.ResourceTypeGeneral;
import de.gerdiproject.json.datacite.extension.ResearchData;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;

/**
 * A harvester for the ENA Database There are parameters to setup a harvestable
 * range: accfrom and accto. See ENAParameterConstants
 * (https://www.ebi.ac.uk/ena/data/view/Taxon:Human,Taxon:Cat,Taxon:Mouse,Taxon:Zebrafish,Taxon:Bacillus%20Subtilis).
 * To harvest only house mouse taxon:
 * https://www.ebi.ac.uk/ena/data/view/Taxon:10090&portal=sequence_release&display=xml
 * usage hint: how many records are available for a single result set -> use the
 * "resultcount" parameter
 *
 * @author Jan Frömberg
 */
public class EnaHarvester extends AbstractListHarvester<Element>
{
    private static final ResourceType RESOURCE_TYPE = createResourceType();
    private final SimpleDateFormat dateFormat;


    /**
     * This is the constructor to initialize the Harvester.
     */
    public EnaHarvester()
    {
        // only one document is created per harvested entry
        super("ENA-Harvester", 1);

        dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd");
    }


    /**
     * Set the ENA ACCESSION-Range Properties (e.g. BC003740-BC093740) and check
     * if they are set. If not, run init() to reset the parameters.
     */
    @Override
    public void setProperty(String key, String value)
    {
        super.setProperty(key, value);

        if (getProperty(ENAParameterConstants.PROPERTY_FROM_KEY) != null
            && getProperty(ENAParameterConstants.PROPERTY_TO_KEY) != null
            && (key.equals(ENAParameterConstants.PROPERTY_FROM_KEY)
                || key.equals(ENAParameterConstants.PROPERTY_TO_KEY)))
            init();
    }


    /**
     * Grab ENA-DB-Entries from a parameterized (properties) domain URL (XML)
     *
     * @return A collection of elements
     */
    @Override
    protected Collection<Element> loadEntries()
    {
        String domainsUrl = String.format(
                                ENAUrlConstants.BASE_URL,
                                getProperty(ENAParameterConstants.PROPERTY_FROM_KEY),
                                getProperty(ENAParameterConstants.PROPERTY_TO_KEY));
        Document doc = httpRequester.getHtmlFromUrl(domainsUrl);

        return doc.select("entry");
    }


    /**
     * Function for creating the ENA Resource Type
     *
     * @return a Sequencing Data ResourceType for the ENA DB
     */
    private static ResourceType createResourceType()
    {
        ResourceType resourceType = new ResourceType(ENAConstants.SEQ_DATA, ResourceTypeGeneral.Dataset);

        return resourceType;
    }


    /**
     * Harvest the ENA DB This method creates a searchable
     * gerdi-json-datacite-document for each entry-element example entry:
     * <entry accession="BC003740" version="1" entryVersion="15" dataClass="STD"
     * taxonomicDivision="MUS" moleculeType="mRNA" sequenceLength="2141"
     * topology="linear" firstPublic="2001-03-17" firstPublicRelease="67"
     * lastUpdated="2008-09-24" lastUpdatedRelease="97">
     *
     * @param entry, An single entry-Element derived from the collection via
     *            loadEntries() with its sub-Elements
     * @return
     */
    @Override
    protected List<IDocument> harvestEntry(Element entry)
    {
        // get attributes
        Elements children = entry.children();
        Attributes attributes = entry.attributes();
        String version = attributes.get(ENAConstants.VERSION);
        String accession = attributes.get(ENAConstants.ACCESSION);

        DataCiteJson document = new DataCiteJson(accession);
        document.setVersion(version);
        document.setPublisher(ENAConstants.PROVIDER);
        document.setFormats(ENAConstants.FORMATS);
        document.setResourceType(RESOURCE_TYPE);
        document.setResearchDisciplines(ENAConstants.DISCIPLINES);

        // get size
        String sequenceLength = ENAConstants.SIZE_PREFIX + attributes.get(ENAConstants.SEQUENCE_LENGTH);
        document.setSizes(Arrays.asList(sequenceLength));

        // get titles
        Title mainTitle = new Title(String.format(ENAConstants.TITLE, accession, version));
        document.setTitles(Arrays.asList(mainTitle));

        // get source ; TODO: what to do? include it in a further release
        //Source source = new Source(String.format(VIEW_URL_XML, accession), PROVIDER);
        //source.setProviderURI(PROVIDER_URL);
        //document.setSources(source);

        List<AbstractDate> dates = new LinkedList<>();
        Calendar cal = Calendar.getInstance();

        // get publication date
        try {
            cal.setTime(dateFormat.parse(attributes.get(ENAConstants.FIRST_PUBLIC)));
            document.setPublicationYear((short) cal.get(Calendar.YEAR));

            Date publicationDate = new Date(attributes.get(ENAConstants.FIRST_PUBLIC), DateType.Available);
            dates.add(publicationDate);
        } catch (ParseException e) { //NOPMD do nothing. just do not add the date if it does not exist
        }

        Date updatedDate = new Date(attributes.get(ENAConstants.LAST_UPDATED), DateType.Updated);
        dates.add(updatedDate);

        // get web links
        List<WebLink> links = new LinkedList<>();
        WebLink viewLink = new WebLink(String.format(ENAUrlConstants.VIEW_URL, accession));
        viewLink.setName(ENAUrlConstants.VIEW_URL_NAME);
        viewLink.setType(WebLinkType.ViewURL);
        links.add(viewLink);

        WebLink viewLinkText = new WebLink(String.format(ENAUrlConstants.VIEW_URL_TEXT, accession));
        viewLinkText.setName(ENAUrlConstants.VIEW_URL_TXT_NAME);
        viewLinkText.setType(WebLinkType.ViewURL);
        links.add(viewLinkText);

        WebLink viewLinkXml = new WebLink(String.format(ENAUrlConstants.VIEW_URL_XML, accession));
        viewLinkXml.setName(ENAUrlConstants.VIEW_URL_XML_NAME);
        viewLinkXml.setType(WebLinkType.ViewURL);
        links.add(viewLinkXml);

        WebLink viewLinkFasta = new WebLink(String.format(ENAUrlConstants.VIEW_URL_FASTA, accession));
        viewLinkFasta.setName(ENAUrlConstants.VIEW_URL_FASTA_NAME);
        viewLinkFasta.setType(WebLinkType.ViewURL);
        links.add(viewLinkFasta);

        WebLink versionHistoryLink = new WebLink(String.format(ENAUrlConstants.VERSION_HISTORY_URL, accession));
        versionHistoryLink.setName(ENAUrlConstants.VERSION_HISTORY_URL_NAME);
        versionHistoryLink.setType(WebLinkType.Related);
        links.add(versionHistoryLink);

        WebLink previewImage = new WebLink(
            String.format(ENAUrlConstants.THUMBNAIL_URL, accession, attributes.get(ENAConstants.SEQUENCE_LENGTH)));
        previewImage.setName(ENAUrlConstants.PREVIEW_IMAGE_NAME);
        previewImage.setType(WebLinkType.ThumbnailURL);
        links.add(previewImage);

        WebLink logoLink = new WebLink(ENAUrlConstants.LOGO_URL);
        logoLink.setName(ENAUrlConstants.LOGO_URL_NAME);
        logoLink.setType(WebLinkType.ProviderLogoURL);
        links.add(logoLink);

        document.setWebLinks(links);

        // get downloads
        List<ResearchData> files = new LinkedList<>();

        ResearchData downloadLinkText = new ResearchData(
            String.format(ENAUrlConstants.DOWNLOAD_URL_TEXT, accession, accession),
            ENAConstants.TXT);
        files.add(downloadLinkText);

        ResearchData downloadLinkXml = new ResearchData(
            String.format(ENAUrlConstants.DOWNLOAD_URL_XML, accession, accession),
            ENAConstants.XML);
        files.add(downloadLinkXml);

        ResearchData downloadLinkFasta = new ResearchData(
            String.format(ENAUrlConstants.DOWNLOAD_URL_FASTA, accession, accession),
            ENAConstants.FASTA);
        files.add(downloadLinkFasta);

        document.setResearchDataList(files);

        // get descriptions
        List<Description> descriptions = new LinkedList<>();

        Elements descriptionElements = children.select(ENAConstants.DESCRIPTION);

        for (Element descElement : descriptionElements) {
            Description description = new Description(descElement.text(), DescriptionType.Abstract);
            descriptions.add(description);
        }

        Elements commentElements = children.select(ENAConstants.COMMENT);

        for (Element commentElement : commentElements) {
            Description description = new Description(commentElement.text(), DescriptionType.Other);
            descriptions.add(description);
        }

        document.setDescriptions(descriptions);

        // get keyword subjects
        List<Subject> subjects = new LinkedList<>();
        Elements keywordElements = children.select(ENAConstants.KEYWORD);

        for (Element keywordElement : keywordElements) {
            Subject subject = new Subject(keywordElement.text());
            subjects.add(subject);
        }

        // get attribute subjects
        subjects.add(new Subject(attributes.get(ENAConstants.DATACLASS)));
        subjects.add(new Subject(attributes.get(ENAConstants.TAX_DIVISION)));
        subjects.add(new Subject(attributes.get(ENAConstants.MOLECULETYPE)));

        document.setSubjects(subjects);

        List<RelatedIdentifier> relatedIdentifiers = new LinkedList<>();

        // parse references
        Elements referenceElements = children.select(ENAConstants.REFERENCE);

        for (Element refElement : referenceElements) {
            String type = refElement.attr(ENAConstants.REF_TYPE);

            switch (type) {
                default:
                    break;

                case ENAConstants.REF_ARTICLE:

                    // get DOIs
                    Elements doiRefs = refElement.getElementsByAttributeValue("db", "DOI");

                    for (Element doiRef : doiRefs) {
                        relatedIdentifiers.add(
                            new RelatedIdentifier(
                                doiRef.attr(ENAConstants.REF_ATTR_ID),
                                RelatedIdentifierType.DOI,
                                RelationType.IsReferencedBy));
                    }

                    // get PMIDs
                    Elements pmidRefs = refElement.getElementsByAttributeValue("db", "PUBMED");

                    for (Element pmidRef : pmidRefs) {
                        relatedIdentifiers.add(
                            new RelatedIdentifier(
                                pmidRef.attr(ENAConstants.REF_ATTR_ID),
                                RelatedIdentifierType.PMID,
                                RelationType.IsReferencedBy));
                    }

                    break;

                case ENAConstants.REF_SUBMISSION:

                    // get submission date
                    try {
                        Date submissionDate = new Date(
                            refElement.children().select(ENAConstants.REF_SUBMISSION_DATE).get(0).text(),
                            DateType.Submitted);
                        dates.add(submissionDate);
                    } catch (NullPointerException e) { //NOPMD skip this date, if it does not exist or is malformed
                    }

                    break;
            }
        }

        // parse features
        Elements taxonElements = children.select(ENAConstants.TAXON);

        for (Element taxonElement : taxonElements) {

            String taxonName = taxonElement.attr(ENAConstants.TAX_SCIENTIFIC_NAME);
            // add taxon link
            String taxId = taxonElement.attr(ENAConstants.TAX_ID);

            if (!taxId.isEmpty()) {
                WebLink taxonLink =
                    new WebLink(String.format(ENAUrlConstants.TAXON_URL, taxonElement.attr(ENAConstants.TAX_ID)));
                taxonLink.setName(ENAUrlConstants.TAXON_URL_NAME + taxonName);
                taxonLink.setType(WebLinkType.Related);
                links.add(taxonLink);
            }

            // add name and common name to subjects
            subjects.add(new Subject(taxonName));

            String commonName = taxonElement.attr(ENAConstants.TAX_COMMON_NAME);

            if (!commonName.isEmpty())
                subjects.add(new Subject(commonName));
        }

        // add dates if there are any
        if (!dates.isEmpty())
            document.setDates(dates);

        // add related identifiers if there are any
        if (!relatedIdentifiers.isEmpty())
            document.setRelatedIdentifiers(relatedIdentifiers);

        return Arrays.asList(document);
    }


    @Override
    protected String initHash() throws NoSuchAlgorithmException, NullPointerException
    {
        // concatenate all last update dates
        final StringBuilder updateDates = new StringBuilder();
        entries.forEach((Element entry) -> updateDates.append(entry.attr(ENAConstants.LAST_UPDATED)));

        return HashGenerator.instance().getShaHash(updateDates.toString());
    }


}
