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

package eu.eidas.node.auth;

import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.AbstractEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ConnectorBeanNames;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Test class for {@link EidasNodeValidationUtil}
 */
public class EidasNodeValidationUtilTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Properties properties;
    private ApplicationContext oldContext = null;

    private ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        properties = new Properties();
        mockResourceBundleMessageSource();
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Testing cases with null parameters.
     * <p>
     * Must succeed.
     */
    @Test
    public void testIsRequestLoAValid() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());
        IEidasAuthenticationRequest eidasAuthenticationRequest = mockIEidasAuthenticationRequestNullLoa();

        checkInvalidLoARequest("Null check for values", null, null);
        checkInvalidLoARequest("Null check for request", null, allowedLoAs);
        checkInvalidLoARequest("Null check for Level", eidasAuthenticationRequest, null);

        checkInvalidLoARequest("Null check for request.levelOfAssurance", eidasAuthenticationRequest, allowedLoAs);
    }


    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where requested notified LoA is lower than allowed LoA.
     * <p>
     * Must return true.
     */
    @Test
    public void testIsRequestNotifiedLoAValidLowerThanAllowed() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        checkValidLoARequest("Normal case LOW<=High (minimum)", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where requested notified LoA is equal to allowed LoA.
     * <p>
     * Must return true.
     */
    @Test
    public void testIsRequestNotifiedLoAValidEqualToAllowed() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
        checkValidLoARequest("Normal case HIGH<=High (minimum)", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where requested notified LoA is higher than allowed LoA.
     * <p>
     * Must return false.
     */
    @Test
    public void testIsRequestNotifiedLoAValidHigherThanAllowed() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue());

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
        checkInvalidLoARequest("Error case HIGH<=substantial (minimum)", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where the requested non notified LoA does not match the notified LoA in allowed LoAs.
     * <p>
     * Must return false .
     */
    @Test
    public void testIsRequestNonNotifiedLoAMismatchNotifiedInvalid() {
        List<String> allowedLoAs = Arrays.asList("http://eidas.europa.eu/NonNotified/LoA/Test");

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        checkInvalidLoARequest("Should not match: notified is different from non notified LoA",
                eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where the requested non notified LoA does not match the non notified LoA in allowed LoAs.
     * <p>
     * Must return false.
     */
    @Test
    public void testIsRequestNonNotifiedLoAMismatchNonNotifiedInValid() {
        List<String> allowedLoAs = Arrays.asList("http://eidas.europa.eu/NonNotified/LoA/Test");

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance("http://eidas.europa.eu/NonNotified/LoA/notAMatch")
                .build();
        checkInvalidLoARequest("Should not match: LoA are not the same", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where the requested non notified LoA matches the non notified LoA in allowed LoAs.
     * <p>
     * Must return true.
     */
    @Test
    public void testIsRequestNonNotifiedLoAMatchValid() {
        List<String> allowedLoAs = Arrays.asList("http://eidas.europa.eu/NonNotified/LoA/Test");

        IEidasAuthenticationRequest
                eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance("http://eidas.europa.eu/NonNotified/LoA/Test")
                .build();
        checkValidLoARequest("Should be valid for matching LoAs", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains a higher notified loa and returns true
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNotified() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.LOW.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertTrue(isBetterOrEqual);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains 1 overlapping loa and returns true
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNonNotified() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.LOW.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList("loa:nonNotifiedLoA", "loa:nonNotifiedLoA2");
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertTrue(isBetterOrEqual);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains a lower notified loa and returns false
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNotifiedNotEquals() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList(NotifiedLevelOfAssurance.LOW.stringValue());
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertFalse(isBetterOrEqual);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains no overlapping loa and returns false
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNonNotifiedNotEqual() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList("loa:Someother");
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertFalse(isBetterOrEqual);
    }

    private void checkInvalidLoARequest(String message, IEidasAuthenticationRequest request, List<String> allowedLoAs) {
        Assert.assertFalse(message, EidasNodeValidationUtil.isRequestLoAValid(request, allowedLoAs));
    }

    private void checkValidLoARequest(String message, IEidasAuthenticationRequest request, List<String> allowedLoAs) {
        Assert.assertTrue(message, EidasNodeValidationUtil.isRequestLoAValid(request, allowedLoAs));
    }

    private EidasAuthenticationRequest.Builder getEidasRequestBuilder() {
        final EidasAuthenticationRequest.Builder requestBuilder = EidasAuthenticationRequest.builder()
                .id("testId")
                .issuer("testIssuer")
                .citizenCountryCode("testCountryCode")
                .destination("testDestination");
        return requestBuilder;
    }

    private IEidasAuthenticationRequest mockIEidasAuthenticationRequestNullLoa() {
        IEidasAuthenticationRequest eidasAuthenticationRequest = Mockito.mock(AbstractEidasAuthenticationRequest.class);

        Mockito.when(eidasAuthenticationRequest.getLevelsOfAssurance()).thenReturn(null);
        Mockito.when(eidasAuthenticationRequest.getAssertionConsumerServiceURL()).thenReturn("http://sp:8080/SP/ReturnPage");
        return eidasAuthenticationRequest;
    }

    private void mockResourceBundleMessageSource() {
        final ResourceBundleMessageSource mockResourceBundleMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        Mockito.when(mockApplicationContext.getBean(ConnectorBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString())).thenReturn(mockResourceBundleMessageSource);
    }

}
