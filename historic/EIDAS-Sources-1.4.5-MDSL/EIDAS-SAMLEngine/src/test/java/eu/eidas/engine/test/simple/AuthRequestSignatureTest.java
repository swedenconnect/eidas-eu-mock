package eu.eidas.engine.test.simple;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.impl.StorkAuthenticationRequest;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
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
            new AttributeDefinition.Builder<String>().nameUri("http://www.stork.gov.eu/1.0/eIdentifier")
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
    public final void testGenerateStorkAuthnRequest() throws Exception {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d").
                issuer("http://localhost:7001/SP/metadata").
                providerName("TestProvider").
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("BE").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(null).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        /*final EidasAuthenticationRequest request = new EidasAuthenticationRequest();

        request.setDestination(destination);
        request.setProviderName(spName);
        request.setQaa(QAAL);
        request.setPersonalAttributeList(pal);
        request.setAssertionConsumerServiceURL(assertConsumerUrl);

        // news parameters
        request.setSpSector(spSector);
        request.setSpInstitution(null);
        request.setSpApplication(spApplication);
        request.setSpCountry(spCountry);
        request.setSPID(spId);
        request.setCitizenCountryCode("BE");*/

        ProtocolEngineInterceptor engineInterceptor = null;
        try {
            engineInterceptor = new ProtocolEngineInterceptor();
        } catch (EIDASSAMLEngineException exc) {
            fail("error while initializing samlengine " + exc);
        }
        assertNotNull(engineInterceptor);
        /*
        engineInterceptor.setSignerProperty(SamlEngineSignI.SIGNATURE_ALGORITHM,
                                            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384");
                                            */
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
                public ProtocolEngineConfiguration get() throws SamlEngineConfigurationException {
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
            return samlEngine.unmarshallRequestAndValidate(tokenSaml, "BE",null);
        }

        public String getSigningAlgo(final byte[] token) throws EIDASSAMLEngineException {
            AuthnRequest unmarshalled = samlEngine.unmarshallRequest(token,null,false);
            return unmarshalled.getSignature().getSignatureAlgorithm();
        }

        public SAMLExtensionFormat getMessageFormat() {
            return SAMLExtensionFormat.EIDAS10;
        }

    }
}
