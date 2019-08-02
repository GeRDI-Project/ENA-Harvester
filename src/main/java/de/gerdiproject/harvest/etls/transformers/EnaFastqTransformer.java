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


public class EnaFastqTransformer extends AbstractIteratorTransformer<EnaFastqVO, DataCiteJson>
{

    @Override
    protected DataCiteJson transformElement(EnaFastqVO vo) throws TransformerException
    {
        // create the document
        final DataCiteJson document = new DataCiteJson(String.valueOf(vo.getId()));

        // add all possible metadata to the document
        document.addTitles(getTitles(vo));
        document.addWebLinks(getWebLinkList(vo));
        document.addDates(getDates(vo));

        //added subject as FASTQ just for filtering
        List<Subject> subjects = new LinkedList<>();
        subjects.add(new Subject(EnaConstants.SUBJECT_FASTQ));
        document.addSubjects(subjects);

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

            String url = String.format(EnaUrlConstants.DOWNLOAD_URL_FASTQ, vo.getId());

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
            String Result1 = response.toString();
            //get only fastq files from result1
            String Result2 = Result1.substring(9);

            final String webLinks = Result2;

            if (webLinks != null)
                if (webLinks.contains(";")) {
                    String[] webLink1 = webLinks.split(";");

                    for (String webLink2 : webLink1)
                        webLinkList.add(new WebLink(webLink2, null, null));
                } else
                    webLinkList.add(new WebLink(webLinks));

            return webLinkList;
        } catch (IOException e) {  // skip this page
            return null;
        }
    }
    private List<AbstractDate> getDates(EnaFastqVO vo)
    {
        final List<AbstractDate> dates = new LinkedList<>();

        // retrieve the dates
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
                }

            }
        }


        return dates;

    }

    /**
     * Creates a unique identifier for a document from MyProject.
     *
     * @param source the source object that contains all metadata that is needed
     *
     * @return a unique identifier of this document
     */

}