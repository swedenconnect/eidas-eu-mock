/*
 * Copyright (c) 2020 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package member_country_specific.idp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.SimpleProtocol.AddressAttribute;
import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import eu.eidas.SimpleProtocol.DateAttribute;
import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.ResponseStatus;
import eu.eidas.SimpleProtocol.StringListAttribute;
import eu.eidas.SimpleProtocol.StringListValue;
import eu.eidas.SimpleProtocol.adapter.DateAdapter;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.SimpleProtocol.utils.StatusCodeTranslator;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.AttributeValueTransliterator;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.lang.Charsets;
import eu.eidas.auth.commons.protocol.eidas.impl.AbstractPostalAddressAttributeValueMarshaller;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class ProcessLogin extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    public static final String AUTHN_FAILED = "AuthnFailed";
    public static final String AUTHENTICATION_FAILED = "authenticationFailed";
    private static final Logger logger = Logger.getLogger(ProcessLogin.class.getName());
    private static final String ATTRIBUTES_FILENAME = "eidas-attributes.xml";
    private static final AttributeRegistry coreAttributeRegistry = AttributeRegistries.fromFile(ATTRIBUTES_FILENAME, IDPUtil.getConfigFilePath());
    private static final String ADDITIONAL_ATTRIBUTES_FILENAME = "additional-attributes.xml";
    private static final AttributeRegistry coreAdditionalAttributeRegistry = AttributeRegistries.fromFile(ADDITIONAL_ATTRIBUTES_FILENAME, IDPUtil.getConfigFilePath());
    public static final String UTF_8 = "UTF-8";
    public HttpServletRequest request;
    private String smsspToken;
    private String username;
    private String callback;
    private Properties idpProperties;

    public ProcessLogin() throws IOException {
        idpProperties = IDPUtil.loadConfigs(Constants.IDP_PROPERTIES);
    }

    public static List<String> getValuesOfAttribute(String attrName, String value) {
        logger.trace("[processAuthentication] Setting: " + attrName + "=>" + value);
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(value);
        if (AttributeValueTransliterator.needsTransliteration(value)) {
            String trValue = AttributeValueTransliterator.transliterate(value);
            tmp.add(trValue);
            logger.trace("[processAuthentication] Setting transliterated: " + attrName + "=>" + trValue);
        }
        return tmp;
    }

    private Properties loadConfigs(String path) {
        try {
            return IDPUtil.loadConfigs(path);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new ApplicationSpecificIDPException("Loading file...", "Could not load configuration file '" + path + "'");
        }
    }

    protected ImmutableAttributeMap.Builder addAttributeValuesJson(AttributeDefinition<?> attr, Properties users, ImmutableAttributeMap.Builder mapBuilder) {

        //lookup in properties file
        String attrName = attr.getFriendlyName();
        String key = username + "." + attrName.replaceAll(".*/", "");

        String value = users.getProperty(key);
        ArrayList<String> values = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            values.addAll(getValuesOfAttribute(attrName, value));
        } else {
            String multivalues = users.getProperty(key + ".multivalue");
            if (null != multivalues && "true".equalsIgnoreCase(multivalues)) {
                for (int i = 1; null != users.getProperty(key + "." + i); i++)
                    values.addAll(getValuesOfAttribute(attrName, users.getProperty(key + "." + i)));
            }
        }

        if (!values.isEmpty()) {
            AttributeValueMarshaller<?> attributeValueMarshaller = attr.getAttributeValueMarshaller();
            ImmutableSet.Builder<AttributeValue<?>> builder = ImmutableSet.builder();
            for (final String val : values) {
                AttributeValue<?> attributeValue = null;
                try {
                    if (AttributeValueTransliterator.needsTransliteration(val))
                        attributeValue = attributeValueMarshaller.unmarshal(val, true);
                    else {
                        if (attributeValueMarshaller instanceof AbstractPostalAddressAttributeValueMarshaller) {
                            attributeValue = ((AbstractPostalAddressAttributeValueMarshaller) attributeValueMarshaller).unmarshallAddressWithoutDecode(val);
                        }
                        else {
                            attributeValue = attributeValueMarshaller.unmarshal(val, false);
                        }
                    }
                } catch (AttributeValueMarshallingException e) {
                    throw new IllegalStateException(e);
                }
                builder.add(attributeValue);
            }
            mapBuilder.put(attr, (ImmutableSet) builder.build());
        }
        return mapBuilder;
    }

    public ImmutableAttributeMap loadUserDataBasedOnJson(AuthenticationRequest jSonRequest, Properties users, String username) {

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        for (Attribute attr : jSonRequest.getAttributes()) {
            ImmutableSortedSet<AttributeDefinition<?>> attributeDefinition = coreAttributeRegistry.getByFriendlyName(attr.getName());
            ImmutableSortedSet<AttributeDefinition<?>> additionalAttributeDefinition =
                    coreAdditionalAttributeRegistry.getByFriendlyName(attr.getName());

            AttributeDefinition<?> attributeDefinitionOut = attributeDefinition.isEmpty() ? additionalAttributeDefinition.first() : attributeDefinition.first();

            addAttributeValuesJson(attributeDefinitionOut, users, mapBuilder);
        }
        Set<String> userAttributes = users.stringPropertyNames();
        for (String attribute : userAttributes) {
            if (attribute.startsWith(username + ".Representative")) {
                ImmutableSortedSet<AttributeDefinition<?>> attributeDefinition = coreAttributeRegistry.getByFriendlyName(attribute.replaceAll(".*\\.", ""));
                addAttributeValuesJson(attributeDefinition.first(), users, mapBuilder);
            }
        }

        return mapBuilder.build();
    }

    public String getServiceUrl(AuthenticationRequest jsonRequest) {
        return jsonRequest.getServiceUrl();
    }

    public String checkAuthentication(String username, String password, String smsspToken) {

        AuthenticationRequest jsonRequest = convertJsonRequest(smsspToken);
        String requestId = jsonRequest.getId();
        if (username == null || password == null) {
            return sendErrorJsonRedirect(requestId, AUTHN_FAILED,
                    AUTHENTICATION_FAILED);
        }

        Properties users = null;
        String pass = null;
        try {
            users = loadConfigs("user.properties");
            pass = users.getProperty(username);
        } catch (Exception e) {
            logger.error(e);
            return sendErrorJsonRedirect(requestId, AUTHN_FAILED,
                    AUTHENTICATION_FAILED);
        }

        if (pass == null || (!pass.equals(password))) {
            return sendErrorJsonRedirect(requestId, AUTHN_FAILED,
                    AUTHENTICATION_FAILED);
        }
        return null;
    }

    private String getIssuer() {
        return idpProperties == null ? null : idpProperties.getProperty(Constants.IDP_ISSUER);
    }

    public AuthenticationRequest convertJsonRequest(String smsspToken) {
        try {
            StringReader stringReaderJson = new StringReader(new String(EidasStringUtil.decodeBytesFromBase64(smsspToken)));
            return (AuthenticationRequest) new SimpleProtocolProcess().convertFromJson(stringReaderJson, AuthenticationRequest.class);
        } catch (JAXBException e) {
            logger.debug(e);
        }
        return null;
    }

    public String generateDummyJsonResponse(String smsspToken, String username, String eidasLoa, String ipAddress, String eidasnameid) throws Exception {

        System.setProperty("file.encoding", UTF_8);
        Field charset = null;
        try {
            charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
        } catch (NoSuchFieldException e) {
            logger.debug(e);
        }

        try {
            charset.set(null, null);
        } catch (IllegalAccessException | NullPointerException e) {
            logger.debug(e);
        }

        Properties users = loadConfigs("user.properties");
        StringReader stringReaderJson = new StringReader(smsspToken);
        AuthenticationRequest jsonRequest = new SimpleProtocolProcess().convertFromJson(stringReaderJson, AuthenticationRequest.class);
        this.username = username;
        String subject = users.getProperty(username + ".subject");
        ImmutableAttributeMap sendAttrMapJson = loadUserDataBasedOnJson(jsonRequest, users, username);

        return generateJsonResponse(jsonRequest, sendAttrMapJson, subject, eidasLoa, ipAddress, eidasnameid);
    }

    private String generateJsonResponse(AuthenticationRequest jsonRequest, ImmutableAttributeMap attrMap, String subject, String eidasLoa, String ipAddress, String eidasnameid) throws Exception {
        List<Attribute> attributesList = new ArrayList<>();

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : attrMap.getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            AttributeValueMarshaller<?> attributeValueMarshaller = definition.getAttributeValueMarshaller();
            final Class parameterizedType = definition.getParameterizedType();
            if ((DateTime.class).equals(parameterizedType)) {
                DateAttribute dateAttribute = translateDateAttribute(attributeValueMarshaller, definition.getFriendlyName(), values);
                attributesList.add(dateAttribute);
            } else if (("LegalAddress".equals(definition.getFriendlyName())) || ("CurrentAddress".equals(definition.getFriendlyName())) || ("RepresentativeLegalAddress".equals(definition.getFriendlyName())) || ("RepresentativeCurrentAddress".equals(definition.getFriendlyName()))) {
                AddressAttribute addressAttribute = translateAddressAttribute(attributeValueMarshaller, definition.getFriendlyName(), values);
                attributesList.add(addressAttribute);
            } else {
                Attribute attribute = translateStringListAttribute(attributeValueMarshaller, definition.getFriendlyName(), values);
                attributesList.add(attribute);
            }
        }
        Response responseObj = new Response();
        responseObj.setId(UUID.randomUUID().toString());
        responseObj.setInResponseTo(jsonRequest.getId());
        responseObj.setIssuer(getIssuer());
        responseObj.setSubject(subject);
        responseObj.setClientIpAddress(ipAddress);
        responseObj.setCreatedOn((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(Calendar.getInstance().getTime()));
        responseObj.setVersion("1");
        String nameIdPolicy;
        if ((eidasnameid == null) || ("".equals(eidasnameid)))
            nameIdPolicy = jsonRequest.getNameIdPolicy();
        else
            nameIdPolicy = eidasnameid;
        if (nameIdPolicy == null || ("".equals(eidasnameid))) {
            nameIdPolicy = "unspecified";
        }

        responseObj.setNameId(nameIdPolicy);

        responseObj.setAuthContextClass(eidasLoa);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode("success");
        responseObj.setStatus(responseStatus);
        responseObj.setAttributes(attributesList);

        String responseJson;
        try {
            responseJson = new SimpleProtocolProcess().convert2Json(responseObj);
            return EidasStringUtil.encodeToBase64(responseJson.getBytes(Charset.forName(UTF_8)));
        } catch (JAXBException e) {
            logger.debug(e.getMessage());
        }
        return null;
    }

    private Attribute translateStringListAttribute(AttributeValueMarshaller<?> attributeValueMarshaller, String friendlyName, ImmutableSet<? extends AttributeValue<?>> attributeValues) {
        final StringListAttribute stringListAttribute = new StringListAttribute();
        stringListAttribute.setName(friendlyName);
        final ArrayList<StringListValue> stringListValues = new ArrayList<>();

        for (AttributeValue<?> attributeValue : attributeValues) {
            String valueString = getAttributeValueMarshaller(attributeValueMarshaller, attributeValue);
            final StringListValue stringListValue = new StringListValue();
            stringListValue.setValue(valueString);
            if (!Charsets.isLatinScript(valueString))
                stringListValue.setLatinScript(false);
            stringListValues.add(stringListValue);
        }

        stringListAttribute.setValues(stringListValues);

        return stringListAttribute;
    }

    private AddressAttribute translateAddressAttribute(AttributeValueMarshaller<?> attributeValueMarshaller, String friendlyName, ImmutableSet<? extends AttributeValue<?>> attributeValues) {
        AddressAttribute addressAttribute = new AddressAttribute();
        addressAttribute.setName(friendlyName);
        for (final AttributeValue<?> attributeValue : attributeValues) {
            String value;
            if (attributeValueMarshaller instanceof AbstractPostalAddressAttributeValueMarshaller) {
                value = getAddressAttributeValue(attributeValueMarshaller, attributeValue);
            } else {
                value = getAttributeValueMarshaller(attributeValueMarshaller, attributeValue);
            }
            if (!value.contains("::"))
                throw new ApplicationSpecificIDPException("Invalid Address Value", "Address must have at least one :: separator");

            Map<String, String> complexAddressAttributeMap = new HashMap<>();
            for (String token : value.split("::")) {
                String[] keyValue = token.split("=");
                complexAddressAttributeMap.put(keyValue[0].toLowerCase(), keyValue[1]);
            }


            Iterator<Map.Entry<String, String>> iterator = complexAddressAttributeMap.entrySet().iterator();
            String[] addressFields = getAddressFields();

            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                if (!Arrays.asList(addressFields).contains(pair.getKey()))
                    throw new ApplicationSpecificIDPException("Invalid Address Value", "Invalid address field :" + pair.getKey() + ". It should be one of this list: po_box, locator_designator, locator_name, thoroughfare, post_name, admin_unit_first_line, admin_unit_second_line, post_code, cv_address_area");
            }
            ComplexAddressAttribute addressId = new ComplexAddressAttribute();
            addressId.setPoBox(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.PO_BOX.getTagName().toLowerCase()));
            addressId.setLocatorDesignator(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.LOCATOR_DESIGNATOR.getTagName().toLowerCase()));
            addressId.setLocatorName(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.LOCATOR_NAME.getTagName().toLowerCase()));
            addressId.setThoroughFare(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.THOROUGHFARE.getTagName().toLowerCase()));
            addressId.setPostName(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.POST_NAME.getTagName().toLowerCase()));
            addressId.setAdminUnitFirstLine(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.ADMIN_UNIT_FIRST_LINE.getTagName().toLowerCase()));
            addressId.setAdminUnitSecondLine(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.ADMIN_UNIT_SECOND_LINE.getTagName().toLowerCase()));
            addressId.setPostCode(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.POST_CODE.getTagName().toLowerCase()));
            addressId.setAddressArea(complexAddressAttributeMap.get(AbstractPostalAddressAttributeValueMarshaller.Tag.CV_ADDRESS_AREA.getTagName().toLowerCase()));
            addressAttribute.setValue(addressId);
        }

        return addressAttribute;
    }

    private DateAttribute translateDateAttribute(AttributeValueMarshaller<?> attributeValueMarshaller, String friendlyName, ImmutableSet<? extends AttributeValue<?>> attributeValues) {

        DateAttribute dateAttribute = new DateAttribute();
        dateAttribute.setName(friendlyName);
        for (final AttributeValue<?> attributeValue : attributeValues) {
            String valueString = getAttributeValueMarshaller(attributeValueMarshaller, attributeValue);
            try {
                DateAdapter dateAdapter = new DateAdapter();
                dateAttribute.setValue(dateAdapter.unmarshal(valueString));
            } catch (Exception e) {
                logger.error("Exception {}", e);
            }
        }
        return dateAttribute;
    }

    private String getAttributeValueMarshaller(AttributeValueMarshaller<?> attributeValueMarshaller, AttributeValue<?> attributeValue) {
        String valueString = null;
        try {
            valueString = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
        } catch (AttributeValueMarshallingException e) {
            throw new IllegalStateException(e);
        }
        return valueString;
    }

    private String sendErrorJsonRedirect(String requestId,
                                         String subStatusCode,
                                         String message) {
        String responseJson = null;
        try {
            Response responseObj = new Response();
            responseObj.setId(UUID.randomUUID().toString());
            responseObj.setInResponseTo(requestId);
            responseObj.setIssuer(getIssuer());
            ResponseStatus responseStatus = new ResponseStatus();
            responseStatus.setStatusCode(StatusCodeTranslator.RESPONDER_FAILURE.stringSmsspStatusCode());
            responseStatus.setSubStatusCode(subStatusCode);
            responseStatus.setStatusMessage(message);
            responseObj.setStatus(responseStatus);

            responseJson = new SimpleProtocolProcess().convert2Json(responseObj);
            responseJson = EidasStringUtil.encodeToBase64(responseJson.getBytes(Charset.forName(UTF_8)));
        } catch (Exception ex) {
            throw new InternalErrorEIDASException("0", "Error generating SMSSPToken", ex);
        }
        return responseJson;
    }

    private String getAddressAttributeValue(AttributeValueMarshaller<?> attributeValueMarshaller, AttributeValue<?> attributeValue) {
        return ((AbstractPostalAddressAttributeValueMarshaller) attributeValueMarshaller).getAddressAsString(attributeValue);
    }

    private String[] getAddressFields() {
        return new String[]{AbstractPostalAddressAttributeValueMarshaller.Tag.PO_BOX.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.LOCATOR_DESIGNATOR.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.LOCATOR_NAME.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.THOROUGHFARE.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.POST_NAME.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.ADMIN_UNIT_FIRST_LINE.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.ADMIN_UNIT_SECOND_LINE.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.POST_CODE.getTagName().toLowerCase(),
                AbstractPostalAddressAttributeValueMarshaller.Tag.CV_ADDRESS_AREA.getTagName().toLowerCase()};
    }

    public String getSmsspToken() {
        return smsspToken;
    }

    public void setSmsspToken(String smsspToken) {
        this.smsspToken = smsspToken;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the callback
     */
    public String getCallback() {
        return callback;
    }

    /**
     * @param callback the callback to set
     */
    public void setCallback(String callback) {
        this.callback = callback;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    @SuppressWarnings("squid:S1186")
    public void setServletResponse(HttpServletResponse response) {
    }
}
