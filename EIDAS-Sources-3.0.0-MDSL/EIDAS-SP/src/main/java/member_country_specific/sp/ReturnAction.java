/*
 * Copyright (c) 2025 by European Commission
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
package member_country_specific.sp;

import eu.eidas.SimpleProtocol.AddressAttribute;
import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import eu.eidas.SimpleProtocol.DateAttribute;
import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.StringAttribute;
import eu.eidas.SimpleProtocol.StringListAttribute;
import eu.eidas.SimpleProtocol.StringListValue;
import eu.eidas.SimpleProtocol.adapter.DateAdapter;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.SimpleProtocol.utils.StatusCodeTranslator;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.action.Action;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.action.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * This Action recives a SAML Response, shows it to the user and then validates it getting the attributes values
 */
public class ReturnAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    static final Logger logger = LoggerFactory.getLogger(ReturnAction.class.getName());

    public static final String ACTION_POPULATE = "populate";
    private static final long serialVersionUID = 3660074009157921579L;
    private static final String ATTRIBUTES_FILENAME = "eidas-attributes.xml";

    private static final AttributeRegistry coreAttributeRegistry = AttributeRegistries.fromFile(ATTRIBUTES_FILENAME, SPUtil.getConfigFilePath());

    private static final String ADDITIONAL_ATTRIBUTES_FILENAME = "additional-attributes.xml";

    private static final AttributeRegistry coreAdditionalAttributeRegistry = AttributeRegistries.fromFile(ADDITIONAL_ATTRIBUTES_FILENAME, SPUtil.getConfigFilePath());
    public HttpServletRequest request;
    private String SMSSPResponse;
    private String smsspResponseJSON;
    private String smsspUnencryptedResponseJSON;
    private Map<AttributeDefinition<?>, Set<String>> attrJsonMap;
    private Map<String, Set<String>> attrJsonMapForDisplay;
    private Properties configs;

    private String providerName;

    /**
     * Translates the samlResponse to XML format in order to be shown in the JSP
     *
     * @return Action.SUCCESS
     */

    @Override
    public String execute() {

        configs = SPUtil.loadSPConfigs();

        providerName = configs.getProperty(Constants.PROVIDER_NAME);

        byte[] decSmsspToken;

        decSmsspToken = EidasStringUtil.decodeBytesFromBase64(SMSSPResponse);
        smsspResponseJSON = EidasStringUtil.toString(decSmsspToken);
        smsspUnencryptedResponseJSON = smsspResponseJSON;

        return Action.SUCCESS;
    }

    /**
     * Validates the request and displays the value of the requested attributes
     *
     * @return ACTION_POPULATE
     * @throws JAXBException if the MS Specific Request could not be unmarshalled
     */
    public String populate() throws JAXBException {

        configs = SPUtil.loadSPConfigs();

        Response jsonResponse = null;
        String jsonResponseStatus = null;
        Map<AttributeDefinition<?>, Set<String>> jsonAttributes = null;

        SimpleProtocolProcess simpleProtocolProcess = new SimpleProtocolProcess();

        StringReader stringReaderJson = new StringReader(SMSSPResponse);
        jsonResponse = simpleProtocolProcess.convertFromJson(stringReaderJson, Response.class);
        jsonResponseStatus = jsonResponse.getStatus().getStatusCode();

        jsonAttributes = mapAttributesSimpleResponseToLightResponse(jsonResponse);

        if ((jsonResponseStatus != null && isStatusFailure(jsonResponseStatus))) {
            throw new ApplicationSpecificServiceException("Smssp Response is fail", jsonResponse.getStatus().getStatusMessage());
        } else {
            attrJsonMap = (jsonAttributes);
            this.attrJsonMapForDisplay = convertToFriendlyNameMap(jsonAttributes);
            return ACTION_POPULATE;
        }
    }

    private boolean isStatusFailure(String jsonResponseStatus) {
        return StatusCodeTranslator.RESPONDER_FAILURE.stringSmsspStatusCode().equals(jsonResponseStatus)
                || StatusCodeTranslator.REQUESTER_FAILURE.stringSmsspStatusCode().equals(jsonResponseStatus)
                || StatusCodeTranslator.VERSION_MISMATCH.stringSmsspStatusCode().equals(jsonResponseStatus);
    }

    public Map<AttributeDefinition<?>, Set<String>> mapAttributesSimpleResponseToLightResponse(Response smsspResponse) {
        final Map<AttributeDefinition<?>, Set<String>> immutableAttributeMapBuilder = new TreeMap<>();
        final List<Attribute> attributes = smsspResponse.getAttributes();

        //in case of error simple responses the attributes can be null
        if (null == attributes)
            return immutableAttributeMapBuilder;

        for (Attribute attribute : attributes) {
            final String name = attribute.getName();
            SortedSet<AttributeDefinition<?>> attributeDefinition = coreAttributeRegistry.getByFriendlyName(name);

            SortedSet<AttributeDefinition<?>> additionalAttributeDefinition =
                    coreAdditionalAttributeRegistry.getByFriendlyName(name);

            AttributeDefinition<?> attributeDefinitionOut = attributeDefinition.isEmpty() ? additionalAttributeDefinition.first() : attributeDefinition.first();
            if (attribute instanceof StringAttribute) {

                final String value = ((StringAttribute) attribute).getValue();
                Set<String> values = new LinkedHashSet<>();
                values.add(value);
                immutableAttributeMapBuilder.put(attributeDefinitionOut, values);
            } else if (attribute instanceof DateAttribute) {
                Date value = ((DateAttribute) attribute).getValue();
                String dateAttributeValue = null;
                try {
                    DateAdapter dateAdapter = new DateAdapter();
                    dateAttributeValue = dateAdapter.marshal(value);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
                Set<String> values = new LinkedHashSet<>();
                values.add(dateAttributeValue);
                immutableAttributeMapBuilder.put(attributeDefinitionOut, values);
            } else if (attribute instanceof StringListAttribute) {
                final List<StringListValue> stringListValues = ((StringListAttribute) attribute).getValues();
                //convert to List of Strings
                final Set<String> strings = new LinkedHashSet<>();
                for (StringListValue value : stringListValues) {
                    strings.add(value.getValue());
                }
                immutableAttributeMapBuilder.put(attributeDefinitionOut, strings);
            } else if (attribute instanceof AddressAttribute) {
                final ComplexAddressAttribute value = ((AddressAttribute) attribute).getValue();
                Set<String> values = new LinkedHashSet<>();
                values.add(value.getPoBox());
                values.add(value.getLocatorDesignator());
                values.add(value.getAddressArea());
                values.add(value.getThoroughFare());
                values.add(value.getPostName());
                values.add(value.getAdminUnitFirstLine());
                values.add(value.getAdminUnitSecondLine());
                values.add(value.getPostCode());
                values.add(value.getLocatorName());
                immutableAttributeMapBuilder.put(attributeDefinitionOut, values);
            }
        }

        return immutableAttributeMapBuilder;
    }

    /**
     * Converts a map of attribute definitions to a map using friendly names as keys.
     * <p>
     * This method extracts the friendly name from each {@code AttributeDefinition<?>}
     * and maps it to the corresponding set of attribute values. The resulting map
     * is structured for easy display in the UI.
     * </p>
     *
     * @param jsonAttributes A map where keys are {@code AttributeDefinition<?>} objects
     *                       and values are sets of attribute values.
     * @return A new {@code Map<String, Set<String>>} where the keys are the friendly names
     * of the attributes, and the values are the corresponding attribute values.
     */
    private Map<String, Set<String>> convertToFriendlyNameMap(Map<AttributeDefinition<?>, Set<String>> jsonAttributes) {
        Map<String, Set<String>> displayMap = new LinkedHashMap<>();

        if (jsonAttributes != null) {
            for (Map.Entry<AttributeDefinition<?>, Set<String>> entry : jsonAttributes.entrySet()) {
                String friendlyName = entry.getKey().getFriendlyName();
                displayMap.put(friendlyName, entry.getValue());
            }
        }
        return displayMap;
    }

    @Override
    public void withServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void withServletResponse(HttpServletResponse response) {
    }

    public Map<String, Set<String>> getAttrJsonMap() {
        return attrJsonMapForDisplay;
    }

    public void setAttrJsonMap(Map<AttributeDefinition<?>, Set<String>> attrJsonMap) {
        this.attrJsonMap = attrJsonMap;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getSMSSPResponse() {
        return SMSSPResponse;
    }

    public void setSMSSPResponse(String smsspResponse) {
        this.SMSSPResponse = smsspResponse;
    }

    public String getSmsspResponseJSON() {
        return smsspResponseJSON;
    }

    public void setSmsspResponseJSON(String smsspResponseJSON) {
        this.smsspResponseJSON = smsspResponseJSON;
    }

    public String getSmsspUnencryptedResponseJSON() {
        return smsspUnencryptedResponseJSON;
    }

    public void setSmsspUnencryptedResponseJSON(String smsspUnencryptedResponseJSON) {
        this.smsspUnencryptedResponseJSON = smsspUnencryptedResponseJSON;
    }
}