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