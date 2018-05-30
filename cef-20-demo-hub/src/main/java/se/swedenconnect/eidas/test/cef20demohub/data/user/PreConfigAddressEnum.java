package se.swedenconnect.eidas.test.cef20demohub.data.user;

import lombok.Getter;

@Getter
public enum PreConfigAddressEnum {
    address01("Street 01, EU-1050 Overtown",
            "Street","01","EU","1050","Overtown","12345","Admin Building",
            "http://example.com/address/id/1234567890"),
    address02("Lane 987, EU-1105 Undertown",
            "Lane","987","EU","1105","Undertown","12345","Bordershop",
            "http://example.com/address/id/9876543210");

    private String fullAddress;
    private String street;
    private String streetNumber;
    private String country;
    private String postalCode;
    private String city;
    private String poBox;
    private String location;
    private String id;

    PreConfigAddressEnum(String fullAddress, String street, String streetNumber, String country, String postalCode, String city, String poBox, String location, String id) {
        this.fullAddress = fullAddress;
        this.street = street;
        this.streetNumber = streetNumber;
        this.country = country;
        this.postalCode = postalCode;
        this.city = city;
        this.poBox = poBox;
        this.location = location;
        this.id = id;
    }
}
