package eu.eidas.node;

import javax.annotation.Nonnull;

public enum SpecificViewNames {

    IDP_RESPONSE("/IdpResponse"),

    EXTERNAL_SIG_MODULE_REDIRECT("/internal/sigCreatorModuleRedirect.jsp"),

    IDP_REDIRECT("/internal/idpRedirect.jsp"),

    COLLEAGUE_RESPONSE_REDIRECT("/internal/colleagueResponseRedirect.jsp"),

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
    SpecificViewNames(@Nonnull String nameValue) {
        name = nameValue;
    }

    @Nonnull
    @Override
    public String toString() {
        return name;

    }
}
