/*
 * Copyright (c) 2019 by European Commission
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

package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.auth.engine.core.eidas.spec.EidasSAMLFormat;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.saml.saml2.core.NameID;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

public class AssertionUtilTest {

    private static final String GET_NAME_ID_METHOD_NAME = "getNameID";

    @BeforeClass
    public static void setup() throws InitializationException {
        if (null == ConfigurationService.get(XMLObjectProviderRegistry.class)) {
            InitializationService.initialize();
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the private static AssertionUtil#getNameId method
     * with transient as nameId format in request
     * and persistent as nameId format in response.
     *
     * Must throw an exception
     */
    @Test
    public void testGetNameIdWithTransientNameIdFormatInRequest() throws Throwable {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(EidasErrorKey.IDP_SAML_RESPONSE.errorCode());

        boolean isFailure = false;
        String subject = "test";
        String unspecifiedNameId = SamlNameIdFormat.TRANSIENT.getNameIdFormat();
        String persistentNameId = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder().build();
        String responder = "responder";
        SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();
        Object[] parameters = getParametersArrayForGetNameIdMethod(isFailure, subject, unspecifiedNameId,
                persistentNameId, attributeMap, responder, extensionFormat);

        invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);
    }

    /**
     * Test the private static AssertionUtil#getNameId method
     * with persistent as nameId format in request
     * and persistent as nameId format in response.
     *
     * Must succeed
     */
    @Test
    public void testGetNameIdWithPersistentNameIdFormatInRequest() throws Throwable {
        boolean isFailure = false;
        String subject = "test";
        String unspecifiedNameId = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        String persistentNameId = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder().build();
        String responder = "responder";
        SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();
        Object[] parameters = getParametersArrayForGetNameIdMethod(isFailure, subject, unspecifiedNameId,
                persistentNameId, attributeMap, responder, extensionFormat);

        Object actualResult = invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);

        String spNameQualifier = "";
        NameID expectedNameID = BuilderFactoryUtil.generateNameID(responder, persistentNameId, spNameQualifier);
        expectedNameID.setValue(subject);

        assertTrue(NameID.class.isInstance(actualResult));

        NameID actualNameId = (NameID) actualResult;
        Assert.assertEquals(expectedNameID.getFormat(), actualNameId.getFormat());
        Assert.assertEquals(expectedNameID.getNameQualifier(), actualNameId.getNameQualifier());
        Assert.assertEquals(expectedNameID.getValue(), actualNameId.getValue());
        Assert.assertEquals(expectedNameID.getSPNameQualifier(), actualNameId.getSPNameQualifier());
    }

    /**
     * Test the private static AssertionUtil#getNameId method
     * with unspecified as nameId format in request
     * and persistent as nameId format in response.
     *
     * Must succeed
     */
    @Test
    public void testGetNameIdWithUnspecifiedNameIdFormatInRequest() throws Throwable {
        boolean isFailure = false;
        String subject = "test";
        String unspecifiedNameId = SamlNameIdFormat.UNSPECIFIED.getNameIdFormat();
        String persistentNameId = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder().build();
        String responder = "responder";
        SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();
        Object[] parameters = getParametersArrayForGetNameIdMethod(isFailure, subject, unspecifiedNameId,
                persistentNameId, attributeMap, responder, extensionFormat);

        Object actualResult = invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);

        String spNameQualifier = "";
        NameID expectedNameID = BuilderFactoryUtil.generateNameID(responder, persistentNameId, spNameQualifier);
        expectedNameID.setValue(subject);

        assertTrue(NameID.class.isInstance(actualResult));

        NameID actualNameId = (NameID) actualResult;
        Assert.assertEquals(expectedNameID.getFormat(), actualNameId.getFormat());
        Assert.assertEquals(expectedNameID.getNameQualifier(), actualNameId.getNameQualifier());
        Assert.assertEquals(expectedNameID.getValue(), actualNameId.getValue());
        Assert.assertEquals(expectedNameID.getSPNameQualifier(), actualNameId.getSPNameQualifier());
    }

    /**
     * Test method for {@link AssertionUtil#isEnableAddressAttributeSubjectConfirmationData()}.
     * when {@link AssertionUtil#setEnableAddressAttributeSubjectConfirmationData(boolean)} was called with true as parameter
     *
     * Must succeed.
     */
    @Test
    public void setEnableAddressAttributeSubjectConfirmationDataEnabled() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        AssertionUtil assertionUtil = createAssertionUtilInstanceFromPrivateConstuctor();

        assertionUtil.setEnableAddressAttributeSubjectConfirmationData(Boolean.TRUE);
        boolean actualEnableAddressAttributeSubjectConfirmationData = assertionUtil.isEnableAddressAttributeSubjectConfirmationData();

        assertTrue(actualEnableAddressAttributeSubjectConfirmationData);
    }

    /**
     * Test method for {@link AssertionUtil#isEnableAddressAttributeSubjectConfirmationData()}.
     *
     * when {@link AssertionUtil#setEnableAddressAttributeSubjectConfirmationData(boolean)} was called with false as parameter
     *
     * Must succeed.
     */
    @Test
    public void setEnableAddressAttributeSubjectConfirmationDataDisabled() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        AssertionUtil assertionUtil = createAssertionUtilInstanceFromPrivateConstuctor();

        assertionUtil.setEnableAddressAttributeSubjectConfirmationData(Boolean.FALSE);
        boolean actualEnableAddressAttributeSubjectConfirmationData = assertionUtil.isEnableAddressAttributeSubjectConfirmationData();

        Assert.assertFalse(actualEnableAddressAttributeSubjectConfirmationData);
    }

    private AssertionUtil createAssertionUtilInstanceFromPrivateConstuctor() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<AssertionUtil> constructor = AssertionUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static Class[] getParametersTypeArrayForGetNameIdMethod() {
        Class[] typeOfParameters = new Class[7];
        typeOfParameters[0] = boolean.class;
        typeOfParameters[1] = String.class;
        typeOfParameters[2] = String.class;
        typeOfParameters[3] = String.class;
        typeOfParameters[4] = ImmutableAttributeMap.class;
        typeOfParameters[5] = String.class;
        typeOfParameters[6] = SAMLExtensionFormat.class;
        return typeOfParameters;
    }

    private Object[] getParametersArrayForGetNameIdMethod(boolean isFailure, String subject, String requestFormat,
            String responseFormat, ImmutableAttributeMap attributeMap, String responder, SAMLExtensionFormat extensionFormat) {
        Object[] parameters = new Object[7];
        parameters[0] = isFailure;
        parameters[1] = subject;
        parameters[2] = requestFormat;
        parameters[3] = responseFormat;
        parameters[4] = attributeMap;
        parameters[5] = responder;
        parameters[6] = extensionFormat;
        return parameters;
    }

    /**
     * Execute methods using reflection
     * @param object the object or the class of the method to execute
     * @param methodName the method name
     * @param classes the array of types of the parameters.
     * @param parameters the array of parameters needed for the execution.
     * @return the result of the method invocation.
     * @throws Throwable a NoSuchMethodException if the method could not be found
     *      or any throwable that the method invoked could throw if it throws something.
     */
    public static Object invoke(Object object, String methodName, Class[] classes, Object[] parameters) throws Throwable {
        Method method;
        if (object instanceof Class) {
            method = ((Class) object).getDeclaredMethod(methodName, classes);
        } else {
            method = object.getClass().getDeclaredMethod(methodName, classes);
        }
        method.setAccessible(true);
        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(methodName + " could not been invoked for " + object, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
