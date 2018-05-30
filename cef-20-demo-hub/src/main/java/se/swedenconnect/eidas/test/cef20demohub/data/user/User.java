package se.swedenconnect.eidas.test.cef20demohub.data.user;

import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import lombok.Data;
import lombok.Getter;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasLegalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasNaturalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestLegalYaml;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestUserYaml;

import java.util.Map;

@Data
public class User {

    private String id;
    private String name;
    private PersonType personType;
    private Map<EidasNaturalAttributeFriendlyName, AttributeData> naturalPersonAttributes;
    private Map<EidasLegalAttributeFriendlyName, AttributeData> legalPersonAttributes;

    public User(String name) {
        this.id = "{empty}";
        this.name = name;
    }

    public User(TestUserYaml person) {
        this.id = person.getPersonIdentifier();
        this.name = person.getGivenName()+ " " + person.getSurname()+ " ("+person.getPersonIdentifier()+")";
        this.personType = PersonType.natural;
    }

    public User(TestLegalYaml org) {
        this.id=org.getLegalPersonIdentifier();
        this.name = org.getLegalName() + " ("+org.getLegalPersonIdentifier()+")";
        this.personType = PersonType.legal;
    }

    @Data
    public static class AttributeData {
        private AttributeDataType dataType;
        private ComplexAddressAttribute addressAttrValue;
        private String stringValue;
        private String dateValue;
    }

    @Getter
    public enum AttributeDataType {
        dateType ("date"),
        stringType ("string_list"),
        stringListType("string_list"),
        addressType("addressId");

        private String exportType;

        AttributeDataType(String exportType) {
            this.exportType = exportType;
        }
    }

    public enum PersonType {
        natural, legal;
    }
}
