package se.swedenconnect.eidas.test.cef20demohub.data;

public enum EidasLegalAttributeFriendlyName {
    legalPersonIdentifier("LegalPersonIdentifier", false, false),
    legalName("LegalName", false, false),
    legalAddress("LegalAddress", false, false),
    vatRegistration("VATRegistration", false, false),
    taxReference("TaxReference", false, false),
    d2012_17_EUIdentifier("D-2012-17-EUIdentifier", false, false),
    lei("LEI", false, false),
    eori("EORI", false, false),
    seed("SEED", false, false),
    sic("SIC", false, false);

    private String frendlyName;
    private boolean defRequested;
    private boolean defRequired;

    EidasLegalAttributeFriendlyName(String frendlyName, boolean defRequested, boolean defRequired) {
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
