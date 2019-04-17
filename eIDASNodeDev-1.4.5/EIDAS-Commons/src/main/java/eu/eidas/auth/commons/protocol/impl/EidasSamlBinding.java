package eu.eidas.auth.commons.protocol.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * SAML binding name.
 *
 * @since 1.1
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "LocalVariableHidesMemberVariable"})
public enum EidasSamlBinding {

    POST("POST", SamlBindingUri.SAML2_POST),

    REDIRECT("GET", SamlBindingUri.SAML2_REDIRECT),

    EMPTY("EMPTY", null);

    private static final EnumMapper<String, EidasSamlBinding> NAME_MAPPER =
            new EnumMapper<String, EidasSamlBinding>(new KeyAccessor<String, EidasSamlBinding>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EidasSamlBinding EidasSamlBinding) {
                    return EidasSamlBinding.getName();
                }
            }, Canonicalizers.trimUpperCase(), values());

    private static final EnumMapper<String, EidasSamlBinding> BINDING_URI_MAPPER =
            new EnumMapper<String, EidasSamlBinding>(new KeyAccessor<String, EidasSamlBinding>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EidasSamlBinding EidasSamlBinding) {
                    SamlBindingUri bindingUri = EidasSamlBinding.getBindingUri();
                    if (null == bindingUri) {
                        return Canonicalizers.NULL_KEY;
                    }
                    return bindingUri.getBindingUri();
                }
            }, Canonicalizers.trimLowerCaseWithNullKey(), values());

    public static EnumMapper<String, EidasSamlBinding> bindingUriMapper() {
        return BINDING_URI_MAPPER;
    }

    @Nullable
    public static EidasSamlBinding fromBindingUri(@Nullable String bindingUri) {
        if (null == bindingUri) {
            return EMPTY;
        }
        return BINDING_URI_MAPPER.fromKey(bindingUri);
    }

    @Nullable
    public static EidasSamlBinding fromName(@Nonnull String name) {
        return NAME_MAPPER.fromKey(name);
    }

    public static EnumMapper<String, EidasSamlBinding> nameMapper() {
        return NAME_MAPPER;
    }

    @Nullable
    public static String toBindingUri(@Nullable String name) {
        if (null == name) {
            return null;
        }
        EidasSamlBinding eidasSamlBinding = EidasSamlBinding.fromName(name);
        if (null == eidasSamlBinding) {
            return null;
        }
        SamlBindingUri bindingUri = eidasSamlBinding.getBindingUri();
        if (null != bindingUri) {
            return bindingUri.getBindingUri();
        }
        return null;
    }

    @Nullable
    public static String toName(@Nullable String bindingUri) {
        EidasSamlBinding eidasSamlBinding = EidasSamlBinding.fromBindingUri(bindingUri);
        if (null == eidasSamlBinding) {
            return null;
        }
        return eidasSamlBinding.getName();
    }

    @Nullable
    public static String toNameNotEmpty(@Nullable String bindingUri) {
        EidasSamlBinding eidasSamlBinding = EidasSamlBinding.fromBindingUri(bindingUri);
        if (null == eidasSamlBinding || eidasSamlBinding == EMPTY) {
            return null;
        }
        return eidasSamlBinding.getName();
    }

    @Nonnull
    private final transient String name;

    @Nullable
    private final transient SamlBindingUri bindingUri;

    EidasSamlBinding(@Nonnull String name, @Nullable SamlBindingUri bindingUri) {
        this.name = name;
        this.bindingUri = bindingUri;
    }

    @Nullable
    public SamlBindingUri getBindingUri() {
        return bindingUri;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
