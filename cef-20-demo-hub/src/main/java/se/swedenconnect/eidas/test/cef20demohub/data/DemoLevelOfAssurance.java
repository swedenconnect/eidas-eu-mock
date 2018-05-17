package se.swedenconnect.eidas.test.cef20demohub.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum DemoLevelOfAssurance {
    low("http://eidas.europa.eu/LoA/low", Arrays.asList("A", "B"), "A"),
    substantial("http://eidas.europa.eu/LoA/substantial", Arrays.asList("C", "D"), "C"),
    high("http://eidas.europa.eu/LoA/high", Arrays.asList("E"),"E");

    String uri;
    List<String> matchList;
    String key;

    DemoLevelOfAssurance(String uri, List<String> matchList, String key) {
        this.uri = uri;
        this.matchList = matchList;
        this.key = key;
    }

    public String getUri() {
        return uri;
    }

    public List<String> getMatchList() {
        return matchList;
    }

    public String getKey() {
        return key;
    }

    public static List<DemoLevelOfAssurance> getList(){
        return Arrays.asList(values());
    }

    public static Optional<DemoLevelOfAssurance> getLoaFromDemoLevel(String demoLevel){
        return Arrays.stream(values()).filter(dloa -> dloa.getMatchList().contains(demoLevel)).findFirst();
    }
}
