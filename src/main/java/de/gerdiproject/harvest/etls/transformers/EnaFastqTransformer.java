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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.EnaFastqVO;
import de.gerdiproject.harvest.utils.HtmlUtils;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Identifier;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.enums.DateType;
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
        final Document viewPage = vo.getViewPage();
        final String identifierString = HtmlUtils.getString(viewPage, EnaConstants.ID);
        final DataCiteJson document = new DataCiteJson(identifierString);

        // add all possible metadata to the document
        document.setIdentifier(new Identifier(identifierString));
        document.addTitles(getTitles(viewPage));

        document.addWebLinks(getWebLinkList(identifierString));
        document.addResearchData(getResearchData(vo));
        document.setPublisher(EnaConstants.PUBLISHER);
        document.addSubjects(EnaConstants.SUBJECT_FASTQ);

        final Date publicationDate = getPublicationDate(viewPage);
        final Date updateDate = getUpdateDate(viewPage);
        document.addDates(Arrays.asList(publicationDate, updateDate));

        if (publicationDate != null) {
            final int publicationYear = publicationDate.getValueAsDateTime().getYear();
            document.setPublicationYear(publicationYear);
        }

        return document;
    }

    private Date getPublicationDate(final Document viewPage)
    {
        final String dateString = HtmlUtils.getString(viewPage, EnaConstants.ENA_FIRST_PUBLIC);
        return dateString == null ? null : new Date(dateString, DateType.Available);
    }

    private Date getUpdateDate(final Document viewPage)
    {
        final String dateString = HtmlUtils.getString(viewPage, EnaConstants.ENA_LAST_UPDATE);
        return dateString == null ? null : new Date(dateString, DateType.Updated);
    }

    private List<Title> getTitles(final Document viewPage)
    {
        return HtmlUtils.getObjects(
                   viewPage,
                   EnaConstants.TITLE_FASTQ_FILE,
                   (Element ele) -> new Title(ele.text()));
    }

    private List<WebLink> getWebLinkList(final String identifierString)
    {
        final List<WebLink> webLinkList = new LinkedList<>();

        webLinkList.add(new WebLink(
                            String.format(EnaUrlConstants.VIEW_URL, identifierString),
                            EnaUrlConstants.VIEW_URL_FASTQ_NAME,
                            WebLinkType.ViewURL));

        webLinkList.add(EnaUrlConstants.LOGO_LINK);

        return webLinkList;
    }

    private List<ResearchData> getResearchData(final EnaFastqVO vo)
    {
        final List<ResearchData> files = new LinkedList<>();

        if (vo.getFileReport() != null) {
            final String[] fileReportElements = vo.getFileReport().split("\\s|;");

            // the index starts with 1, because the first element is a prefix
            for (int i = 1; i < fileReportElements.length; i++)
                files.add(new ResearchData("http://" + fileReportElements[i], EnaUrlConstants.DOWNLOAD_URL_FASTQ_NAME + i));
        }

        return files;
    }

    @Override
    public void clear()
    {
        // nothing to clean up
    }
}