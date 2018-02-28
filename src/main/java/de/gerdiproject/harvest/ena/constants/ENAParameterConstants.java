/*
 * Copyright 2018 Jan Frömberg <jan.froemberg@tu-dresden.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gerdiproject.harvest.ena.constants;

/**
 * A static collection of constant parameters for configuring the ENA harvester.
 *
 * @author Jan Frömberg
 */
public class ENAParameterConstants
{
    // KEYS
    public static final String PROPERTY_FROM_KEY = "accfrom";
    public static final String PROPERTY_TO_KEY = "accto";

    // DEFAULT VALUES
    public static final String ENTRY_DEFAULT_FROM = "BC003738";
    public static final String ENTRY_DEFAULT_TO = "BC004738";

    /**
     * Private Constructor, because this is a static class.
     */
    private ENAParameterConstants()
    {
    }
}
