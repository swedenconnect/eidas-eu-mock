/*
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */
package eu.eidas.config.samlmetadata;

import java.io.File;
import java.util.List;

import eu.eidas.config.ConfigurationException;

public interface MetadataRepository {
    List<String> getIDs() throws ConfigurationException;
    MetadataItem getMetadataItem(String id) throws ConfigurationException;
    void removeItem(String id) throws ConfigurationException;
    void addItemFromFile(File sourceFile, String newFileName) throws ConfigurationException;
}
