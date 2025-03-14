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
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import org.junit.Test;

import javax.annotation.Nonnull;

import static eu.eidas.auth.commons.protocol.eidas.impl.LevelOfAssuranceTestConstants.LEVEL_OF_ASSURANCE_NOTIFIED_LOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * AbstractAuthenticationRequestBuilderTest
 * Collection of builder related tests for fields of {@link AbstractAuthenticationRequest}
 */
public class AbstractAuthenticationRequestBuilderTest {

    @Test
    public void testToString() {
        final AbstractAuthenticationRequest.AbstractBuilder builder = createEmptyAuthenticationRequestBuilder();
        final IAuthenticationRequest authenticationRequest = populateAllBuilderFields(builder).build();
        final String toString = authenticationRequest.toString();
        assertAllPopulatedBuilderFieldsAreInToString(toString);
    }

    @Test
    public void testRequestCopyBuilder() {
        final AbstractAuthenticationRequest.AbstractBuilder abstractAuthenticationRequestAbstractBuilder
                = createEmptyAuthenticationRequestBuilder();

        final IAuthenticationRequest authenticationRequest
                = populateAllBuilderFields(abstractAuthenticationRequestAbstractBuilder).build();

        final IAuthenticationRequest authenticationRequestBis
                = createAuthenticationRequestBuilderFromRequest(authenticationRequest).build();

        assertAbstractAuthenticationRequestAllFieldsAreEqual(authenticationRequest, authenticationRequestBis);
        assertEquals("AuthRequests are different according to equals()", authenticationRequest, authenticationRequestBis);
    }

    @Test
    public void testBuilderCopyConstructor() {
        final AbstractAuthenticationRequest.AbstractBuilder builder = createEmptyAuthenticationRequestBuilder();
        final IAuthenticationRequest authenticationRequest = populateAllBuilderFields(builder).build();
        final IAuthenticationRequest authenticationRequestBis = createAuthenticationRequestBuilderFromRequestBuilder(builder).build();
        assertAbstractAuthenticationRequestAllFieldsAreEqual(authenticationRequest, authenticationRequestBis);
        assertEquals("AuthRequests are different according to equals()", authenticationRequest, authenticationRequestBis);
    }

    @Test
    public void testFieldsCreation() {
        final AbstractAuthenticationRequest.AbstractBuilder builder = createEmptyAuthenticationRequestBuilder();
        final IAuthenticationRequest authenticationRequest = populateAllBuilderFields(builder).build();
        assertAbstractAuthenticationRequestAllFieldsArePresent(authenticationRequest);
    }

    @Test
    public void testMinimumDataSetCreationEidasRequest() {
        final AbstractAuthenticationRequest.AbstractBuilder builder = createEmptyAuthenticationRequestBuilder();
        populateMinimalBuilderFields(builder);
        final IAuthenticationRequest eidasAuthenticationRequest = builder.build();
        assertNotNull(eidasAuthenticationRequest);
    }

    private void assertAllPopulatedBuilderFieldsAreInToString(String print) {
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.SAMLID));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.ASSERTION_CONSUMER_SERVICE_URL));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.DESTINATION));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.ISSUER));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.PROVIDER_NAME));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.SP_COUNTRY));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.REQUESTED_ATTRIBUTES.toString()));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.NAME_ID_FORMAT));
        assertTrue(print.contains(LEVEL_OF_ASSURANCE_NOTIFIED_LOW));
        assertTrue(print.contains(AbstractAuthenticationRequestConstants.BINDING));
    }

    static AbstractAuthenticationRequest.AbstractBuilder populateAllBuilderFields(AbstractAuthenticationRequest.AbstractBuilder builder){
        return builder
                .id(AbstractAuthenticationRequestConstants.SAMLID)
                .assertionConsumerServiceURL(AbstractAuthenticationRequestConstants.ASSERTION_CONSUMER_SERVICE_URL)
                .destination(AbstractAuthenticationRequestConstants.DESTINATION)
                .issuer(AbstractAuthenticationRequestConstants.ISSUER)
                .providerName(AbstractAuthenticationRequestConstants.PROVIDER_NAME)
                .serviceProviderCountryCode(AbstractAuthenticationRequestConstants.SP_COUNTRY)
                .citizenCountryCode(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY)
                .requestedAttributes(AbstractAuthenticationRequestConstants.REQUESTED_ATTRIBUTES)
                .nameIdFormat(AbstractAuthenticationRequestConstants.NAME_ID_FORMAT)
                .levelOfAssurance(LEVEL_OF_ASSURANCE_NOTIFIED_LOW)
                .binding(AbstractAuthenticationRequestConstants.BINDING);
    }

    static AbstractAuthenticationRequest.AbstractBuilder populateMinimalBuilderFields(AbstractAuthenticationRequest.AbstractBuilder builder) {
        return builder
                .id(AbstractAuthenticationRequestConstants.SAMLID)
                .issuer(AbstractAuthenticationRequestConstants.ISSUER)
                .destination(AbstractAuthenticationRequestConstants.DESTINATION)
                .citizenCountryCode(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY)
                .levelOfAssurance(LEVEL_OF_ASSURANCE_NOTIFIED_LOW);
    }

    /**
     * Assertion method for
     * {@link IAuthenticationRequest} created with {@link AbstractAuthenticationRequestBuilderTest#populateMinimalBuilderFields(AbstractAuthenticationRequest.AbstractBuilder)}
     *
     * @param abstractAuthenticationRequest
     */
    static void assertAbstractAuthenticationRequestMinimalFieldsArePresent(IAuthenticationRequest abstractAuthenticationRequest) {
        assertEquals(AbstractAuthenticationRequestConstants.SAMLID,                 abstractAuthenticationRequest.getId());
        assertEquals(AbstractAuthenticationRequestConstants.ISSUER,                 abstractAuthenticationRequest.getIssuer());
        assertEquals(AbstractAuthenticationRequestConstants.DESTINATION,            abstractAuthenticationRequest.getDestination());
        assertEquals(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY,        abstractAuthenticationRequest.getCitizenCountryCode());
        assertEquals(LEVEL_OF_ASSURANCE_NOTIFIED_LOW,                               abstractAuthenticationRequest.getLevelOfAssurance());
    }

    /**
     * Assertion method for
     * {@link IAuthenticationRequest} created with {@link AbstractAuthenticationRequestBuilderTest#populateAllBuilderFields(AbstractAuthenticationRequest.AbstractBuilder)}
     *
     * @param abstractAuthenticationRequest
     */
    static void assertAbstractAuthenticationRequestAllFieldsArePresent(IAuthenticationRequest abstractAuthenticationRequest) {
        assertEquals(AbstractAuthenticationRequestConstants.SAMLID,                         abstractAuthenticationRequest.getId());
        assertEquals(AbstractAuthenticationRequestConstants.ASSERTION_CONSUMER_SERVICE_URL, abstractAuthenticationRequest.getAssertionConsumerServiceURL());
        assertEquals(AbstractAuthenticationRequestConstants.DESTINATION,                    abstractAuthenticationRequest.getDestination());
        assertEquals(AbstractAuthenticationRequestConstants.ISSUER,                         abstractAuthenticationRequest.getIssuer());
        assertEquals(AbstractAuthenticationRequestConstants.PROVIDER_NAME,                  abstractAuthenticationRequest.getProviderName());
        assertEquals(AbstractAuthenticationRequestConstants.CITIZEN_COUNTRY,                abstractAuthenticationRequest.getCitizenCountryCode());
        assertEquals(AbstractAuthenticationRequestConstants.SP_COUNTRY,                     abstractAuthenticationRequest.getServiceProviderCountryCode());
        assertEquals(AbstractAuthenticationRequestConstants.REQUESTED_ATTRIBUTES,           abstractAuthenticationRequest.getRequestedAttributes());
        assertEquals(AbstractAuthenticationRequestConstants.NAME_ID_FORMAT,                 abstractAuthenticationRequest.getNameIdFormat());
        assertEquals(LEVEL_OF_ASSURANCE_NOTIFIED_LOW,                                       abstractAuthenticationRequest.getLevelOfAssurance());
        assertEquals(AbstractAuthenticationRequestConstants.BINDING,                        abstractAuthenticationRequest.getBinding());
    }

    /**
     * Assertion method for
     * to compare {@link IAuthenticationRequest}'s are equal by fields
     * @param expected
     * @param actual
     */
    static void assertAbstractAuthenticationRequestAllFieldsAreEqual(IAuthenticationRequest expected, IAuthenticationRequest actual ) {
        assertEquals(expected.getId(),                          actual.getId());
        assertEquals(expected.getAssertionConsumerServiceURL(), actual.getAssertionConsumerServiceURL());
        assertEquals(expected.getDestination(),                 actual.getDestination());
        assertEquals(expected.getIssuer(),                      actual.getIssuer());
        assertEquals(expected.getProviderName(),                actual.getProviderName());
        assertEquals(expected.getCitizenCountryCode(),          actual.getCitizenCountryCode());
        assertEquals(expected.getServiceProviderCountryCode(),  actual.getServiceProviderCountryCode());
        assertEquals(expected.getRequestedAttributes(),         actual.getRequestedAttributes());
        assertEquals(expected.getNameIdFormat(),                actual.getNameIdFormat());
        assertEquals(expected.getLevelOfAssurance(),            actual.getLevelOfAssurance());
        assertEquals(expected.getBinding(),                     actual.getBinding());
    }

    static AbstractAuthenticationRequest.AbstractBuilder createMinimalAbstractAuthenticationRequestBuilder() {
        return populateMinimalBuilderFields(createEmptyAuthenticationRequestBuilder());
    }

    static AbstractAuthenticationRequest.AbstractBuilder createEmptyAuthenticationRequestBuilder() {
        return new ConcreteAbstractAuthenticationRequestBuilder();
    }

    static AbstractAuthenticationRequest.AbstractBuilder createAuthenticationRequestBuilderFromRequest(IAuthenticationRequest iar) {
        return new ConcreteAbstractAuthenticationRequestBuilder(iar);
    }

    static AbstractAuthenticationRequest.AbstractBuilder createAuthenticationRequestBuilderFromRequestBuilder(AbstractAuthenticationRequest.AbstractBuilder iar) {
        return new ConcreteAbstractAuthenticationRequestBuilder(iar);
    }
    /**
     * Concrete class for testing AbstractAuthenticationRequest
     */
    static class ConcreteAbstractAuthenticationRequestBuilder extends AbstractAuthenticationRequest.AbstractBuilder {

        ConcreteAbstractAuthenticationRequestBuilder() {
            super();
        }

        ConcreteAbstractAuthenticationRequestBuilder(AbstractAuthenticationRequest.AbstractBuilder iar) {
            super(iar);
        }

        ConcreteAbstractAuthenticationRequestBuilder(IAuthenticationRequest iar) {
            super(iar);
        }
        @Override
        protected void validate() throws IllegalArgumentException {}

        @Nonnull
        @Override
        protected IAuthenticationRequest newInstance() {
            return new AbstractAuthenticationRequest(this) {};
        }
    }
}
