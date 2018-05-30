package se.swedenconnect.eidas.test.cef20demohub.data.user.yaml;

import lombok.Data;

@Data
public class TestAddressYaml {

    private String fullAddress;
    private String street;
    private String streetNumber;
    private String country;
    private String postalCode;
    private String city;
    private String poBox;
    private String location;
    private String id;

    public TestAddressYaml() {
    }
}
