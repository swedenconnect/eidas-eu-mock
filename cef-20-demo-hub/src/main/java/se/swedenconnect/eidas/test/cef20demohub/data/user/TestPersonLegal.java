package se.swedenconnect.eidas.test.cef20demohub.data.user;

import lombok.Getter;

@Getter
public enum TestPersonLegal {

    testOrg01("125457487Company",
            "Organization Name",
            "VAT Registration Number",
            "Tax Reference Number",
            "Directive 2012/17/EU Identifier",
            "Legal Entity Identifier(LEI)",
            "Economic Operator Registration and Identification (EORI)",
            "System for Exchange of Excise Data(SEED)",
            "Standard Industrial Classification (SIC)",
            TestAddress.address01),
    testOrg02("98765432-234234",
            "Example Inc",
            "EU554323234234301",
            "TIC-2342342234",
            "2012/17/EU-12312313",
            "LEI-230948203948",
            "EORI-124989872349",
            "SEED-32424234",
            "SIC-3423423",
            TestAddress.address02);

    private String legalPersonIdentifier;
    private String legalName;
    private String vatRegistration;
    private String taxReference;
    private String d2012_17_EUIdentifier;
    private String lei;
    private String eori;
    private String seed;
    private String sic;
    private TestAddress address;

    TestPersonLegal(String legalPersonIdentifier, String legalName, String vatRegistration, String taxReference, String d2012_17_EUIdentifier, String lei, String eori, String seed, String sic, TestAddress address) {
        this.legalPersonIdentifier = legalPersonIdentifier;
        this.legalName = legalName;
        this.vatRegistration = vatRegistration;
        this.taxReference = taxReference;
        this.d2012_17_EUIdentifier = d2012_17_EUIdentifier;
        this.lei = lei;
        this.eori = eori;
        this.seed = seed;
        this.sic = sic;
        this.address = address;
    }
}
