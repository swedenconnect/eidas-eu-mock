package se.swedenconnect.eidas.test.cef20demohub.data.user;

import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import lombok.Data;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasLegalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasNaturalAttributeFriendlyName;

import java.util.Map;

@Data
public class User {

    private String name;
    private PersonType personType;
    private Map<EidasNaturalAttributeFriendlyName, AttributeData> naturalPersonAttributes;
    private Map<EidasLegalAttributeFriendlyName, AttributeData> legalPersonAttributes;

    public User(String name) {
        this.name = name;
    }

    public User(TestPersonNatural person) {
        this.name = person.getGivenName()+ " " + person.getSurname()+ " ("+person.getPersonIdentifier()+")";
        this.naturalPersonAttributes = DemoUserFactory.getNaturalPersonAttributes(person);
        this.personType = PersonType.natural;
    }

    public User(TestPersonLegal org) {
        this.name = org.getLegalName() + " ("+org.getLegalPersonIdentifier()+")";
        this.legalPersonAttributes = DemoUserFactory.getLegalPersonAttributes(org);
        this.personType = PersonType.legal;
    }

    @Data
    public static class AttributeData {
        private AttributeDataType dataType;
        private ComplexAddressAttribute addressAttrValue;
        private String stringValue;
        private String dateValue;
    }

    public enum AttributeDataType {
        dateType, stringType, addressType
    }

    public enum PersonType {
        natural, legal;
    }
}
