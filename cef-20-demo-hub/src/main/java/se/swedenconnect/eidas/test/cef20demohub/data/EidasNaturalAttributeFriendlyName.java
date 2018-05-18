package se.swedenconnect.eidas.test.cef20demohub.data;

public enum EidasNaturalAttributeFriendlyName {
    personIdentifier("PersonIdentifier", true, true, true),
    familyName("FamilyName", true, true, true),
    firstName("FirstName", true, true, true),
    dateOfBirth("DateOfBirth", true, true, true),
    birthName("BirthName", true, false, false),
    placeOfBirth("PlaceOfBirth", true, false, false),
    currentAddress("CurrentAddress", false, false, false),
    gender("Gender", true, false, false);

    private String frendlyName;
    private boolean defRequested;
    private boolean defRequired;
    private boolean mandatory;

    EidasNaturalAttributeFriendlyName(String frendlyName, boolean defRequested, boolean defRequired, boolean mandatory) {
        this.frendlyName = frendlyName;
        this.defRequested = defRequested;
        this.defRequired = defRequired;
        this.mandatory = mandatory;
    }

    public String getFrendlyName() {
        return frendlyName;
    }

    public String getFrendlyName(boolean representative) {
        return representative ? "Representative" + frendlyName : frendlyName;
    }

    public boolean isDefRequested() {
        return defRequested;
    }

    public boolean isDefRequired() {
        return defRequired;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
