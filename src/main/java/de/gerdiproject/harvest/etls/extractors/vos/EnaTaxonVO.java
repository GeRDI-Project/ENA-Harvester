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

import java.util.List;

import org.jsoup.nodes.Element;

import lombok.Value;

/**
 * This class is a value object that contains all extracted Taxon (meta-) data from
 * ENA.
 *
 * @author Robin Weiss
 */
@Value
public class EnaTaxonVO
{
    private Element xml;
    private List<EnaReferenceVO> references;


    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final EnaTaxonVO other = (EnaTaxonVO) obj;

        if (references == null) {
            if (other.references != null)
                return false;
        } else if (!references.equals(other.references))
            return false;

        if (xml == null) {
            if (other.xml != null)
                return false;
        } else if (!xml.hasSameValue(other.xml))
            return false;

        return true;
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((references == null) ? 0 : references.hashCode());
        result = prime * result + ((xml == null) ? 0 : xml.hashCode());
        return result;
    }



}