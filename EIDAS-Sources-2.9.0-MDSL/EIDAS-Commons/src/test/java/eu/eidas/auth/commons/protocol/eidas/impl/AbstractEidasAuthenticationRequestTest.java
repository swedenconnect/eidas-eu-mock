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

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AbstractEidasAuthenticationRequestTest
 * Collection of tests not covered in the Builder/fields related tests for {@link AbstractEidasAuthenticationRequest}
 */
public class AbstractEidasAuthenticationRequestTest {

    /**
     * Test method for
     * {@link AbstractEidasAuthenticationRequest#equals(Object)}
     * for every attribute available on class (using reflection)
     * x1          x2          nullable        non-nullable
     * value1      value1      T               T
     * value1      value2      F               F
     * null        null        T               E
     * value1      null        F               E
     *
     * EIDINT-7522 Ignored pending rework to make code testable without use of reflection
     */
    @Ignore
    @Test
    public void testEquals() {
        final Set<String> attributesToTest = new HashSet<>();
        attributesToTest.add("levelOfAssurance");
        attributesToTest.add("nonNotifiedLevelsOfAssurance");
        attributesToTest.add("levelOfAssuranceComparison");

        Arrays.stream(AbstractEidasAuthenticationRequest.class.getDeclaredFields())
                .filter(field -> attributesToTest.contains(field.getName()))
                .forEach(field -> {
                    final Map<String, List<Object>> exampleValues = LevelOfAssuranceTestConstants.lookup;
                    final Object v1 = exampleValues.get(field.getName()).get(0);
                    final Object v2 = exampleValues.get(field.getName()).get(1);

                    if (!Arrays.stream(field.getAnnotationsByType(Nonnull.class)).findAny().isPresent()) {
                        assertFieldsAreEqual(field, null, null);
                        assertFieldsAreDistinct(field, v1, null);
                    }
                    assertFieldsAreEqual(field, v1, v1);
                    assertFieldsAreDistinct(field, v1, v2);
                });
    }

    private void assertFieldsAreEqual(Field field, Object v1, Object v2) {
        String message = "Similar fields have failed to return true on equals"; // field.getName()
        Assert.assertTrue(message, isTestEquals(field.getName(), v1, v2));
    }

    private void assertFieldsAreDistinct(Field field, Object v1, Object v2) {
        String message = "Distinct nullable field has failed to return false on equals"; // field.getName()
        Assert.assertFalse(message, isTestEquals(field.getName(), v1, v2));
    }

    /**
     * Test Method for
     * {@link AbstractEidasAuthenticationRequest#equals(Object)}
     * when Object has the same reference
     */
    @Test
    public void testEqualsReflexive() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder =
                AbstractEidasAuthenticationRequestBuilderTest.createEmptyEidasAuthenticationRequestBuilder();
        AbstractEidasAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final IAuthenticationRequest x = builder.build();
        Assert.assertTrue(x.equals(x));
        Assert.assertEquals(x.hashCode(), x.hashCode());
    }

    /**
     * Test Method for
     * {@link AbstractEidasAuthenticationRequest#equals(Object)}
     * when Object is the identical but different reference
     */
    @Test
    public void testEqualsSameBuilder() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder =
                AbstractEidasAuthenticationRequestBuilderTest.createEmptyEidasAuthenticationRequestBuilder();
        AbstractEidasAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final IAuthenticationRequest x = builder.build();
        final IAuthenticationRequest y = builder.build();
        Assert.assertTrue(x.equals(y));
        Assert.assertEquals(x.hashCode(), y.hashCode());
    }

    /**
     * Test Method for
     * {@link AbstractEidasAuthenticationRequest#equals(Object)}
     * when Object is not the same class
     */
    @Test
    public void testEqualsDifferentClass() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder =
                AbstractEidasAuthenticationRequestBuilderTest.createEmptyEidasAuthenticationRequestBuilder();
        AbstractEidasAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final IAuthenticationRequest x = builder.build();
        final IAuthenticationRequest y = Mockito.mock(IAuthenticationRequest.class);
        Assert.assertFalse(x.equals(y));
        Assert.assertNotEquals(x.hashCode(), y.hashCode());
    }

    /**
     * Test Method for
     * {@link AbstractEidasAuthenticationRequest#equals(Object)}
     * when Object differs with attributes from superclass
     */
    @Test
    public void testEqualsDifferentSuperAttributes() {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder =
                AbstractEidasAuthenticationRequestBuilderTest.createEmptyEidasAuthenticationRequestBuilder();
        AbstractEidasAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final IAuthenticationRequest x = builder.build();
        final IAuthenticationRequest y = builder.serviceProviderCountryCode("Country B").build();
        Assert.assertFalse(x.equals(y));
        Assert.assertNotEquals(x.hashCode(), y.hashCode());
    }

    private boolean isTestEquals(String field, Object v1, Object v2) {
        final AbstractEidasAuthenticationRequest.AbstractBuilder builder =
                AbstractEidasAuthenticationRequestBuilderTest.createEmptyEidasAuthenticationRequestBuilder();
        AbstractEidasAuthenticationRequestBuilderTest.populateMinimalBuilderFields(builder);
        final AbstractEidasAuthenticationRequest x = (AbstractEidasAuthenticationRequest) builder.build();
        final AbstractEidasAuthenticationRequest y = (AbstractEidasAuthenticationRequest) builder.build();

        //ReflectionUtil.setFinalFieldValue(AbstractEidasAuthenticationRequest.class, x, field, v1);
        //ReflectionUtil.setFinalFieldValue(AbstractEidasAuthenticationRequest.class, y, field, v2);
        return x.equals(y) && y.equals(x) && (x.hashCode() == y.hashCode());
    }
}
