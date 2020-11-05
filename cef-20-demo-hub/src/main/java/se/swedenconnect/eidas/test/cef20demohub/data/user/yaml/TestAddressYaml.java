package se.swedenconnect.eidas.test.cef20demohub.data.user.yaml;

import lombok.Data;

@Data
public class TestAddressYaml {

    private String location;
    private String street;
    private String streetNumber;
    private String poBox;
    private String postalCode;
    private String city;
    private String region;
    private String country;

    public TestAddressYaml() {
    }
}
