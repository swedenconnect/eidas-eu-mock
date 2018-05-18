package se.swedenconnect.eidas.test.cef20demohub.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

public enum LevelOfAssuranceComparison {

    MINIMUM("minimum"),
    EXACT("exact");

    private String value;

    LevelOfAssuranceComparison(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<LevelOfAssuranceComparison> getEnumFromValue(String value) {
        return Arrays.stream(values()).filter(loaComparison -> loaComparison.getValue().equalsIgnoreCase(value)).findFirst();
    }

    public static String getRegexp() {
        StringBuilder b = new StringBuilder();
        b.append("^(");
        Iterator<LevelOfAssuranceComparison> iterator = Arrays.asList(values()).iterator();
        while (iterator.hasNext()) {
            b.append(iterator.next().getValue());
            b.append(iterator.hasNext() ? "|" : "");
        }
        b.append(")$");
        return b.toString();
    }

}
