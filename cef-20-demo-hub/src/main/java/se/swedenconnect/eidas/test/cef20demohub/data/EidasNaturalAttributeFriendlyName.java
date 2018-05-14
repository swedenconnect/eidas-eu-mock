package se.swedenconnect.eidas.test.cef20demohub.data;

public enum EidasNaturalAttributeFriendlyName {
    personIdentifier("PersonIdentifier", true, true),
    familyName("FamilyName", true, true),
    firstName("FirstName", true, true),
    dateOfBirth("DateOfBirth", true, true),
    birthName("BirthName", true, false),
    placeOfBirth("PlaceOfBirth", true, false),
    currentAddress("CurrentAddress", false, false),
    gender("Gender", true, false);

    private String frendlyName;
    private boolean defRequested;
    private boolean defRequired;

    EidasNaturalAttributeFriendlyName(String frendlyName, boolean defRequested, boolean defRequired) {
        this.frendlyName = frendlyName;
        this.defRequested = defRequested;
        this.defRequired = defRequired;
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
}
