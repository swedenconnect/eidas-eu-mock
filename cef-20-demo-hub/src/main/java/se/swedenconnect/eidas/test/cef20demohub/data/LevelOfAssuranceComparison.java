package se.swedenconnect.eidas.test.cef20demohub.data;

public enum LevelOfAssuranceComparison {

    MINIMUM("minimum"),
    EXACT("exact");

    private String value;

    LevelOfAssuranceComparison(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
