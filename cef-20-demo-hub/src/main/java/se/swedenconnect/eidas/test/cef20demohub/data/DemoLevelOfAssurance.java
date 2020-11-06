package se.swedenconnect.eidas.test.cef20demohub.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

@Getter
@AllArgsConstructor
public enum DemoLevelOfAssurance {
    none("", new ArrayList<>(), "X", "No Notified LoA", true, true),
    low("http://eidas.europa.eu/LoA/low", Arrays.asList("A", "B"), "A", "eIDAS Low",true, false),
    substantial("http://eidas.europa.eu/LoA/substantial", Arrays.asList("C", "D"), "C","eIDAS Substantial", true, false),
    high("http://eidas.europa.eu/LoA/high", Arrays.asList("E"), "E", "eIDAS High",true, false),
    nonNotifiedLow("http://eidas.europa.eu/NotNotified/LoA/low", Arrays.asList("http://eidas.europa.eu/NotNotified/LoA/low"),
      "http://eidas.europa.eu/NotNotified/LoA/low", "Non Notified Low",false, false),
    nonNotifiedSubstantial("http://eidas.europa.eu/NotNotified/LoA/substantial", Arrays.asList("http://eidas.europa.eu/NotNotified/LoA/substantial"),
      "http://eidas.europa.eu/NotNotified/LoA/substantial", "Non Notified Substantial",false, false),
    nonNotifiedHigh("http://eidas.europa.eu/NotNotified/LoA/high", Arrays.asList("http://eidas.europa.eu/NotNotified/LoA/high"),
      "http://eidas.europa.eu/NotNotified/LoA/high", "Non Notified High",false, false);

    String uri;
    List<String> matchList;
    String key;
    String displayName;
    boolean notified;
    boolean empty;


    public static List<DemoLevelOfAssurance> getList() {
        return Arrays.asList(values());
    }

    public static Optional<DemoLevelOfAssurance> getLoaFromDemoLevel(String demoLevel) {
        return Arrays.stream(values()).filter(dloa -> dloa.getMatchList().contains(demoLevel)).findFirst();
    }

    public static String getRegexp() {
        StringBuilder b = new StringBuilder();
        b.append("^(");
        Iterator<DemoLevelOfAssurance> iterator = Arrays.asList(values()).iterator();
        while (iterator.hasNext()) {
            b.append(iterator.next().getKey());
            b.append(iterator.hasNext() ? "|" : "");
        }
        b.append(")$");
        return b.toString();
    }
}
