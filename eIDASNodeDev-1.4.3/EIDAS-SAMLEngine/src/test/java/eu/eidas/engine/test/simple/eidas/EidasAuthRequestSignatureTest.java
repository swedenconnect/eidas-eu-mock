package eu.eidas.engine.test.simple.eidas;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
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
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static eu.eidas.engine.EidasAttributeTestUtil.newEidasAttributeDefinition;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EidasAuthRequestSignatureTest {

    private static final String SAML_ENGINE_NAME = "CONF1";

    /**
     * The engine.
     */

    @Before
    public void setUp() {
    }


    /**
     * Instantiates a new EIDAS authentication request test.
     */
    public EidasAuthRequestSignatureTest() {

        final AttributeDefinition dateOfBirth = newEidasAttributeDefinition("DateOfBirth", "DateOfBirth", true);
        final AttributeDefinition eIDNumber = newEidasAttributeDefinition("PersonIdentifier", "PersonIdentifier", true, true, false);

        immutableAttributeMap= new ImmutableAttributeMap.Builder().put(dateOfBirth).put(eIDNumber).build();

        destination = "http://EidasService.gov.xx/EIDASNODE/ColleagueRequest";
        assertConsumerUrl = "http://EidasConnector.gov.xx/EIDASNODE/ColleagueResponse";

        spName = "University of Oxford";
        spSector = "EDU001";
        spApplication = "APP001";
        spCountry = "EN";

        spId = "EDU001-OXF001-APP001";

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
     * The List of Personal Attributes.
     */
    private ImmutableAttributeMap immutableAttributeMap;

    /**
     * The assertion consumer URL.
     */
    private String assertConsumerUrl;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthRequestSignatureTest.class.getName());

    /**
     * Test generate authentication request error personal attribute name error.
     */
    final static private String REQUEST_ISSUER = "http://localhost:7001/SP/metadata".toLowerCase();
    @Test
    public final void testGenerateAuthnRequest() throws Exception {
		IEidasAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .issuer(REQUEST_ISSUER)
                .providerName(spName)
                .requestedAttributes(immutableAttributeMap)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .serviceProviderCountryCode(spCountry)
                .spType("public")
                .levelOfAssurance(LevelOfAssurance.LOW.stringValue())
                .nameIdFormat(SamlNameIdFormat.TRANSIENT.getNameIdFormat())
                .citizenCountryCode("ES")
                .build();

        ProtocolEngineInterceptor engineInterceptor = null;
        try {
            engineInterceptor = new ProtocolEngineInterceptor();
            engineInterceptor.setWhitelist(Arrays.asList(REQUEST_ISSUER));
        } catch (EIDASSAMLEngineException exc) {
            fail("error while initializing samlengine " + exc);
        }
        assertNotNull(engineInterceptor);
        /*
        engineInterceptor.setSignerProperty(SamlEngineSignI.SIGNATURE_ALGORITHM,
                                            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384");
                                            */
        IRequestMessage binaryRequestMessage = engineInterceptor.generateAuthnRequest(request);
        assertNotNull(binaryRequestMessage);
        byte[] tokenSaml = binaryRequestMessage.getMessageBytes();
        String requestXML = EidasStringUtil.toString(tokenSaml);
        try {
            String signingAlgo = engineInterceptor.getSigningAlgo(tokenSaml);
            assertEquals(signingAlgo, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
            
            IAuthenticationRequest authenticationRequest = engineInterceptor.validateAuthnRequest(tokenSaml);
            assertNotNull(authenticationRequest);
        } catch (EIDASSAMLEngineException exc) {
            LOG.error("Error: " + requestXML);
            fail("error while validating request " + exc);
        }
/*
        engineInterceptor.setSignerProperty(SamlEngineSignI.SIGNATURE_ALGORITHM,
                                            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
                                            */
        try {
            binaryRequestMessage = engineInterceptor.generateAuthnRequest(request);
            tokenSaml = binaryRequestMessage.getMessageBytes();
            String signingAlgo = engineInterceptor.getSigningAlgo(tokenSaml);
            assertEquals(signingAlgo, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
            IAuthenticationRequest authenticationRequest = engineInterceptor.validateAuthnRequest(tokenSaml);
            assertNotNull(authenticationRequest);
        } catch (EIDASSAMLEngineException exc) {
            LOG.error("Error");
            fail("error while validating request " + exc);
        }
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

        private Collection<String> whitelist;
        
        private void setWhitelist(Collection<String> list) {
			whitelist=list;
		}

        private Collection<String> getWhitelist() {
			return whitelist;
		}

        public IRequestMessage generateAuthnRequest(final IAuthenticationRequest request) throws EIDASSAMLEngineException {
            return samlEngine.generateRequestMessage(request, request.getIssuer());
        }

        public IAuthenticationRequest validateAuthnRequest(final byte[] tokenSaml) throws EIDASSAMLEngineException {
            return samlEngine.unmarshallRequestAndValidate(tokenSaml, "ES",getWhitelist());
        }

		public String getSigningAlgo(final byte[] token) throws EIDASSAMLEngineException {
            AuthnRequest unmarshalled = samlEngine.unmarshallRequest(token,getWhitelist(),true);
            return unmarshalled.getSignature().getSignatureAlgorithm();
        }

        public SAMLExtensionFormat getMessageFormat() {
            return SAMLExtensionFormat.EIDAS10;
        }

    }
}
