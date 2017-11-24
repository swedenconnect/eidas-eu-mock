package eu.eidas.node;

/**
 * See "Effective Java edition 2 (Joshua Bloch - Addison Wesley 20012)" item 30
 */
public enum NodeViewNames {
    EIDAS_CONNECTOR_COLLEAGUE_REQUEST_REDIRECT("/internal/colleagueRequestRedirect.jsp"),
    EIDAS_CONNECTOR_COLLEAGUE_RESPONSE_REDIRECT("/internal/colleagueResponseRedirect.jsp"),
    EIDAS_CONNECTOR_COUNTRY_SELECTOR("/countrySelector.jsp"),
    EIDAS_CONNECTOR_PRESENT_CONSENT("/presentConsent.jsp"),
    EIDAS_CONNECTOR_REDIRECT("/internal/connectorRedirect.jsp"),
    EIDAS_SERVICE_CITIZEN_CONSENT("/citizenConsent.jsp"),
    EIDAS_SERVICE_IDP_REDIRECT("/internal/idpRedirect.jsp"),
    EIDAS_SERVICE_SIG_CREATOR_MODULE("/internal/sigCreatorModuleRedirect.jsp"),
    EIDAS_SERVICE_PRESENT_CONSENT("/presentConsent.jsp"),
    INTERNAL_ERROR("/internalError.jsp"),
    INTERCEPTOR_ERROR("/interceptorError.jsp"),
    ERROR("/error.jsp"),
    PRESENT_ERROR("/presentError.jsp"),
    MISSING_PARAMETER("/missingParameter.jsp"),
    EIDAS_SERVICE_NO_CONSENT("/CitizenConsent"),
    SERVLET_PATH_SERVICE_PROVIDER ( "/ServiceProvider"),
    SERVLET_PATH_BKU_ANMELDUNG( "/Bku-anmeldung"),
    SUBMIT_ERROR("/presentSamlResponseError.jsp"),
    EIDAS_CONNECTOR_COUNTRY_FRAMING("/countryFraming.jsp"),
    ;


    /**
     * constant name.
     */
    private String name;

    /**
     * Constructor
     * @param name name of the bean
     */
    NodeViewNames(final String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;

    }
}
