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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_HIGH;
import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_LOW;

/**
 * AbstractAuthenticationRequestTest
 * Collection of tests not covered in the Builder/fields related tests for {@link AbstractAuthenticationRequest}
 */
public class AbstractAuthenticationRequestTest {

    /**
     * Test method for
     * {@link AbstractAuthenticationRequest#equals(Object)}
     * x1          x2          nullable        non-nullable
     * value1      value1      T               T
     * value1      value2      F               F
     * null        null        T               E
     * value1      null        F               E
     * <p>
     * EIDINT-7522 Ignored pending rework to make code testable without use of reflection
     */
    @Ignore
    @Test
    public void testEquals() {
        final Set<String> attributesToTest = new HashSet<>();
        attributesToTest.add("assertionConsumerServiceURL");
        attributesToTest.add("binding");
        attributesToTest.add("destination");
        attributesToTest.add("originalIssuer");
        attributesToTest.add("serviceProviderCountryCode");
        attributesToTest.add("originCountryCode");

        Arrays.stream(AbstractAuthenticationRequest.class.getDeclaredFields())
                .filter(field -> attributesToTest.contains(field.getName()))
                .forEach(field -> {
                    final Object v1 = "ExampleValue1";
                    final Object v2 = "Examplevalue2";

                    if (!Arrays.stream(field.getAnnotationsByType(Nonnull.class)).findAny().isPresent()) {
                        assertFieldsAreEqual(field, null, null);
                        assertFieldsAreDistinct(field, v1, null);
                    }
                    assertFieldsAreEqual(field, v1, v1);
                    assertFieldsAreDistinct(field, v1, v2);
                });
    }

    /**
     * Test Method for
     * {@link AbstractAuthenticationRequest#equals(Object)}
     * when Object is not the same class
     */
    @Test
    public void testEqualsDifferentClass() {
        final AbstractAuthenticationRequest.AbstractBuilder builder =
                AbstractAuthenticationRequestBuilderTest.createEmptyAuthenticationRequestBuilder();
        AbstractAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final IAuthenticationRequest x = builder.build();
        final IAuthenticationRequest y = Mockito.mock(IAuthenticationRequest.class);
        Assert.assertFalse(x.equals(y));
        Assert.assertNotEquals(x.hashCode(), y.hashCode());
    }

    /**
     * Test Method for
     * {@link AbstractAuthenticationRequest#equals(Object)}
     * when lightRequests are equal/not equal respectively AbstractAuthenticationRequest.equals must be true/false
     * <p>
     * Must succeed
     */
    @Test
    public void testLightRequestEqualsDifferentNotifiedLevelsOfAssurance() {
        final LightRequest lightRequest1 = new LightRequest.Builder()
                .id(AbstractAuthenticationRequestConstants.SAMLID)
                .issuer(AbstractAuthenticationRequestConstants.ISSUER)
                .citizenCountryCode(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE_NOTIFIED_LOW)
                .build();
        final LightRequest lightRequest2 = new LightRequest.Builder()
                .id(AbstractAuthenticationRequestConstants.SAMLID)
                .issuer(AbstractAuthenticationRequestConstants.ISSUER)
                .citizenCountryCode(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH)
                .build();

        final AbstractAuthenticationRequest.AbstractBuilder builder1 = AbstractAuthenticationRequestBuilderTest.createEmptyAuthenticationRequestBuilder();
        AbstractAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder1);
        builder1.lightRequest(lightRequest1);
        final IAuthenticationRequest authenticationRequest1 = builder1.build();

        final AbstractAuthenticationRequest.AbstractBuilder builder2 = AbstractAuthenticationRequestBuilderTest.createEmptyAuthenticationRequestBuilder();
        AbstractAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder2);
        builder2.lightRequest(lightRequest2);
        final IAuthenticationRequest authenticationRequest2 = builder2.build();

        Assert.assertTrue("Similar lightRequest field has failed to return true on equals",
                authenticationRequest1.equals(authenticationRequest1)
                        && authenticationRequest1.hashCode() == authenticationRequest1.hashCode());

        Assert.assertFalse("Distinct lightRequest field has failed to return false on equals",
                authenticationRequest1.equals(authenticationRequest2)
                        && authenticationRequest1.hashCode() == authenticationRequest2.hashCode());
    }

    /**
     * Test Method for
     * {@link AbstractAuthenticationRequest#equals(Object)}
     * when Object has the same reference
     */
    @Test
    public void reflexiveEquals() {
        final AbstractAuthenticationRequest.AbstractBuilder builder =
                AbstractAuthenticationRequestBuilderTest.createEmptyAuthenticationRequestBuilder();
        AbstractAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final IAuthenticationRequest x = builder.build();
        Assert.assertTrue(x.equals(x));
    }

    private void assertFieldsAreEqual(Field field, Object v1, Object v2) {
        String message = "Similar fields have failed to return true on equals"; // field.getName()
        Assert.assertTrue(message, isTestEquals(field.getName(), v1, v2));
    }

    private void assertFieldsAreDistinct(Field field, Object v1, Object v2) {
        String message = "Distinct nullable field has failed to return false on equals"; // field.getName()
        Assert.assertFalse(message, isTestEquals(field.getName(), v1, v2));
    }

    private boolean isTestEquals(String field, Object v1, Object v2) {
        final AbstractAuthenticationRequest.AbstractBuilder builder =
                AbstractAuthenticationRequestBuilderTest.createEmptyAuthenticationRequestBuilder();
        AbstractAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final AbstractAuthenticationRequest x = (AbstractAuthenticationRequest) builder.build();
        final AbstractAuthenticationRequest y = (AbstractAuthenticationRequest) builder.build();

        return x.equals(y) && y.equals(x) && (x.hashCode() == y.hashCode());
    }
}
