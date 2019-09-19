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
package de.gerdiproject.harvest.etls;

import de.gerdiproject.harvest.ena.constants.EnaConstants;
import de.gerdiproject.harvest.etls.extractors.EnaFastqExtractor;
import de.gerdiproject.harvest.etls.extractors.EnaFastqVO;
import de.gerdiproject.harvest.etls.transformers.EnaFastqTransformer;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * An ETL for harvesting ENA FASTQ data
 * Identify which INSDC partner accepted the original submission by looking at the first letter: ‘E’ for ENA, ‘D’ for DDBJ, or ‘S’ for NCBI<br>
 * Example : https://www.ebi.ac.uk/ena/data/view/ERR000001&display=xml
 *           https://www.ebi.ac.uk/ena/data/view/DRR000001&display=xml
 *           https://www.ebi.ac.uk/ena/data/view/SRR000001&display=xml<br>
 *
 * @author Komal Ahir, Jan Frömberg
 */

public class EnaFastqETL extends StaticIteratorETL<EnaFastqVO, DataCiteJson>
{
    /**
     * Constructor
     * @param enaEDS character to extract fastq data for different ETLs with char 'E', 'D', 'S'
     */
    public EnaFastqETL(final char enaEDS)
    {
        super(String.format(EnaConstants.FASTQ_ETL_NAME, enaEDS),
              new EnaFastqExtractor(enaEDS),
              new EnaFastqTransformer());
    }
}


