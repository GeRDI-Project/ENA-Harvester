/**
 * Copyright © 2019 Robin Weiss (http://www.gerdi-project.de)
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
package de.gerdiproject.harvest;

import java.io.File;

import de.gerdiproject.harvest.application.AbstractContextListenerTest;
import de.gerdiproject.harvest.application.ContextListenerTestWrapper;
import de.gerdiproject.harvest.application.MainContextUtils;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.EnaAccessionETL;
import de.gerdiproject.harvest.utils.data.constants.DataOperationConstants;
import de.gerdiproject.harvest.utils.file.FileUtils;

/**
 * This class provides Unit Tests for the {@linkplain EnaContextListener}.
 *
 * @author Robin Weiss
 */
public class EnaContextListenerTest extends AbstractContextListenerTest<EnaContextListener>
{
    @Override
    protected EnaContextListener setUpTestObjects()
    {
        final EnaContextListener contextListener = super.setUpTestObjects();

        // set up mocked HTTP responses
        final File httpResourceFolder = getResource("mockedHttpResponses");

        if (httpResourceFolder != null) {
            final File httpCacheFolder = new File(
                MainContextUtils.getCacheDirectory(getClass()),
                DataOperationConstants.CACHE_FOLDER_PATH);

            FileUtils.copyFile(httpResourceFolder, httpCacheFolder);
        }

        // set up config
        final File configFileResource = getResource("config.json");
        final ContextListenerTestWrapper<? extends AbstractIteratorETL<?, ?>> contextInitializer =
            new ContextListenerTestWrapper<>(contextListener, () -> new EnaAccessionETL());

        final File configFile = contextInitializer.getConfigFile();
        FileUtils.copyFile(configFileResource, configFile);

        return contextListener;
    }
}
