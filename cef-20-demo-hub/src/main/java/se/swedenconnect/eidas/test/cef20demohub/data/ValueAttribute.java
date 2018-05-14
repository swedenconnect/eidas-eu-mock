package se.swedenconnect.eidas.test.cef20demohub.data;

import lombok.Data;

@Data
public class ValueAttribute {
    String name;
    String value;

    public ValueAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ValueAttribute() {
    }
}
