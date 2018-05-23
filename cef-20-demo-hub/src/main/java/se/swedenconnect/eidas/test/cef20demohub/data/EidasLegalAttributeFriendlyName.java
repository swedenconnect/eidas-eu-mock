package se.swedenconnect.eidas.test.cef20demohub.data;

public enum EidasLegalAttributeFriendlyName {
    legalPersonIdentifier("LegalPersonIdentifier", false, false, true),
    legalName("LegalName", false, false, true),
    legalAddress("LegalAddress", false, false, false),
    vatRegistration("VATRegistration", false, false, false),
    taxReference("TaxReference", false, false, false),
    d2012_17_EUIdentifier("D-2012-17-EUIdentifier", false, false, false),
    lei("LEI", false, false, false),
    eori("EORI", false, false, false),
    seed("SEED", false, false, false),
    sic("SIC", false, false, false);

    private String friendlyName;
    private boolean defRequested;
    private boolean defRequired;
    private boolean mandatory;

    EidasLegalAttributeFriendlyName(String friendlyName, boolean defRequested, boolean defRequired, boolean mandatory) {
        this.friendlyName = friendlyName;
        this.defRequested = defRequested;
        this.defRequired = defRequired;
        this.mandatory = mandatory;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getFrendlyName(boolean representative) {
        return representative ? "Representative" + friendlyName : friendlyName;
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
