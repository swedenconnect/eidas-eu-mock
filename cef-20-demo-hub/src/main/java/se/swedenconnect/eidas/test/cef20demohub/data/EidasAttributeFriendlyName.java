package se.swedenconnect.eidas.test.cef20demohub.data;

public enum EidasAttributeFriendlyName {
    personIdentifier("PersonIdentifier"),
    familyName("FamilyName"),
    firstName("FirstName"),
    dateOfBirth("DateOfBirth"),
    birthName("BirthName"),
    placeOfBirth("PlaceOfBirth"),
    currentAddress("CurrentAddress"),
    gender("Gender"),
    legalPersonIdentifier("LegalPersonIdentifier"),
    legalName("LegalName"),
    legalAddress("LegalAddress"),
    vatRegistration("VATRegistration"),
    taxReference("TaxReference"),
    d2012_17_EUIdentifier("D-2012-17-EUIdentifier"),
    lei("LEI"),
    eori("EORI"),
    seed("SEED"),
    sic("SIC");

private String frendlyName;

    EidasAttributeFriendlyName(String frendlyName) {
        this.frendlyName = frendlyName;
    }

    public String getFrendlyName() {
        return frendlyName;
    }

    public String getFrendlyName(boolean representative) {
        return representative ? "Representative"+frendlyName : frendlyName;
    }
}
