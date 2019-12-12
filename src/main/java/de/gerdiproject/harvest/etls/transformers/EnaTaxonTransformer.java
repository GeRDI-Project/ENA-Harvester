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
package de.gerdiproject.harvest.etls.transformers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaTaxonConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.vos.EnaReferenceVO;
import de.gerdiproject.harvest.etls.extractors.vos.EnaTaxonVO;
import de.gerdiproject.harvest.utils.HtmlUtils;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.RelatedIdentifier;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.RelatedIdentifierType;
import de.gerdiproject.json.datacite.enums.RelationType;
import de.gerdiproject.json.datacite.enums.TitleType;
import de.gerdiproject.json.datacite.extension.generic.ResearchData;
import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;

/**
 * Transforms ENA taxon metadata to {@linkplain DataCiteJson} objects.
 *
 * @author Robin Weiss
 */
public class EnaTaxonTransformer extends AbstractIteratorTransformer<EnaTaxonVO, DataCiteJson>
{
    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        // nothing to retrieve from the ETL
    }


    @Override
    protected DataCiteJson transformElement(final EnaTaxonVO vo) throws TransformerException
    {
        final Element taxon = vo.getXml().selectFirst(EnaTaxonConstants.TAXON_ELEMENT);
        final String taxId = HtmlUtils.getAttribute(taxon, EnaTaxonConstants.TAXON_ID_ATTRIBUTE);

        // get some shared attributes

        final DataCiteJson document = new DataCiteJson(String.format(EnaTaxonConstants.ID_TITLE, taxId));
        document.setPublisher(EnaConstants.PUBLISHER);
        document.addFormats(EnaTaxonConstants.FORMATS);
        document.setResourceType(EnaConstants.RESOURCE_TYPE);
        document.addResearchDisciplines(EnaConstants.DISCIPLINES);

        document.addTitles(getTitles(taxId, taxon));
        document.addWebLinks(getWebLinks(taxId));
        document.addSubjects(getSubjects(taxId, taxon));
        document.addResearchData(getResearchData(taxId));
        document.addDescriptions(getDescriptions(taxon));
        document.addRelatedIdentifiers(getRelatedIdentifiers(vo.getReferences()));

        return document;
    }


    private List<RelatedIdentifier> getRelatedIdentifiers(final List<EnaReferenceVO> references)
    {
        if (references.isEmpty())
            return null;

        final List<RelatedIdentifier> relatedIdentifierList = new LinkedList<>();

        for (final EnaReferenceVO ref : references) {
            final RelationType relationType = ref.getSource().equals(EnaTaxonConstants.CITATION_REF_TYPE)
                                              ? RelationType.IsCitedBy
                                              : RelationType.IsReferencedBy;

            if (ref.getDoi() != null) { // NOPMD != null is intended
                relatedIdentifierList.add(
                    new RelatedIdentifier(
                        ref.getDoi(),
                        RelatedIdentifierType.DOI,
                        relationType));

            } else if (ref.getReference() != null) { // NOPMD != null is intended
                relatedIdentifierList.add(
                    new RelatedIdentifier(
                        ref.getReference(),
                        RelatedIdentifierType.Handle,
                        relationType));

            } else if (ref.getSourceUrl() != null) { // NOPMD != null is intended
                relatedIdentifierList.add(
                    new RelatedIdentifier(
                        ref.getSourceUrl(),
                        RelatedIdentifierType.URL,
                        relationType));
            }
        }

        return relatedIdentifierList;
    }


    private List<Description> getDescriptions(final Element taxon)
    {
        final String name = HtmlUtils.getAttribute(taxon, EnaTaxonConstants.SCIENTIFIC_NAME_ATTRIBUTE);
        final String rank = HtmlUtils.getAttribute(taxon, EnaTaxonConstants.RANK_ATTRIBUTE);

        if (rank == null || name == null)
            return null;

        return Arrays.asList(
                   new Description(
                       String.format(EnaTaxonConstants.DESCRIPTION, name, rank),
                       DescriptionType.Abstract));
    }


    private List<ResearchData> getResearchData(final String taxId)
    {
        return Arrays.asList(new ResearchData(
                                 String.format(EnaTaxonConstants.DOWNLOAD_XML_URL, taxId),
                                 EnaConstants.XML));
    }


    private List<Subject> getSubjects(final String taxId, final Element taxon)
    {
        final List<Subject> subjectList = new LinkedList<>();

        subjectList.add(new Subject(taxId));

        final String scientificName = HtmlUtils.getAttribute(taxon, EnaTaxonConstants.SCIENTIFIC_NAME_ATTRIBUTE);

        if (scientificName != null)
            subjectList.add(new Subject(scientificName));

        final String taxonomicDivision = HtmlUtils.getAttribute(taxon, EnaTaxonConstants.TAX_DIVISION_ATTRIBUTE);

        if (taxonomicDivision != null)
            subjectList.add(new Subject(taxonomicDivision));

        final String rank = HtmlUtils.getAttribute(taxon, EnaTaxonConstants.RANK_ATTRIBUTE);

        if (rank != null)
            subjectList.add(new Subject(rank));

        // add common names
        for (final Element synonym : taxon.select(EnaTaxonConstants.SYNONYM_ELEMENT)) {
            final String commonName = HtmlUtils.getAttribute(synonym, EnaTaxonConstants.NAME_ATTRIBUTE);
            subjectList.add(new Subject(commonName));
        }

        return subjectList;
    }


    private List<WebLink> getWebLinks(final String taxId)
    {
        final List<WebLink> weblinkList = new LinkedList<>();

        weblinkList.add(new WebLink(
                            String.format(EnaTaxonConstants.VIEW_URL, taxId),
                            EnaUrlConstants.VIEW_URL_NAME,
                            WebLinkType.ViewURL));

        weblinkList.add(new WebLink(
                            String.format(EnaTaxonConstants.XML_URL, taxId),
                            EnaUrlConstants.VIEW_URL_XML_NAME,
                            WebLinkType.SourceURL));

        weblinkList.add(EnaUrlConstants.LOGO_LINK);

        return weblinkList;
    }


    private List<Title> getTitles(final String taxId, final Element taxon)
    {
        final List<Title> titleList = new LinkedList<>();

        // add main title
        titleList.add(new Title(String.format(EnaTaxonConstants.ID_TITLE, taxId)));

        // add common names as alternative titles
        for (final Element synonym : taxon.select(EnaTaxonConstants.SYNONYM_ELEMENT)) {
            final String alternativeName = HtmlUtils.getAttribute(synonym, EnaTaxonConstants.NAME_ATTRIBUTE);
            titleList.add(new Title(alternativeName, TitleType.AlternativeTitle, EnaTaxonConstants.NAME_LANGUAGE));
        }

        return titleList;
    }


    @Override
    public void clear()
    {
        // nothing to clean up
    }
}
