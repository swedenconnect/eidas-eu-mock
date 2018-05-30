package se.swedenconnect.eidas.test.cef20demohub.data.user.yaml;

import lombok.Data;

@Data
public class TestLegalYaml {

    private String legalPersonIdentifier;
    private String legalName;
    private String vatRegistration;
    private String taxReference;
    private String d2012_17_EUIdentifier;
    private String lei;
    private String eori;
    private String seed;
    private String sic;
    private TestAddressYaml address;

    public TestLegalYaml() {
    }
}
