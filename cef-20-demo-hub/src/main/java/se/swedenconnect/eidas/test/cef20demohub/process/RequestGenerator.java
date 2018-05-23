package se.swedenconnect.eidas.test.cef20demohub.process;

import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.SimpleProtocol.RequestedAuthenticationContext;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.test.cef20demohub.data.*;

import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class RequestGenerator {
    public static final String CITIZEN_COUNTRY_PARAM = "citizenCountry";
    public static final String SP_TYPE_PARAM = "spType";
    public static final String REQ_LOA_PARAM = "requestLoa";
    public static final String LOA_MATCHING_PARAM = "loaComparison";
    public static final String NAME_ID_PARAM = "nameIdType";

    //Regexp
    public static final String COUNTRY_REGEXP = "^[A-Z]{2}$";
    public static final String SP_TYPE_REGEXP = SpType.getRegexp();
    public static final String REQUESTED_LOA_REGEXP = DemoLevelOfAssurance.getRegexp();
    public static final String LOA_MATCHING_REGEXP = LevelOfAssuranceComparison.getRegexp();
    public static final String NAME_ID_REGEXP = "^("+RequestModel.UNSPECIFIED+"|"+RequestModel.TRANSIENT+"|"+RequestModel.PERSISTENT+")$";


    public RequestData getRequest(Map<String, String[]> parameterMap, String returnUrl, String serviceName) throws JAXBException, IllegalArgumentException {
        RequestModel rm = new RequestModel();
        rm.setReturnUrl(returnUrl);
        rm.setCitizenCountry(getParamValue(CITIZEN_COUNTRY_PARAM, parameterMap, COUNTRY_REGEXP));
        rm.setEidasloa(getParamValue(REQ_LOA_PARAM, parameterMap, REQUESTED_LOA_REGEXP));
        rm.setEidasloaCompareType(LevelOfAssuranceComparison.getEnumFromValue(getParamValue(LOA_MATCHING_PARAM, parameterMap, LOA_MATCHING_REGEXP)).get());
        rm.setEidasNameIdentifier(getParamValue(NAME_ID_PARAM, parameterMap, NAME_ID_REGEXP));
        rm.setEidasSPType(SpType.getEnumFromValue(getParamValue(SP_TYPE_PARAM, parameterMap, SP_TYPE_REGEXP)).get());
        rm.setProviderName(serviceName);

        //Set attributes
        List<Attribute> simpleAttributes = getAttributeList(parameterMap);
        rm.setSimpleAttributes(simpleAttributes);
        RequestData request = getRequest(rm);
        return request;
    }

    private String getParamValue(String paramName, Map<String, String[]> parameterMap) throws IllegalArgumentException {
        return getParamValue(paramName, parameterMap, null);
    }

    private String getParamValue(String paramName, Map<String, String[]> parameterMap, String regexp) throws IllegalArgumentException {
        return getParamValue(paramName, parameterMap, regexp, null);

    }

    private String getParamValue(String paramName, Map<String, String[]> parameterMap, String regexp, String defaultVal) throws IllegalArgumentException {
        String paramVal;
        try {
            if (!parameterMap.containsKey(paramName)) {
                if (defaultVal == null) {
                    throw new IllegalArgumentException("Param " + paramName + " is required from request page");
                }
                paramVal = defaultVal;
            } else {
                String[] valArray = parameterMap.get(paramName);
                if (valArray == null || valArray.length == 0) {
                    throw new IllegalArgumentException("Param " + paramName + " is required from request page");
                }
                paramVal = valArray[0];
            }
            if (regexp != null) {
                if (!paramVal.matches(regexp)) {
                    throw new IllegalArgumentException("Input value for parameter " + paramName + " does not meet content requirements");
                }
            }
            return paramVal;

        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to retrieve parameter " + paramName + " from request page");
        }
    }


    public RequestData getRequest(RequestModel requestModel) throws JAXBException {
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setId(UUID.randomUUID().toString());
        authenticationRequest.setServiceUrl(requestModel.getReturnUrl());
        authenticationRequest.setProviderName(requestModel.getProviderName());
        authenticationRequest.setCitizenCountry(requestModel.getCitizenCountry());
        String eidasNameIdentifier = requestModel.getEidasNameIdentifier();
        if (RequestModel.PERSISTENT.equalsIgnoreCase(eidasNameIdentifier)
                || RequestModel.TRANSIENT.equalsIgnoreCase(eidasNameIdentifier)
                || RequestModel.UNSPECIFIED.equalsIgnoreCase(eidasNameIdentifier))
            authenticationRequest.setNameIdPolicy(eidasNameIdentifier);
        authenticationRequest.setSpType(requestModel.getEidasSPType().getValue());

        //Set LoA
        final RequestedAuthenticationContext requestedAuthenticationContext = new RequestedAuthenticationContext();
        final ArrayList<String> levelOfAssurances = new ArrayList<>();
        levelOfAssurances.add(requestModel.getEidasloa());
        requestedAuthenticationContext.setContextClass(levelOfAssurances);
        requestedAuthenticationContext.setComparison(requestModel.getEidasloaCompareType().getValue());
        authenticationRequest.setAuthContext(requestedAuthenticationContext);

        //Set attributes
        authenticationRequest.setAttributes(requestModel.getSimpleAttributes());
        String jsonRequest = new SimpleProtocolProcess().convert2Json(authenticationRequest);
        String b64Request = new String(Base64.getEncoder().encode(jsonRequest.getBytes(StandardCharsets.UTF_8)));

        //Create return object
        RequestData requestData = new RequestData();
        requestData.setAuthnRequest(authenticationRequest);
        requestData.setBase64Request(b64Request);
        return requestData;
    }


    public List<Attribute> getAttributeList(Map<String, String[]> parameterMap) {
        List<Attribute> attributeList = new ArrayList<>();

        EidasNaturalAttributeFriendlyName[] natAttrArray = EidasNaturalAttributeFriendlyName.values();
        for (EidasNaturalAttributeFriendlyName natAttr : natAttrArray) {
            //Target Attributes
            if (parameterMap.containsKey(natAttr.name())) {
                attributeList.add(getAttribute(natAttr.getFriendlyName(), parameterMap.containsKey("req_" + natAttr.name())));
            }
            //Representative Attributes
            if (parameterMap.containsKey("repr_"+natAttr.name())) {
                attributeList.add(getAttribute(natAttr.getFriendlyName(true), parameterMap.containsKey("req_repr_" + natAttr.name())));
            }
        }
        EidasLegalAttributeFriendlyName[] legalAttrArray = EidasLegalAttributeFriendlyName.values();
        for (EidasLegalAttributeFriendlyName legalAttr : legalAttrArray) {
            if (parameterMap.containsKey(legalAttr.name())) {
                attributeList.add(getAttribute(legalAttr.getFriendlyName(), parameterMap.containsKey("req_" + legalAttr.name())));
            }
            //Representative Attributes
            if (parameterMap.containsKey("repr_"+legalAttr.name())) {
                attributeList.add(getAttribute(legalAttr.getFrendlyName(true), parameterMap.containsKey("req_repr_" + legalAttr.name())));
            }
        }
        return attributeList;
    }

    private Attribute getAttribute(String attributeFriendlyName) {
        return getAttribute(attributeFriendlyName, false);
    }

    private Attribute getAttribute(String attributeFriendlyName, boolean required) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeFriendlyName);
        attribute.setRequired(required);
        return attribute;
    }


}
