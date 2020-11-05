package se.swedenconnect.eidas.test.cef20demohub.data.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreConfigAddressEnum {
    address01("Admin Building","Street","01","12345","1050","Overtown","Wales",
            "UK"),
    address02("Bordershop","Lane","987","12345","1105","Undertown","Scottland",
            "UK");

    private String location;
    private String street;
    private String streetNumber;
    private String poBox;
    private String postalCode;
    private String city;
    private String region;
    private String country;
}
