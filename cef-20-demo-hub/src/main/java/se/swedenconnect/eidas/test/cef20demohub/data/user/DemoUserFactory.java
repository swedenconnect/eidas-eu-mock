package se.swedenconnect.eidas.test.cef20demohub.data.user;

import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasLegalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasNaturalAttributeFriendlyName;

import java.util.*;
import java.util.stream.Collectors;

public class DemoUserFactory {

    public static final List<User> testUsers;

    static {
        testUsers = new ArrayList<>();
        testUsers.add(new User(TestPersonNatural.xavi));
        testUsers.add(new User(TestPersonNatural.fridaKranstege));
        testUsers.add(new User(TestPersonLegal.testOrg01));
        testUsers.add(new User(TestPersonLegal.testOrg02));
    }

    private DemoUserFactory() {
    }

    public static List<User> getSortedFilteredUserList(List<User> userList, User.PersonType type){
        List<User> users = userList.stream()
                .filter(user -> user.getPersonType().equals(type))
                .collect(Collectors.toList());

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return users;
    }

    public static ComplexAddressAttribute getTestAddress (TestAddress testAddress){
        ComplexAddressAttribute address = new ComplexAddressAttribute();
        address.setAddressId(testAddress.getId());
        address.setFullCVAddress(testAddress.getFullAddress());
        address.setThoroughFare(testAddress.getStreet());
        address.setLocatorDesignator(testAddress.getStreetNumber());
        address.setAdminUnitFirstLine(testAddress.getCountry());
        address.setPostCode(testAddress.getPostalCode());
        address.setAdminUnitSecondLine(testAddress.getCity());
        address.setPostName(testAddress.getLocation()+ " "+ testAddress.getCity());
        address.setPoBox(testAddress.getPoBox());
        address.setLocatorName(testAddress.getLocation());
        return address;
    }

    public static Map<EidasNaturalAttributeFriendlyName, User.AttributeData> getNaturalPersonAttributes (TestPersonNatural person){
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

    public static Map<EidasLegalAttributeFriendlyName, User.AttributeData> getLegalPersonAttributes (TestPersonLegal org){
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
