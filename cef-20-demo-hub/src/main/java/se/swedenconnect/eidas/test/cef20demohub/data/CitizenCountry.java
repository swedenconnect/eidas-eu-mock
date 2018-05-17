package se.swedenconnect.eidas.test.cef20demohub.data;

import lombok.Data;

@Data
public class CitizenCountry {
    private String countryCode;
    private String countryImage;
    private String countryName;

    public CitizenCountry() {
    }

    public CitizenCountry(String countryCode, String countryImage, String countryName) {
        this.countryCode = countryCode;
        this.countryImage = countryImage;
        this.countryName = countryName;
    }
}
