/*
 * Copyright (c) 2024 by European Commission
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

package member_country_specific.specific.connector.communication;

import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.ImmutableValueMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.tx.CorrelationMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SpecificConnectorTest {

    private SpecificConnector specificConnector;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        specificConnector = new SpecificConnector();

        String path = new File("").getAbsolutePath();
        int lastPathSeparatorIndex = path.lastIndexOf(File.separator);
        String basePath = new StringBuilder(path)
                .delete(lastPathSeparatorIndex, path.length())
                .append("/EIDAS-Config/server/specificConnector")
                .toString();

        specificConnector.setSpecificMSSpRequestCorrelationMap(mock(CorrelationMap.class));
        specificConnector.setIssuerName("dummy");
        specificConnector.setEidasAttributesFile(basePath + "/eidas-attributes.xml");
        specificConnector.setAdditionalAttributesFile(basePath + "/additional-attributes.xml");
    }

    @Test
    public void whenTypeIs_RequestedAttribute_NoValueIsParsed() throws JAXBException {

        final String ATTRIBUTE_NAME_UNDER_TEST = "TaxReference";
        String testAttributeLine = "{ \"type\" : \"requested_attribute\", \"name\" : \"" + ATTRIBUTE_NAME_UNDER_TEST + "\",  \"value\" : \"TEST\", \"required\" : false }";
        String specificRequestBase64 = testJsonBase64(testAttributeLine);

        ILightRequest iLightRequest = specificConnector.translateSpecificRequest(specificRequestBase64);

        ImmutableAttributeMap requestedAttributes = iLightRequest.getRequestedAttributes();
        ImmutableValueMap valuesByFriendlyName = requestedAttributes.getValuesByFriendlyName(ATTRIBUTE_NAME_UNDER_TEST);

        assertNotNull(valuesByFriendlyName);
        assertFalse(valuesByFriendlyName.isEmpty());

        AttributeDefinition<?> attributeDefinition = valuesByFriendlyName.getDefinitions().stream()
                .filter(definition -> ATTRIBUTE_NAME_UNDER_TEST.equals(definition.getFriendlyName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(ATTRIBUTE_NAME_UNDER_TEST + " not present in attribute definitions"));


        Set<?> values = valuesByFriendlyName.getValues(attributeDefinition);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void whenTypeIs_String_ValueIsParsed() throws JAXBException {

        final String ATTRIBUTE_NAME_UNDER_TEST = "TaxReference";
        String testAttributeLine = "{ \"type\" : \"string\", \"name\" : \"" + ATTRIBUTE_NAME_UNDER_TEST + "\",  \"value\" : \"TEST\", \"required\" : false }";
        String specificRequestBase64 = testJsonBase64(testAttributeLine);

        ILightRequest iLightRequest = specificConnector.translateSpecificRequest(specificRequestBase64);

        ImmutableAttributeMap requestedAttributes = iLightRequest.getRequestedAttributes();
        ImmutableValueMap valuesByFriendlyName = requestedAttributes.getValuesByFriendlyName(ATTRIBUTE_NAME_UNDER_TEST);

        assertNotNull(valuesByFriendlyName);
        assertFalse(valuesByFriendlyName.isEmpty());

        AttributeDefinition<?> attributeDefinition = valuesByFriendlyName.getDefinitions().stream()
                .filter(definition -> ATTRIBUTE_NAME_UNDER_TEST.equals(definition.getFriendlyName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(ATTRIBUTE_NAME_UNDER_TEST + " not present in attribute definitions"));


        Set<?> values = valuesByFriendlyName.getValues(attributeDefinition);
        assertNotNull(values);
        assertTrue(values.contains("TEST"));
    }

    @Test
    public void whenTypeIs_String_ButName_LegalAddress_ValueIsNotParsed() throws JAXBException {

        final String ATTRIBUTE_NAME_UNDER_TEST = "LegalAddress";
        String testAttributeLine = "{ \"type\" : \"string\", \"name\" : \"" + ATTRIBUTE_NAME_UNDER_TEST + "\",  \"value\" : \"TEST\", \"required\" : false }";
        String specificRequestBase64 = testJsonBase64(testAttributeLine);

        ILightRequest iLightRequest = specificConnector.translateSpecificRequest(specificRequestBase64);

        ImmutableAttributeMap requestedAttributes = iLightRequest.getRequestedAttributes();
        ImmutableValueMap valuesByFriendlyName = requestedAttributes.getValuesByFriendlyName(ATTRIBUTE_NAME_UNDER_TEST);

        assertNotNull(valuesByFriendlyName);
        assertFalse(valuesByFriendlyName.isEmpty());

        AttributeDefinition<?> attributeDefinition = valuesByFriendlyName.getDefinitions().stream()
                .filter(definition -> ATTRIBUTE_NAME_UNDER_TEST.equals(definition.getFriendlyName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(ATTRIBUTE_NAME_UNDER_TEST + " not present in attribute definitions"));


        Set<?> values = valuesByFriendlyName.getValues(attributeDefinition);
        assertNotNull(values);
        assertFalse(values.contains("TEST"));
        assertTrue(values.stream().anyMatch(value -> value instanceof PostalAddress));
    }

    /**
     * Test method for
     * {@link SpecificConnector#translateLightRequestAttributes(ILightResponse)}
     * when light response contains a map with a person identifier attribute
     * <p/>
     * Must succeed.
     */
    @Test
    public void testTranslateLightRequestAttributes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method translateLightRequestAttributes = SpecificConnector.class.getDeclaredMethod(
                "translateLightRequestAttributes", ILightResponse.class);
        translateLightRequestAttributes.setAccessible(true);

        final ILightResponse mockILightResponse = Mockito.mock(ILightResponse.class);
        final ImmutableAttributeMap immutableAttributeMap = ImmutableAttributeMap.of(EidasSpec.Definitions.PERSON_IDENTIFIER,
                new StringAttributeValue("testData12345"));
        Mockito.when(mockILightResponse.getAttributes()).thenReturn(immutableAttributeMap);

        List<Attribute> attributes = (List<Attribute>) translateLightRequestAttributes.invoke(specificConnector, mockILightResponse);
        Assert.assertFalse(attributes.isEmpty());
    }

    /**
     * Test method for
     * {@link SpecificConnector#translateLightRequestAttributes(ILightResponse)}
     * when light response contains an empty map
     * <p/>
     * Must succeed.
     */
    @Test
    public void testTranslateLightRequestAttributesWithEmptyMap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method translateLightRequestAttributes = SpecificConnector.class.getDeclaredMethod(
                "translateLightRequestAttributes", ILightResponse.class);
        translateLightRequestAttributes.setAccessible(true);

        final ILightResponse mockILightResponse = Mockito.mock(ILightResponse.class);
        Mockito.when(mockILightResponse.getAttributes()).thenReturn(ImmutableAttributeMap.of());

        List<Attribute> attributes = (List<Attribute>) translateLightRequestAttributes.invoke(specificConnector, mockILightResponse);
        Assert.assertTrue(attributes.isEmpty());
    }

    private String testJsonBase64(String testAttributeLine) {
        return EidasStringUtil.encodeToBase64("{\n" +
                " \"authentication_request\" : {\n" +
                " \"attribute_list\" : [ \n" +
                "{ \"type\" : \"requested_attribute\", \"name\" : \"EORI\", \"required\" : false }, " +
                testAttributeLine +
                "],\n" +
                " \"requested_authentication_context\" : \n" +
                "{ \"comparison\" : \"minimum\", \"context_class\" : [ \"A\" ] } \n" +
                ",\n" +
                " \"citizen_country\" : \"CB\",\n" +
                " \"created_on\" : \"2019-05-13T15:51:12.521+02:00\",\n" +
                " \"force_authentication\" : true,\n" +
                " \"id\" : \"abd3767b-fbed-4c73-b200-db42b719a251\",\n" +
                " \"name_id_policy\" : \"unspecified\",\n" +
                " \"provider_name\" : \"DEMO-SP-CB\",\n" +
                " \"serviceUrl\" : \"http://cef-eid-build-1:8081/SP/ReturnPage\",\n" +
                " \"sp_type\" : \"public\",\n" +
                " \"version\" : \"1\"\n" +
                " }\n" +
                " }");
    }

}