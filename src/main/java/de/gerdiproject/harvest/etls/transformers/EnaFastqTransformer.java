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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.ena.constants.EnaUrlConstants;
import de.gerdiproject.harvest.etls.extractors.EnaFastqVO;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;
import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;


public class EnaFastqTransformer extends AbstractIteratorTransformer<EnaFastqVO, DataCiteJson>
{
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    protected DataCiteJson transformElement(EnaFastqVO vo) throws TransformerException
    {
        // create the document
        final DataCiteJson document = new DataCiteJson(String.valueOf(vo.getId()));

        // add all possible metadata to the document
        document.addTitles(getTitles(vo));
        document.addWebLinks(getWebLinkList(vo));
        document.setPublisher(EnaConstants.PROVIDER);
        document.addSubjects(getSubjects(vo));

        // get publication year and Dates
        Calendar cal = Calendar.getInstance();

        final List<AbstractDate> dates = new LinkedList<>();

        final Elements attributes = vo.getViewPage().select(EnaConstants.RUN_ATTRIBUTE);

        for (Element attribute : attributes) {

            Elements tags = attribute.children();

            for (Element tagElement : tags) {

                String node = tagElement.text();

                if (node.contains(EnaConstants.ENA_LAST_UPDATE)) {
                    Date lastUpdated = new Date(attribute.text(), DateType.Updated);
                    dates.add(lastUpdated);
                }

                if (node.contains(EnaConstants.ENA_FIRST_PUBLIC)) {

                    Date firstPublic = new Date(attribute.text(), DateType.Available);
                    dates.add(firstPublic);

                    try {
                        String tagValue = attribute.text();
                        String publicationDate = tagValue.substring(17);
                        cal.setTime(dateFormat.parse(publicationDate));
                        document.setPublicationYear(cal.get(Calendar.YEAR));

                    } catch (ParseException e) { //do nothing. just do not add the publication year if it does not exist
                        return null;
                    }
                }

            }
        }

        document.addDates(dates);
        return document;
    }




    private List<Title> getTitles(EnaFastqVO vo)
    {
        final List<Title> titleLists = new LinkedList<>();

        // get the title
        final Elements titles = vo.getViewPage().select(EnaConstants.TITLE_FASTQ_FILE);

        // verify that there is data
        for (Element title : titles) {
            Title titleList = new Title(title.text());
            titleLists.add(titleList);
        }

        return titleLists;
    }


    private List<WebLink> getWebLinkList(EnaFastqVO vo) throws TransformerException
    {
        final List<WebLink> webLinkList = new LinkedList<>();

        try {
            final String url = String.format(EnaUrlConstants.DOWNLOAD_URL_FASTQ, vo.getId());
            final Elements identifiers = vo.getViewPage().select(EnaConstants.ALTERNATE_ID);

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", EnaConstants.USER_AGENT);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            //get result
            String Result = response.toString();

            //get only fastq files from result1
            final String webLinks = Result.substring(9);

            //fetch only weblinks for valid id
            for (Element identifier : identifiers) {
                String node = identifier.text();

                if (node.contains(EnaConstants.ERR_ID)) {
                    if (webLinks.contains(";")) {
                        String[] webLink1 = webLinks.split(";");

                        for (String webLink2 : webLink1)
                            webLinkList.add(new WebLink("http://" + webLink2, EnaUrlConstants.VIEW_URL_FASTQ_NAME, WebLinkType.Related));

                    } else
                        webLinkList.add(new WebLink("http://" + webLinks, EnaUrlConstants.VIEW_URL_FASTQ_NAME, WebLinkType.Related));
                }
            }


        } catch (IOException e) {  // skip this page
            return null;
        }

        return webLinkList;

    }


    private List<Subject> getSubjects(EnaFastqVO vo)
    {

        final List<Subject> subjects = new LinkedList<>();

        final Elements identifiers = vo.getViewPage().select(EnaConstants.ALTERNATE_ID);

        for (Element identifier : identifiers) {
            String node = identifier.text();

            if (node.contains(EnaConstants.ERR_ID)) {
                Subject identifierList = new Subject(identifier.text(), null);
                subjects.add(identifierList);
                subjects.add(new Subject(EnaConstants.SUBJECT_FASTQ, null));

            }
        }

        return subjects;
    }




    /**
     * Creates a unique identifier for a document from MyProject.
     *
     * @param source the source object that contains all metadata that is needed
     *
     * @return a unique identifier of this document
     */

}