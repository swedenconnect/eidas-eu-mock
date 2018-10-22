/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core;

import javax.annotation.Nonnull;

import eu.eidas.util.Preconditions;

/**
 * Defines which extensions to be processed in a saml message
 *
 * // TODO this is incorrect as it uses the STORK namespace for eIDAS
 */
@Deprecated
public enum SAMLExtensionFormat {

    STORK10("stork1",
            SAMLCore.STORK10_NS.getValue(), SAMLCore.STORK10_PREFIX.getValue(),
            SAMLCore.STORK10P_NS.getValue(), SAMLCore.STORK10P_PREFIX.getValue(),
            SAMLCore.STORK10_BASE_URI.getValue()),

    EIDAS10("eidas",
            SAMLCore.EIDAS10_NS.getValue(), SAMLCore.EIDAS10_PREFIX.getValue(),
            SAMLCore.EIDAS10_NS.getValue(), SAMLCore.EIDAS10_PREFIX.getValue(),
            SAMLCore.EIDAS10_BASE_URI.getValue());

    @Deprecated
    public static final String EIDAS_FORMAT_NAME = EIDAS10.getName();

    @Deprecated
    public static final String STORK1_FORMAT_NAME = STORK10.getName();

    /**
     * Check if the extension message format is STORK 1
     *
     * @param messageFormat the message format needs to be non null
     * @return true if STORK 1_0
     */
    public static boolean isEidasExtensionFormatName(@Nonnull String messageFormat) {
        return EIDAS_FORMAT_NAME.equalsIgnoreCase(messageFormat);
    }

    /**
     * Check if the extension message format is STORK 1
     *
     * @param messageFormat the message format needs to be non null
     * @return true if STORK 1_0
     */
    public static boolean isStork1ExtensionFormatName(@Nonnull String messageFormat) {
        return STORK1_FORMAT_NAME.equalsIgnoreCase(messageFormat);
    }

    @Nonnull
    private final transient String name;

    @Nonnull
    private final transient String assertionNS;

    @Nonnull
    private final transient String assertionPrefix;

    @Nonnull
    private final transient String protocolNS;

    @Nonnull
    private final transient String protocolPrefix;

    @Nonnull
    private final transient String baseURI;

    SAMLExtensionFormat(@Nonnull String formatName,
                        @Nonnull String assertionNS,
                        @Nonnull String assertionPrefix,
                        @Nonnull String protocolNS,
                        @Nonnull String protocolPrefix,
                        @Nonnull String baseURI) {
        Preconditions.checkNotNull(formatName, "formatName");
        Preconditions.checkNotNull(assertionNS, "assertionNS");
        Preconditions.checkNotNull(assertionPrefix, "assertionPrefix");
        Preconditions.checkNotNull(protocolNS, "protocolNS");
        Preconditions.checkNotNull(protocolPrefix, "protocolPrefix");
        Preconditions.checkNotNull(baseURI, "baseURI");
        name = formatName;
        this.assertionNS = assertionNS;
        this.assertionPrefix = assertionPrefix;
        this.protocolNS = protocolNS;
        this.protocolPrefix = protocolPrefix;
        this.baseURI = baseURI;
    }

    @Nonnull
    public String getAssertionNS() {
        return assertionNS;
    }

    @Nonnull
    public String getAssertionPrefix() {
        return assertionPrefix;
    }

    @Nonnull
    public String getBaseURI() {
        return baseURI;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getProtocolNS() {
        return protocolNS;
    }

    @Nonnull
    public String getProtocolPrefix() {
        return protocolPrefix;
    }

    @Override
    public String toString() {
        return "SAMLExtensionFormat{" +
                "name='" + name + '\'' +
                ", assertionNS='" + assertionNS + '\'' +
                ", assertionPrefix='" + assertionPrefix + '\'' +
                ", protocolNS='" + protocolNS + '\'' +
                ", protocolPrefix='" + protocolPrefix + '\'' +
                ", baseURI='" + baseURI + '\'' +
                '}';
    }
}
