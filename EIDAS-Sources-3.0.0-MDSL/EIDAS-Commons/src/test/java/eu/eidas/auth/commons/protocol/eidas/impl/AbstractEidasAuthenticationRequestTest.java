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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * AbstractEidasAuthenticationRequestTest
 * Collection of tests not covered in the Builder/fields related tests for {@link AbstractEidasAuthenticationRequest}
 */
public class AbstractEidasAuthenticationRequestTest {

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

}
