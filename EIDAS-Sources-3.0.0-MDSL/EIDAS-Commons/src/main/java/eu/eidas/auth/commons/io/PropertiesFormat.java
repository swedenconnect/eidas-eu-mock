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
package eu.eidas.auth.commons.io;

import java.io.File;
import java.util.Locale;

import javax.annotation.Nonnull;

import eu.eidas.util.Preconditions;

/**
 * Properties Format
 *
 * @since 1.1
 */
public enum PropertiesFormat {

    PROPERTIES(".properties"),

    XML(".xml");

    @Nonnull
    public static PropertiesFormat getFormat(@Nonnull File file) {
        Preconditions.checkNotNull(file, "file");
        return getFormat(file.getName());
    }

    @Nonnull
    public static PropertiesFormat getFormat(@Nonnull String fileName) {
        Preconditions.checkNotNull(fileName, "fileName");
        String lowerCaseFileName = fileName.toLowerCase(Locale.ENGLISH);
        if (lowerCaseFileName.endsWith(PROPERTIES.getExtension())) {
            return PROPERTIES;
        } else if (lowerCaseFileName.endsWith(XML.getExtension())) {
            return XML;
        } else {
            throw new IllegalArgumentException("Unknown properties extension format: \"" + fileName + "\"");
        }
    }

    @Nonnull
    private final transient String extension;

    PropertiesFormat(@Nonnull String ext) {
        extension = ext;
    }

    @Nonnull
    public String getExtension() {
        return extension;
    }
}