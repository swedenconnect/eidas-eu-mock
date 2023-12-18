/*
 * Copyright (c) 2021 by European Commission
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
import org.mockito.Mockito;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.saml.saml2.core.NameID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
     * Test for
     * {@link AssertionUtil#getNameID(boolean, String, String, String, ImmutableAttributeMap, String, SAMLExtensionFormat)}
     * with transient as nameId format in request
     * and persistent as nameId format in response.
     * <p>
     * Must fail
     */
    @Test
    public void testGetNameIdWithTransientNameIdFormatInRequest() throws Throwable {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(EidasErrorKey.IDP_SAML_RESPONSE.errorCode());

        boolean isFailure = false;
        String subject = "test";
        String requestNameIdFormatTransient = SamlNameIdFormat.TRANSIENT.getNameIdFormat();
        String responseNameIdFormatPersistent = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder().build();
        String responder = "responder";
        SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();
        Object[] parameters = getParametersArrayForGetNameIdMethod(isFailure, subject, requestNameIdFormatTransient,
                responseNameIdFormatPersistent, attributeMap, responder, extensionFormat);

        invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);
    }

    /**
     * Test for
     * {@link AssertionUtil#getNameID(boolean, String, String, String, ImmutableAttributeMap, String, SAMLExtensionFormat)}
     * with persistent as nameId format in request
     * and persistent as nameId format in response.
     *
     * Must succeed
     */
    @Test
    public void testGetNameIdWithPersistentNameIdFormatInRequest() throws Throwable {
        boolean isFailure = false;
        String subject = "test";
        String requestNameIdFormatPersistent = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        String responseNameIdFormatPersistent = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder().build();
        String responder = "responder";
        SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();
        Object[] parameters = getParametersArrayForGetNameIdMethod(isFailure, subject, requestNameIdFormatPersistent,
                responseNameIdFormatPersistent, attributeMap, responder, extensionFormat);

        Object actualResult = invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);

        String spNameQualifier = "";
        NameID expectedNameID = BuilderFactoryUtil.generateNameID(responder, responseNameIdFormatPersistent, spNameQualifier);
        expectedNameID.setValue(subject);

        assertTrue(NameID.class.isInstance(actualResult));

        NameID actualNameId = (NameID) actualResult;
        Assert.assertEquals(expectedNameID.getFormat(), actualNameId.getFormat());
        Assert.assertEquals(expectedNameID.getNameQualifier(), actualNameId.getNameQualifier());
        Assert.assertEquals(expectedNameID.getValue(), actualNameId.getValue());
        Assert.assertEquals(expectedNameID.getSPNameQualifier(), actualNameId.getSPNameQualifier());
    }

    /**
     * Test for
     * {@link AssertionUtil#getNameID(boolean, String, String, String, ImmutableAttributeMap, String, SAMLExtensionFormat)}
     * with unspecified as nameId format in request
     * and persistent as nameId format in response.
     *
     * Must succeed
     */
    @Test
    public void testGetNameIdWithUnspecifiedNameIdFormatInRequest() throws Throwable {
        boolean isFailure = false;
        String subject = "test";
        String requestNameIdFormatUnspecified = SamlNameIdFormat.UNSPECIFIED.getNameIdFormat();
        String responseNameIdFormatPersistent = SamlNameIdFormat.PERSISTENT.getNameIdFormat();
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder().build();
        String responder = "responder";
        SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();
        Object[] parameters = getParametersArrayForGetNameIdMethod(isFailure, subject, requestNameIdFormatUnspecified,
                responseNameIdFormatPersistent, attributeMap, responder, extensionFormat);

        Object actualResult = invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);

        String spNameQualifier = "";
        NameID expectedNameID = BuilderFactoryUtil.generateNameID(responder, responseNameIdFormatPersistent, spNameQualifier);
        expectedNameID.setValue(subject);

        assertTrue(NameID.class.isInstance(actualResult));

        NameID actualNameId = (NameID) actualResult;
        Assert.assertEquals(expectedNameID.getFormat(), actualNameId.getFormat());
        Assert.assertEquals(expectedNameID.getNameQualifier(), actualNameId.getNameQualifier());
        Assert.assertEquals(expectedNameID.getValue(), actualNameId.getValue());
        Assert.assertEquals(expectedNameID.getSPNameQualifier(), actualNameId.getSPNameQualifier());
    }

    /**
     * Test for
     * {@link AssertionUtil#getNameID(boolean, String, String, String, ImmutableAttributeMap, String, SAMLExtensionFormat)}
     * without nameId format in request and without nameId format in response.
     * using {@link EidasSAMLFormat} as extensionFormat
     * <p>
     * Must succeed
     */
    @Test
    public void testGetNameIdWithMissingNameIdFormatInRequest() throws Throwable {
        final String requestNameIdFormat = null;
        final String responseNameIdFormat = null;
        final SAMLExtensionFormat extensionFormat = new EidasSAMLFormat();
        final Object[] parameters = getParametersArrayForGetNameIdMethod(
                false,
                "test",
                requestNameIdFormat,
                responseNameIdFormat,
                ImmutableAttributeMap.builder().build(),
                "responder",
                extensionFormat);
        final Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();

        Object actualResult = invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);

        assertTrue(actualResult instanceof NameID);
        final NameID actualNameId = (NameID) actualResult;
        Assert.assertEquals(SamlNameIdFormat.PERSISTENT.toString(), actualNameId.getFormat());
    }

    /**
     * Test for
     * {@link AssertionUtil#getNameID(boolean, String, String, String, ImmutableAttributeMap, String, SAMLExtensionFormat)}
     * without nameId format in request and without nameId format in response.
     * not using {@link EidasSAMLFormat} as extensionFormat
     * <p>
     * Must succeed
     */
    @Test
    public void testGetNameIdWithMissingNameIdFormatOtherSamlFormat() throws Throwable {
        final String requestNameIdFormat = null;
        final String responseNameIdFormat = null;
        final SAMLExtensionFormat extensionFormat = Mockito.mock(SAMLExtensionFormat.class);
        final Object[] parameters = getParametersArrayForGetNameIdMethod(
                false,
                "test",
                requestNameIdFormat,
                responseNameIdFormat,
                ImmutableAttributeMap.builder().build(),
                "responder",
                extensionFormat);
        final Class[] parametersTypes = getParametersTypeArrayForGetNameIdMethod();

        final Object actualResult = invoke(AssertionUtil.class, GET_NAME_ID_METHOD_NAME, parametersTypes, parameters);

        assertTrue(actualResult instanceof NameID);
        final NameID actualNameId = (NameID) actualResult;
        Assert.assertEquals(SamlNameIdFormat.UNSPECIFIED.toString(), actualNameId.getFormat());
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
