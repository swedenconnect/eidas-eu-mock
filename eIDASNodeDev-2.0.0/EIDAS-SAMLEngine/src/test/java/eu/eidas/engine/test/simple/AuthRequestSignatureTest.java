/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.engine.test.simple;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

public class AuthRequestSignatureTest {

    private static final String SAML_ENGINE_NAME = "CONF2";

    /**
     * The engine.
     */

    @Before
    public void setUp() {
    }

    /**
     * The destination.
     */
    private String destination;

    /**
     * The service provider name.
     */
    private String spName;

    /**
     * The service provider sector.
     */
    private String spSector;

    /**
     * The service provider application.
     */
    private String spApplication;

    /**
     * The service provider country.
     */
    private String spCountry;

    /**
     * The service provider id.
     */
    private String spId;

    /**
     * The quality authentication assurance level.
     */
    private static final int QAAL = 3;

    /**
     * The assertion consumer URL.
     */
    private String assertConsumerUrl;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthRequestSignatureTest.class.getName());

    private static final AttributeDefinition<String> EIDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifierr")
                    .friendlyName("eIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(false)
                    .uniqueIdentifier(false)
                    .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final ImmutableAttributeMap REQUESTED_ATTRIBUTES =
            new ImmutableAttributeMap.Builder().put(EIDENTIFIER).build();

    /**
     * Test generate authentication request error personal attribute name error.
     */
    @Test
    @Ignore
    public final void testGenerateEidasAuthnRequest() throws Exception {
        IEidasAuthenticationRequest request = EidasAuthenticationRequest.builder().
                id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d").
                issuer("http://localhost:7001/SP/metadata").
                providerName("TestProvider").
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("BE").
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        ProtocolEngineInterceptor engineInterceptor = null;
        try {
            engineInterceptor = new ProtocolEngineInterceptor();
        } catch (EIDASSAMLEngineException exc) {
            fail("error while initializing samlengine " + exc);
        }
        assertNotNull(engineInterceptor);
        IRequestMessage authReq;
        try {

            authReq = engineInterceptor.generateAuthnRequest(request);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
            throw e;
        }
        assertNotNull(authReq);
        byte[] tokenSaml = authReq.getMessageBytes();
        String requestXML = EidasStringUtil.toString(tokenSaml);
        IAuthenticationRequest authenticationRequest = null;
        try {
            String signingAlgo = engineInterceptor.getSigningAlgo(tokenSaml);
            assertEquals(signingAlgo, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
            authenticationRequest = engineInterceptor.validateAuthnRequest(tokenSaml);
        } catch (EIDASSAMLEngineException exc) {
            LOG.error("Error: " + requestXML);
            fail("error while validating request " + exc);
        }
        assertNotNull(authenticationRequest);

        /*
        engineInterceptor.setSignerProperty(SamlEngineSignI.SIGNATURE_ALGORITHM,
                                            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
                                            */
        authReq = null;
        try {
            authReq = engineInterceptor.generateAuthnRequest(request);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
        assertNotNull(authReq);
        tokenSaml = authReq.getMessageBytes();
        authReq = null;
        try {
            String signingAlgo = engineInterceptor.getSigningAlgo(tokenSaml);
            assertEquals(signingAlgo, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
            authenticationRequest = engineInterceptor.validateAuthnRequest(tokenSaml);
        } catch (EIDASSAMLEngineException exc) {
            LOG.error("Error");
            fail("error while validating request " + exc);
        }
        assertNotNull(authenticationRequest);
    }

    private class ProtocolEngineInterceptor extends AbstractProtocolEngine {

        private final ProtocolEngine samlEngine;

        ProtocolEngineInterceptor() throws EIDASSAMLEngineException {
            super(new ProtocolConfigurationAccessor() {

                @Nonnull
                @Override
                public ProtocolEngineConfiguration get() throws ProtocolEngineConfigurationException {
                    return DefaultProtocolEngineConfigurationFactory.getInstance().getConfiguration(SAML_ENGINE_NAME);
                }
            });
            samlEngine = (ProtocolEngine) ProtocolEngineFactory.getDefaultProtocolEngine(SAML_ENGINE_NAME);
        }

        public IRequestMessage generateAuthnRequest(final IAuthenticationRequest request)
                throws EIDASSAMLEngineException {
            return samlEngine.generateRequestMessage(request, null);
        }

        public IAuthenticationRequest validateAuthnRequest(final byte[] tokenSaml) throws EIDASSAMLEngineException {
            return samlEngine.unmarshallRequestAndValidate(tokenSaml, "BE");
        }

        public String getSigningAlgo(final byte[] token) throws EIDASSAMLEngineException {
            AuthnRequest unmarshalled = samlEngine.unmarshallRequest(token);
            return unmarshalled.getSignature().getSignatureAlgorithm();
        }


    }
}
