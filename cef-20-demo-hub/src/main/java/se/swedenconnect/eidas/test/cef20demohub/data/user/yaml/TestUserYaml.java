package se.swedenconnect.eidas.test.cef20demohub.data.user.yaml;

import lombok.Data;

@Data
public class TestUserYaml {

    private String givenName;
    private String surname;
    private String personIdentifier;
    private String dateOfBirth;
    private String gender;
    private String placeOfBirth;
    private String birthName;
    private TestAddressYaml address;

    public TestUserYaml() {
    }
}
