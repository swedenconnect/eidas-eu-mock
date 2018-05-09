package member_country_specific.sp;

import com.google.common.collect.ImmutableSortedSet;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.SimpleProtocol.*;
import eu.eidas.SimpleProtocol.adapter.DateAdapter;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.*;

/**
 * This Action recives a SAML Response, shows it to the user and then validates it getting the attributes values
 *
 * @author iinigo
 */
public class ReturnAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    static final Logger logger = LoggerFactory.getLogger(ReturnAction.class.getName());

    public static final String ACTION_POPULATE = "populate";
    private static final long serialVersionUID = 3660074009157921579L;
    private static final String ATTRIBUTES_FILENAME = "eidasAttributes.xml";

    private static final AttributeRegistry coreAttributeRegistry = AttributeRegistries.fromFile(ATTRIBUTES_FILENAME, null);

    private static final String ADDITIONAL_ATTRIBUTES_FILENAME = "additional-attributes.xml";

    private static final AttributeRegistry coreAdditionalAttributeRegistry = AttributeRegistries.fromFile(ADDITIONAL_ATTRIBUTES_FILENAME, SPUtil.getConfigFilePath());
    public HttpServletRequest request;
    private String SMSSPResponse;
    private String smsspResponseJSON;
    private String smsspUnencryptedResponseJSON;
    private Map<AttributeDefinition<?>, Set<String>> attrJsonMap;
    private Properties configs;

    private String providerName;

    /**
     * Translates the samlResponse to XML format in order to be shown in the JSP
     *
     * @return
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
     * @return
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

        if ((jsonResponseStatus != null && "failure".equals(jsonResponseStatus))) {
            throw new ApplicationSpecificServiceException("Smssp Response is fail", jsonResponse.getStatus().getStatusMessage());
        } else {
            attrJsonMap = (jsonAttributes);
            return ACTION_POPULATE;
        }
    }

    public Map<AttributeDefinition<?>, Set<String>> mapAttributesSimpleResponseToLightResponse(Response smsspResponse) {
        final Map<AttributeDefinition<?>, Set<String>> immutableAttributeMapBuilder = new TreeMap<>();
        final List<Attribute> attributes = smsspResponse.getAttributes();

        //in case of error simple responses the attributes can be null
        if (null == attributes)
            return immutableAttributeMapBuilder;

        for (Attribute attribute : attributes) {
            final String name = attribute.getName();
            ImmutableSortedSet<AttributeDefinition<?>> attributeDefinition = coreAttributeRegistry.getByFriendlyName(name);

            ImmutableSortedSet<AttributeDefinition<?>> additionalAttributeDefinition =
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
                values.add(value.getAddressId());
                values.add(value.getPoBox());
                values.add(value.getLocatorDesignator());
                values.add(value.getAddressArea());
                values.add(value.getThoroughFare());
                values.add(value.getPostName());
                values.add(value.getAdminUnitFirstLine());
                values.add(value.getAdminUnitSecondLine());
                values.add(value.getPostCode());
                values.add(value.getFullCVAddress());
                values.add(value.getLocatorName());
                immutableAttributeMapBuilder.put(attributeDefinitionOut, values);
            }
        }

        return immutableAttributeMapBuilder;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void setServletResponse(HttpServletResponse response) {
    }

    public Map<AttributeDefinition<?>, Set<String>> getAttrJsonMap() {
        return attrJsonMap;
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