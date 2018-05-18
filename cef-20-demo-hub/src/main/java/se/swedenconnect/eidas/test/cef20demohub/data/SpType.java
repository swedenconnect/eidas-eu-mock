package se.swedenconnect.eidas.test.cef20demohub.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

/**
 * Supported SpType as per the eIDAS specification.
 */
public enum SpType {

    PUBLIC("public"),
    PRIVATE("private");

    private String value;

    SpType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Optional<SpType> getEnumFromValue(String value) {
        return Arrays.stream(values()).filter(spType -> spType.getValue().equalsIgnoreCase(value)).findFirst();
    }


    public static String getRegexp() {
        StringBuilder b = new StringBuilder();
        b.append("^(");
        Iterator<SpType> iterator = Arrays.asList(values()).iterator();
        while (iterator.hasNext()) {
            b.append(iterator.next().getValue());
            b.append(iterator.hasNext() ? "|" : "");
        }
        b.append(")$");
        return b.toString();
    }

}
