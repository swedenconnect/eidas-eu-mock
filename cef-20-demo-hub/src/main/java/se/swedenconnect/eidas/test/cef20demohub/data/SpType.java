package se.swedenconnect.eidas.test.cef20demohub.data;

/**
 * Supported SpType as per the eIDAS specification.
 */
public enum SpType {

    PUBLIC("public"),
    PRIVATE("private");

    private String value;

    SpType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
