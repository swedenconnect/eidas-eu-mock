package se.swedenconnect.eidas.test.cef20demohub.data.user;

import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasLegalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasNaturalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestAddressYaml;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestLegalYaml;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestUserYaml;

import java.util.*;
import java.util.stream.Collectors;

public class YamlUserFactory {

    public static List<User> getSortedNatuserList(List<TestUserYaml> natUserYamlList, boolean addEmpty){
        List<User> userList = natUserYamlList.stream().map(testUserYaml -> getUser(testUserYaml)).collect(Collectors.toList());
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        if (addEmpty) {
            userList.add(0,new User("--- No Natural Person Identity ---"));
        }

        return userList;
    }

    public static List<User> getSortedLegalPersonList(List<TestLegalYaml> legalUserYamlList, boolean addEmpty){
        List<User> userList = legalUserYamlList.stream().map(legalPersonYaml -> getUser(legalPersonYaml)).collect(Collectors.toList());
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        if (addEmpty) {
            userList.add(0,new User("--- No Legal Person Identity ---"));
        }

        return userList;
    }

    private static User getUser(TestUserYaml testUserYaml) {
        User user = new User(testUserYaml);
        user.setNaturalPersonAttributes(getNaturalPersonAttributes(testUserYaml));
        return user;
    }

    private static User getUser(TestLegalYaml testLegalYaml) {
        User user = new User(testLegalYaml);
        user.setLegalPersonAttributes(getLegalPersonAttributes(testLegalYaml));
        return user;
    }

    public static List<TestUserYaml> getPreconfiguredNatUserYamlList(){
        return Arrays.stream(PreConfigNaturalPersonEnum.values())
                .map(preConfigNaturalPersonEnum -> getPreConfigTestUser(preConfigNaturalPersonEnum))
                .collect(Collectors.toList());
    }

    public static List<TestLegalYaml> getPreconfiguredLegalPersonYamlList(){
        return Arrays.stream(PreConfigLegalPersonEnum.values())
                .map(preConfigLegalPersonEnum -> getPreConfigLegalPerson(preConfigLegalPersonEnum))
                .collect(Collectors.toList());
    }

    public static TestUserYaml getPreConfigTestUser(PreConfigNaturalPersonEnum natPerson){
        TestUserYaml tu = new TestUserYaml();
        tu.setGivenName(natPerson.getGivenName());
        tu.setSurname(natPerson.getSurname());
        tu.setPersonIdentifier(natPerson.getPersonIdentifier());
        tu.setDateOfBirth(natPerson.getDateOfBirth());
        tu.setGender(natPerson.getGender());
        tu.setBirthName(natPerson.getBirthName());
        tu.setPlaceOfBirth(natPerson.getPlaceOfBirth());
        tu.setAddress(getPreconfigAddress(natPerson.getAddress()));
        return tu;
    }

    public static TestLegalYaml getPreConfigLegalPerson(PreConfigLegalPersonEnum legalPerson){
        TestLegalYaml tl = new TestLegalYaml();
        tl.setAddress(getPreconfigAddress(legalPerson.getAddress()));
        tl.setD2012_17_EUIdentifier(legalPerson.getD2012_17_EUIdentifier());
        tl.setEori(legalPerson.getEori());
        tl.setLegalName(legalPerson.getLegalName());
        tl.setLegalPersonIdentifier(legalPerson.getLegalPersonIdentifier());
        tl.setLei(legalPerson.getLei());
        tl.setSeed(legalPerson.getSeed());
        tl.setSic(legalPerson.getSic());
        tl.setTaxReference(legalPerson.getTaxReference());
        tl.setVatRegistration(legalPerson.getVatRegistration());
        return tl;
    }

    private static TestAddressYaml getPreconfigAddress(PreConfigAddressEnum address) {
        if (address==null){
            return null;
        }
        TestAddressYaml ta = new TestAddressYaml();
        ta.setCity(address.getCity());
        ta.setCountry(address.getCountry());
        ta.setFullAddress(address.getFullAddress());
        ta.setId(address.getId());
        ta.setLocation(address.getLocation());
        ta.setPoBox(address.getPoBox());
        ta.setPostalCode(address.getPostalCode());
        ta.setStreet(address.getStreet());
        ta.setStreetNumber(address.getStreetNumber());
        return ta;
    }

    public static ComplexAddressAttribute getTestAddress (TestAddressYaml preConfigAddressEnum){
        ComplexAddressAttribute address = new ComplexAddressAttribute();
        address.setAddressId(preConfigAddressEnum.getId());
        address.setFullCVAddress(preConfigAddressEnum.getFullAddress());
        address.setThoroughFare(preConfigAddressEnum.getStreet());
        address.setLocatorDesignator(preConfigAddressEnum.getStreetNumber());
        address.setAdminUnitFirstLine(preConfigAddressEnum.getCountry());
        address.setPostCode(preConfigAddressEnum.getPostalCode());
        address.setAdminUnitSecondLine(preConfigAddressEnum.getCity());
        address.setPostName(preConfigAddressEnum.getLocation()+ " "+ preConfigAddressEnum.getCity());
        address.setPoBox(preConfigAddressEnum.getPoBox());
        address.setLocatorName(preConfigAddressEnum.getLocation());
        return address;
    }

    public static Map<EidasNaturalAttributeFriendlyName, User.AttributeData> getNaturalPersonAttributes (TestUserYaml person){
        Map<EidasNaturalAttributeFriendlyName, User.AttributeData> attrMap = new HashMap<>();

        putAttr(EidasNaturalAttributeFriendlyName.firstName, User.AttributeDataType.stringType, person.getGivenName(), attrMap);
        putAttr(EidasNaturalAttributeFriendlyName.familyName, User.AttributeDataType.stringType, person.getSurname(), attrMap);
        putAttr(EidasNaturalAttributeFriendlyName.personIdentifier, User.AttributeDataType.stringType,person.getPersonIdentifier(), attrMap);
        putAttr(EidasNaturalAttributeFriendlyName.dateOfBirth, User.AttributeDataType.dateType, person.getDateOfBirth(), attrMap);
        putAttr(EidasNaturalAttributeFriendlyName.gender, User.AttributeDataType.stringType, person.getGender(), attrMap);
        putAttr(EidasNaturalAttributeFriendlyName.placeOfBirth, User.AttributeDataType.stringType, person.getPlaceOfBirth(), attrMap);
        putAttr(EidasNaturalAttributeFriendlyName.birthName, User.AttributeDataType.stringType, person.getBirthName(), attrMap);

        if (person.getAddress()!=null){
            attrMap.put(EidasNaturalAttributeFriendlyName.currentAddress, getAttributeData(User.AttributeDataType.addressType, getTestAddress(person.getAddress())));
        }

        return attrMap;
    }

    public static Map<EidasLegalAttributeFriendlyName, User.AttributeData> getLegalPersonAttributes (TestLegalYaml org){
        Map<EidasLegalAttributeFriendlyName, User.AttributeData> attrMap = new HashMap<>();

        putAttr(EidasLegalAttributeFriendlyName.legalPersonIdentifier, User.AttributeDataType.stringType, org.getLegalPersonIdentifier(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.legalName, User.AttributeDataType.stringType, org.getLegalName(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.vatRegistration, User.AttributeDataType.stringType, org.getVatRegistration(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.taxReference, User.AttributeDataType.stringType, org.getTaxReference(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.d2012_17_EUIdentifier, User.AttributeDataType.stringType, org.getD2012_17_EUIdentifier(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.lei, User.AttributeDataType.stringType, org.getLei(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.eori, User.AttributeDataType.stringType, org.getEori(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.seed, User.AttributeDataType.stringType, org.getSeed(), attrMap);
        putAttr(EidasLegalAttributeFriendlyName.sic, User.AttributeDataType.stringType, org.getSic(), attrMap);

        if (org.getAddress()!=null){
            attrMap.put(EidasLegalAttributeFriendlyName.legalAddress, getAttributeData(User.AttributeDataType.addressType, getTestAddress(org.getAddress())));
        }

        return attrMap;
    }




    private static void putAttr(EidasLegalAttributeFriendlyName attrName, User.AttributeDataType type, String val, Map<EidasLegalAttributeFriendlyName,User.AttributeData> attrMap) {
        if (val!=null){
            attrMap.put(attrName, getAttributeData(type, val));
        }

    }
    private static void putAttr(EidasNaturalAttributeFriendlyName attrName, User.AttributeDataType type, String val, Map<EidasNaturalAttributeFriendlyName,User.AttributeData> attrMap) {
        if (val!=null){
            attrMap.put(attrName, getAttributeData(type, val));
        }

    }


    private static User.AttributeData getAttributeData(User.AttributeDataType dataType, Object val){
        User.AttributeData data = new User.AttributeData();
        data.setDataType(dataType);

        switch (dataType){
            case dateType:
                data.setDateValue((String)val);
                return data;
            case stringType:
                data.setStringValue((String)val);
                return data;
            case addressType:
                data.setAddressAttrValue((ComplexAddressAttribute) val);
                return data;
        }
        return null;
    }


}
