package eu.eidas.node;

import javax.annotation.Nonnull;

public enum SpecificParameterNames {

    ATTRIBUTE_LIST("attrList"),

    STR_ATTR_LIST("strAttrList"),

    DATA("data"),

    DATA_URL("DataURL"),

    SIG_MODULE_CREATOR_URL("sigCreatorModuleURL"),

    SAML_TOKEN("samlToken"),

    IDP_URL("idpUrl"),

    IDP_SIGN_ASSERTION("idpSignAssertion"),

    COLLEAGUE_REQUEST("colleagueRequest"),

    SPECIFIC_RESPONSE("specificResponse"),

    RELAY_STATE("relayState"),

    RESPONSE_SIGN_ASSERTION("response.sign.assertions"),

    RESPONSE_ENCRYPT_ASSERTION("response.encryption.mandatory"),
    //
    ;

    /**
     * constant name.
     */
    @Nonnull
    private final transient String name;

    /**
     * Constructor
     *
     * @param name name of the bean
     */
    SpecificParameterNames(@Nonnull String nameValue) {
        name = nameValue;
    }

    @Nonnull
    @Override
    public String toString() {
        return name;

    }
}
