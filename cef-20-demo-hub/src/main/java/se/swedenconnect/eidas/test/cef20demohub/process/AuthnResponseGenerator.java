package se.swedenconnect.eidas.test.cef20demohub.process;

import eu.eidas.SimpleProtocol.*;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.test.cef20demohub.configuration.SPConfigurationProperties;
import se.swedenconnect.eidas.test.cef20demohub.configuration.UserConfiguration;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasLegalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.EidasNaturalAttributeFriendlyName;
import se.swedenconnect.eidas.test.cef20demohub.data.ResponseData;
import se.swedenconnect.eidas.test.cef20demohub.data.user.User;

import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AuthnResponseGenerator {
    private static final String NATURAL_PERSON_PARAMETER = "natPerson";
    private static final String LEGAL_PERSON_PARAMETER = "legalPerson";
    private static final String REPER_NATURAL_PERSON_PARAMETER = "reprNatPerson";
    private static final String REPR_LEGAL_PERSON_PARAMETER = "reprLegalPerson";
    private static final String LOA_PARAMETER = "loaSelect";
    private static final String EMPTY_SELECT = "{empty}";

    private final SPConfigurationProperties configurationProperties;
    private final UserConfiguration userConfiguration;

    @Autowired
    public AuthnResponseGenerator(SPConfigurationProperties configurationProperties, UserConfiguration userConfiguration) {
        this.configurationProperties = configurationProperties;
        this.userConfiguration = userConfiguration;
    }

    public ResponseData getAuthnResponse(Map<String, String[]> parameterMap, AuthenticationRequest authenticationRequest, String clientIp, String spCountry) throws JAXBException {
        SPConfigurationProperties.SpConfig config = configurationProperties.getSp().get(spCountry);
        ResponseData responseData = new ResponseData();
        Response response = responseData.getResponse();

        response.setId(UUID.randomUUID().toString());
        response.setClientIpAddress(clientIp);
        response.setDestination(authenticationRequest.getServiceUrl());
        response.setInResponseTo(authenticationRequest.getId());
        response.setIssuer(config.getIdpName());

        ResponseStatus status = new ResponseStatus();
        response.setStatus(status);
        response.setVersion("1");
        response.setCreatedOn((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(Calendar.getInstance().getTime()));

        setUserAttributes(parameterMap, authenticationRequest, responseData, spCountry);
        setNameIdAndSubject(responseData, authenticationRequest);

        //Finally set status
        status.setStatusCode(responseData.isError() ? "error" : "success");
        if (responseData.isError()) {
            status.setStatusMessage(responseData.getErrorMessage());
        }

        String jsonRequest = new SimpleProtocolProcess().convert2Json(response);
        String b64Response = new String(Base64.getEncoder().encode(jsonRequest.getBytes(StandardCharsets.UTF_8)));
        responseData.setB64Response(b64Response);
        return responseData;
    }

    private void setNameIdAndSubject(ResponseData responseData, AuthenticationRequest authenticationRequest) {
        List<Attribute> attributeList = responseData.getResponse().getAttributes();
        String nameIdPolicy = authenticationRequest.getNameIdPolicy();
        responseData.getResponse().setNameId(nameIdPolicy);

        Optional<Attribute> natId = attributeList.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(EidasNaturalAttributeFriendlyName.personIdentifier.getFriendlyName())).findFirst();
        Optional<Attribute> legalId = attributeList.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(EidasLegalAttributeFriendlyName.legalPersonIdentifier.getFriendlyName())).findFirst();

        String subjectId = null;
        if (natId.isPresent()) {
            subjectId = getSimpleAttrVal(natId.get());
        } else {
            if (legalId.isPresent()) {
                subjectId = getSimpleAttrVal(legalId.get());
            }
        }
        if (subjectId != null) {
            responseData.getResponse().setSubject(subjectId);
        } else {
            responseData.setError(true);
            responseData.setErrorMessageTitle("No ID data attribute present");
            responseData.setErrorMessage("Selected identities contains no ID attributes");
        }

    }

    private String getSimpleAttrVal(Attribute attribute) {
        if (attribute instanceof StringListAttribute) {
            StringListAttribute slattr = (StringListAttribute) attribute;
            List<StringListValue> values = slattr.getValues();
            if (!values.isEmpty()) {
                return values.get(0).getValue();
            }
        }
        if (attribute instanceof StringAttribute) {
            StringAttribute stringAttribute = (StringAttribute) attribute;
            return stringAttribute.getValue();
        }
        return null;
    }

    /**
     * Sets attributes, subject and nameID in response.
     *
     * @param parameterMap          The HTTP request parameter map
     * @param authenticationRequest The Authentication request
     * @param responseData          The response object
     */
    private void setUserAttributes(Map<String, String[]> parameterMap, AuthenticationRequest authenticationRequest, ResponseData responseData, String country) {
        List<Attribute> attributeList = new ArrayList<>();
        responseData.getResponse().setAttributes(attributeList);

        Map<String, User> userMap = userConfiguration.getCountryUserMap().get(country);
        Map<String, Boolean> requestedAttrMap = getRequestedAttributesMap(authenticationRequest);
        responseData.setRequestedAttributesMap(requestedAttrMap);

        final String natPersonId = getParameterString(parameterMap.get(NATURAL_PERSON_PARAMETER));
        final String legalPersonId = getParameterString(parameterMap.get(LEGAL_PERSON_PARAMETER));
        final String reprNatPersonId = getParameterString(parameterMap.get(REPER_NATURAL_PERSON_PARAMETER));
        final String reprLegalPersonId = getParameterString(parameterMap.get(REPR_LEGAL_PERSON_PARAMETER));
        final String loaSelectKey = getParameterString(parameterMap.get(LOA_PARAMETER));

        if (!(testPersonId(natPersonId, userMap)
                && testPersonId(legalPersonId, userMap)
                && testPersonId(reprNatPersonId, userMap)
                && testPersonId(reprLegalPersonId, userMap)
        )) {
            responseData.setError(true);
            responseData.setErrorMessageTitle("Bad user selection");
            responseData.setErrorMessage("Illegal selection of user identities");
            return;
        }

        if (natPersonId.equalsIgnoreCase(EMPTY_SELECT) && legalPersonId.equalsIgnoreCase(EMPTY_SELECT)) {
            responseData.setError(true);
            responseData.setErrorMessageTitle("Bad user selection");
            responseData.setErrorMessage("At least one legal or natural person identity must be selected");
            return;
        }

        // Set Loa
        responseData.getResponse().setAuthContextClass(loaSelectKey);

        // Set attributes
        addAttributes(natPersonId, userMap, responseData, false, false);
        addAttributes(legalPersonId, userMap, responseData, true, false);
        addAttributes(reprNatPersonId, userMap, responseData, false, true);
        addAttributes(reprLegalPersonId, userMap, responseData, true, true);

    }

    private Map<String, Boolean> getRequestedAttributesMap(AuthenticationRequest authenticationRequest) {
        Map<String, Boolean> reqAttrMap = new HashMap<>();
        List<Attribute> attributeList = authenticationRequest.getAttributes();
        attributeList.stream().forEach(attribute -> {
            reqAttrMap.put(attribute.getName(), attribute.getRequired());
        });
        return reqAttrMap;
    }

    private void addAttributes(String id, Map<String, User> userMap, ResponseData responseData, boolean legal, boolean representative) {
        if (id.equalsIgnoreCase(EMPTY_SELECT)) {
            return;
        }
        User user = userMap.get(id);
        List<Attribute> attributes = responseData.getResponse().getAttributes();
        Map<String, Boolean> reqAttrMap = responseData.getRequestedAttributesMap();

        if (legal) {
            Map<EidasLegalAttributeFriendlyName, User.AttributeData> legalPersonAttributes = user.getLegalPersonAttributes();
            legalPersonAttributes.keySet().stream()
                    .filter(legalFriendlyName -> representative || reqAttrMap.containsKey(legalFriendlyName.getFriendlyName()))
                    .forEach(legalFriendlyName -> {
                        String friendlyName = legalFriendlyName.getFrendlyName(representative);
                        User.AttributeData attributeData = legalPersonAttributes.get(legalFriendlyName);
                        Attribute attribute = getAttribute(friendlyName, attributeData);
                        if (attribute != null) {
                            attributes.add(attribute);
                        }
                    });
        } else {
            Map<EidasNaturalAttributeFriendlyName, User.AttributeData> naturalPersonAttributes = user.getNaturalPersonAttributes();
            naturalPersonAttributes.keySet().stream()
                    .filter(natFriendlyName -> representative || reqAttrMap.containsKey(natFriendlyName.getFriendlyName()))
                    .forEach(eidasNaturalAttributeFriendlyName -> {
                        String friendlyName = eidasNaturalAttributeFriendlyName.getFriendlyName(representative);
                        User.AttributeData attributeData = naturalPersonAttributes.get(eidasNaturalAttributeFriendlyName);
                        Attribute attribute = getAttribute(friendlyName, attributeData);
                        if (attribute != null) {
                            attributes.add(attribute);
                        }
                    });
        }
    }

    private Attribute getAttribute(String frendlyName, User.AttributeData attributeData) {
        Attribute attribute = null;
        switch (attributeData.getDataType()) {
            case dateType:
                DateAttribute dateAttr = new DateAttribute();
                dateAttr.setName(frendlyName);
                dateAttr.setValue(getDate(attributeData.getDateValue()));
                attribute = dateAttr;
                break;
            case stringType:
                StringListAttribute strAttr = new StringListAttribute();
                strAttr.setName(frendlyName);
                StringListValue stringListValue = new StringListValue();
                stringListValue.setValue(attributeData.getStringValue());
                strAttr.setValues(Arrays.asList(stringListValue));
                attribute = strAttr;
                break;
            case addressType:
                AddressAttribute addrAttr = new AddressAttribute();
                addrAttr.setName(frendlyName);
                addrAttr.setValue(attributeData.getAddressAttrValue());
                attribute = addrAttr;
                break;
        }
        return attribute;
    }

    private Date getDate(String dateValue) {
        String[] dateSplit = dateValue.split("\\-");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("CET"));
        calendar.set(Integer.valueOf(dateSplit[0]), Integer.valueOf(dateSplit[1]) - 1, Integer.valueOf(dateSplit[2]));
        return calendar.getTime();
    }

    private boolean testPersonId(String legalPersonId, Map<String, User> userMap) {
        if (userMap.containsKey(legalPersonId)) {
            return true;
        }
        if (legalPersonId.equalsIgnoreCase(EMPTY_SELECT)) {
            return true;
        }
        return false;
    }

    private String getParameterString(String[] stringArray) {
        if (stringArray == null) {
            return null;
        }
        if (stringArray.length < 1) {
            return null;
        }
        return stringArray[0];
    }

}
