package eu.eidas.auth.commons.protocol.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * SAML binding URI.
 *
 * @since 1.1
 */
public enum SamlBindingUri {

    /**
     * URI for SAML 1 Artifact binding.
     */
    SAML1_ARTIFACT("urn:oasis:names:tc:SAML:1.0:profiles:artifact-01"),

    /**
     * URI for SAML 1 POST binding.
     */
    SAML1_POST("urn:oasis:names:tc:SAML:1.0:profiles:browser-post"),

    /**
     * URI for SAML 1 SOAP 1.1 binding.
     */
    SAML1_SOAP11("urn:oasis:names:tc:SAML:1.0:bindings:SOAP-binding"),

    /**
     * URI for SAML 2 Artifact binding.
     */
    SAML2_ARTIFACT("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact"),

    /**
     * URI for SAML 2 POST binding.
     */
    SAML2_POST("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"),

    /**
     * URI for SAML 2 POST-SimpleSign binding.
     */
    SAML2_POST_SIMPLE_SIGN("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST-SimpleSign"),

    /**
     * URI for SAML 2 HTTP redirect binding.
     */
    SAML2_REDIRECT("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"),

    /**
     * URI for SAML 2 SOAP binding.
     */
    SAML2_SOAP11("urn:oasis:names:tc:SAML:2.0:bindings:SOAP"),

    /**
     * URI for SAML 2 PAOS binding.
     */
    SAML2_PAOS("urn:oasis:names:tc:SAML:2.0:bindings:PAOS");

    private static final EnumMapper<String, SamlBindingUri> MAPPER =
            new EnumMapper<String, SamlBindingUri>(new KeyAccessor<String, SamlBindingUri>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull SamlBindingUri samlBindingUri) {
                    return samlBindingUri.getBindingUri();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static SamlBindingUri fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, SamlBindingUri> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String bindingUri;

    SamlBindingUri(@Nonnull String bindingUri) {
        this.bindingUri = bindingUri;
    }

    @Nonnull
    public String getBindingUri() {
        return bindingUri;
    }

    @Override
    public String toString() {
        return bindingUri;
    }
}
