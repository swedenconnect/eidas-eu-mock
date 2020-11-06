package se.swedenconnect.eidas.test.cef20demohub.data;

import eu.eidas.SimpleProtocol.Attribute;
import lombok.Data;

import java.util.List;

@Data
public class RequestModel {
    public static final String PERSISTENT = "persistent";
    public static final String TRANSIENT = "transient";
    public static final String UNSPECIFIED = "unspecified";

    private String returnUrl, providerName, citizenCountry, eidasNameIdentifier, eidasloa;
    private List<String> nnLoaList;
    private SpType eidasSPType;
    private LevelOfAssuranceComparison eidasloaCompareType;
    private List<Attribute> simpleAttributes;
}
