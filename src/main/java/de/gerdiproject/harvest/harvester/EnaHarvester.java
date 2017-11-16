/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package de.gerdiproject.harvest.harvester;

import de.gerdiproject.harvest.EnaContextListener;
import de.gerdiproject.harvest.IDocument;
import de.gerdiproject.json.datacite.*;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.RelatedIdentifierType;
import de.gerdiproject.json.datacite.enums.RelationType;
import de.gerdiproject.json.datacite.enums.ResourceTypeGeneral;
import de.gerdiproject.json.datacite.extension.ResearchData;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A harvester for the ENA Database (https://www.ebi.ac.uk/ena/data/view/Taxon:Human,Taxon:Cat,Taxon:Mouse,Taxon:Zebrafish,Taxon:Bacillus%20Subtilis).
 *
 * @author Jan Frömberg
 */
public class EnaHarvester extends AbstractListHarvester<Element>
{
    private final static String BASE_URL = "https://www.ebi.ac.uk/ena/data/view/%s-%s&display=xml&header=true";

    private static final String PROVIDER = "European Nucleotide Archive (ENA)";
    //private static final String PROVIDER_URL = "https://www.ebi.ac.uk/ena";

    private static final List<String> FORMATS = Collections.unmodifiableList(Arrays.asList("XML", "FASTA", "TEXT"));
    private static final ResourceType RESOURCE_TYPE = createResourceType();

    private static final String VIEW_URL = "https://www.ebi.ac.uk/ena/data/view/%s";
    private static final String VIEW_URL_TEXT = "https://www.ebi.ac.uk/ena/data/view/%s&display=text";
    private static final String VIEW_URL_XML = "https://www.ebi.ac.uk/ena/data/view/%s&display=xml";
    private static final String VIEW_URL_FASTA = "https://www.ebi.ac.uk/ena/data/view/%s&display=fasta";

    private static final String DOWNLOAD_URL_TEXT = VIEW_URL_TEXT + "&download=txt&filename=%s.txt";
    private static final String DOWNLOAD_URL_XML = VIEW_URL_XML + "&download=xml&filename=%s.xml";
    private static final String DOWNLOAD_URL_FASTA = VIEW_URL_FASTA + "&download=fasta&filename=%s.fasta";

    private static final String VERSION_HISTORY_URL = "https://www.ebi.ac.uk/cgi-bin/sva/sva.pl?search=Go&amp;query=%s";
    private static final String TAXON_URL = "https://www.ebi.ac.uk/ena/data/view/Taxon:%s";

    private static final String THUMBNAIL_URL = "https://www.ebi.ac.uk/ena/data/view/graphics/%s&showSequence=false&featureRange=1-%s";
    private static final String LOGO_URL = "https://www.ebi.ac.uk/web_guidelines/images/logos/ena/ENA-logo.png";

    private static final String TITLE = "Sequence: %s.%s";
    private static final String SIZE_PREFIX = "Sequence length: ";

    private final SimpleDateFormat dateFormat;


    /**
     * Default Constructor. Sets language to "en" and version to "v1". Version
     * and language are essential parts of the URL.
     */
    public EnaHarvester()
    {
        // only one document is created per harvested entry
        super("ENA-Harvester", 1);

        dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd");
    }

    @Override
    public void setProperty(String key, String value)
    {
        super.setProperty(key, value);

        if (getProperty(EnaContextListener.PROPERTY_FROM ) != null 
        		&& getProperty(EnaContextListener.PROPERTY_TO ) != null 
        		&& (key.equals(EnaContextListener.PROPERTY_FROM) || key.equals(EnaContextListener.PROPERTY_TO)))
            init();
    }

    /**
     * Grap stuff from URL
     * @return A collection of elements
     */
    @Override
    protected Collection<Element> loadEntries()
    {
        String domainsUrl = String.format(BASE_URL, getProperty(EnaContextListener.PROPERTY_FROM), getProperty(EnaContextListener.PROPERTY_TO));
        //logger.info(domainsUrl);
        Document doc = httpRequester.getHtmlFromUrl(domainsUrl);
        //logger.info("Document null?: " + (doc == null));
        //logger.info(doc.html());
        //logger.info("found " + doc.select("entry").size() + " documents from " + domainsUrl);
        return doc.select("entry");
    }

    private static ResourceType createResourceType()
    {
        ResourceType resourceType = new ResourceType("Sequencing Data", ResourceTypeGeneral.Dataset);

        return resourceType;
    }

    /**
     * Harvest the ENA DB
     * create document for each entry-tag
     * example entry: <entry accession="BC003740" version="1" entryVersion="15" dataClass="STD" taxonomicDivision="MUS" moleculeType="mRNA" sequenceLength="2141" topology="linear"
     * firstPublic="2001-03-17" firstPublicRelease="67" lastUpdated="2008-09-24" lastUpdatedRelease="97">
     * 
     * @param entry
     * @return
     */
    @Override
    protected List<IDocument> harvestEntry(Element entry)
    {
        // get attributes
        Elements children = entry.children();
        Attributes attributes = entry.attributes();
        String version = attributes.get("version");
        String accession = attributes.get("accession");

        DataCiteJson document = new DataCiteJson();
        document.setVersion(version);
        document.setPublisher(PROVIDER);
        document.setFormats(FORMATS);
        document.setResourceType(RESOURCE_TYPE);

        // get size
        String sequenceLength = SIZE_PREFIX + attributes.get("sequenceLength");
        document.setSizes(Arrays.asList(sequenceLength));

        // get titles
        Title mainTitle = new Title(String.format(TITLE, accession, version));
        document.setTitles(Arrays.asList(mainTitle));

        // get source ; TODO: what to do?
        //Source source = new Source(String.format(VIEW_URL_XML, accession), PROVIDER);
        //source.setProviderURI(PROVIDER_URL);
        //document.setSources(source);

        List<AbstractDate> dates = new LinkedList<>();
        Calendar cal = Calendar.getInstance();

        // get publication date
        try {
        		cal.setTime(dateFormat.parse(attributes.get("firstPublic")));
            document.setPublicationYear((short) cal.get(Calendar.YEAR));

            Date publicationDate = new Date(attributes.get("firstPublic"), DateType.Available);
            dates.add(publicationDate);
        } catch (ParseException e) { //NOPMD do nothing. just do not add the date if it does not exist
        }

        Date updatedDate = new Date(attributes.get("lastUpdated"), DateType.Updated);
		dates.add(updatedDate);

        // get web links
        List<WebLink> links = new LinkedList<>();
        WebLink viewLink = new WebLink(String.format(VIEW_URL, accession));
        viewLink.setName("View website");
        viewLink.setType(WebLinkType.ViewURL);
        links.add(viewLink);

        WebLink viewLinkText = new WebLink(String.format(VIEW_URL_TEXT, accession));
        viewLinkText.setName("View plain text");
        viewLinkText.setType(WebLinkType.ViewURL);
        links.add(viewLinkText);

        WebLink viewLinkXml = new WebLink(String.format(VIEW_URL_XML, accession));
        viewLinkXml.setName("View XML");
        viewLinkXml.setType(WebLinkType.ViewURL);
        links.add(viewLinkXml);

        WebLink viewLinkFasta = new WebLink(String.format(VIEW_URL_FASTA, accession));
        viewLinkFasta.setName("View FASTA");
        viewLinkFasta.setType(WebLinkType.ViewURL);
        links.add(viewLinkFasta);

        WebLink versionHistoryLink = new WebLink(String.format(VERSION_HISTORY_URL, accession));
        versionHistoryLink.setName("Version History");
        versionHistoryLink.setType(WebLinkType.Related);
        links.add(versionHistoryLink);

        WebLink previewImage = new WebLink(String.format(THUMBNAIL_URL, accession, attributes.get("sequenceLength")));
        previewImage.setName("Overview");
        previewImage.setType(WebLinkType.ThumbnailURL);
        links.add(previewImage);

        WebLink logoLink = new WebLink(LOGO_URL);
        logoLink.setName("Logo");
        logoLink.setType(WebLinkType.ProviderLogoURL);
        links.add(logoLink);

        document.setWebLinks(links);

        // get downloads
        List<ResearchData> files = new LinkedList<>();

        ResearchData downloadLinkText = new ResearchData(String.format(DOWNLOAD_URL_TEXT, accession, accession), "TXT");
        files.add(downloadLinkText);

        ResearchData downloadLinkXml = new ResearchData(String.format(DOWNLOAD_URL_XML, accession, accession), "XML");
        files.add(downloadLinkXml);

        ResearchData downloadLinkFasta = new ResearchData(String.format(DOWNLOAD_URL_FASTA, accession, accession), "FASTA");
        files.add(downloadLinkFasta);

        document.setResearchDataList(files);

        // get descriptions
        List<Description> descriptions = new LinkedList<>();

        Elements descriptionElements = children.select("description");

        for (Element descElement : descriptionElements) {
            Description description = new Description(descElement.text(), DescriptionType.Abstract);
            descriptions.add(description);
        }

        Elements commentElements = children.select("comment");

        for (Element commentElement : commentElements) {
            Description description = new Description(commentElement.text(), DescriptionType.Other);
            descriptions.add(description);
        }

        document.setDescriptions(descriptions);

        // get keyword subjects
        List<Subject> subjects = new LinkedList<>();
        Elements keywordElements = children.select("keyword");

        for (Element keywordElement : keywordElements) {
            Subject subject = new Subject(keywordElement.text());
            subjects.add(subject);
        }

        // get attribute subjects
        subjects.add(new Subject(attributes.get("dataClass")));
        subjects.add(new Subject(attributes.get("taxonomicDivision")));
        subjects.add(new Subject(attributes.get("moleculeType")));

        document.setSubjects(subjects);

        List<RelatedIdentifier> relatedIdentifiers = new LinkedList<>();

        // parse references
        Elements referenceElements = children.select("reference");

        for (Element refElement : referenceElements) {
            String type = refElement.attr("type");

            switch (type) {
                default:
                    break;

                case "article":

                    // get DOIs
                    Elements doiRefs = refElement.getElementsByAttributeValue("db", "DOI");

                    for (Element doiRef : doiRefs) {
                        relatedIdentifiers.add(new RelatedIdentifier(
                                                   doiRef.attr("id"),
                                                   RelatedIdentifierType.DOI,
                                                   RelationType.IsReferencedBy));
                    }

                    // get PMIDs
                    Elements pmidRefs = refElement.getElementsByAttributeValue("db", "PUBMED");

                    for (Element pmidRef : pmidRefs) {
                        relatedIdentifiers.add(new RelatedIdentifier(
                                                   pmidRef.attr("id"),
                                                   RelatedIdentifierType.PMID,
                                                   RelationType.IsReferencedBy));
                    }

                    break;

                case "submission":

                    // get submission date
                    try {
                        Date submissionDate = new Date(
                            refElement.children().select("submissionDate").get(0).text(),
                            DateType.Submitted);
                        dates.add(submissionDate);
                    } catch (NullPointerException e) { //NOPMD skip this date, if it does not exist or is malformed
                    }

                    break;
            }
        }

        // parse features
        Elements taxonElements = children.select("taxon");

        for (Element taxonElement : taxonElements) {

            String taxonName = taxonElement.attr("scientificName");
            // add taxon link
            String taxId = taxonElement.attr("taxId");

            if (!taxId.isEmpty()) {
                WebLink taxonLink = new WebLink(String.format(TAXON_URL, taxonElement.attr("taxId")));
                taxonLink.setName("View Organism: " + taxonName);
                taxonLink.setType(WebLinkType.Related);
                links.add(taxonLink);
            }

            // add name and common name to subjects
            subjects.add(new Subject(taxonName));

            String commonName = taxonElement.attr("commonName");

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
}