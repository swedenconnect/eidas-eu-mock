package eu.eidas.node;

/**
 * See "Effective Java edition 2 (Joshua Bloch - Addison Wesley 20012)" item 30
 */
public enum NodeViewNames {
    EIDAS_CONNECTOR_COLLEAGUE_REQUEST_REDIRECT("/internal/colleagueRequestRedirect.jsp"),
    EIDAS_CONNECTOR_REDIRECT("/internal/connectorRedirect.jsp"),
    INTERNAL_ERROR("/internalError.jsp"),
    INTERCEPTOR_ERROR("/interceptorError.jsp"),
    ERROR("/error.jsp"),
    PRESENT_ERROR("/presentError.jsp"),
    SERVLET_PATH_SERVICE_PROVIDER ( "/ServiceProvider"),
    SUBMIT_ERROR("/presentSamlResponseError.jsp"),
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
