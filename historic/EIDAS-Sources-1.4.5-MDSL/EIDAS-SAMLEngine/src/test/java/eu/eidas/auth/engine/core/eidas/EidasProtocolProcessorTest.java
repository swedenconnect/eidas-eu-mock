package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.DefaultCoreProperties;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.util.tests.TestingConstants;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * Test class for {@link EidasProtocolProcessor}
 *
 */
public class EidasProtocolProcessorTest {

    /**
     * Attribute Definition
     */
    private static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();


    private static ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("Service");

    private static ProtocolEngineI protocolEngineWrongOrderMetadataElementsProtocolVersioning = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceWrongOrderMetadataElementsProtocolVersioning");

    private IAuthenticationRequest iAuthenticationRequest = buildIAuthenticationRequest();

    private IAuthenticationResponse iAuthenticationResponse = buildIAuthenticateResponse();

    private String ipAddress = "128.128.128.128";

    private DefaultCoreProperties defaultCoreProperties = createDefaultCoreProperties();

    private DateTime currentTime = DateTime.now();


    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier matches the one where assertion must be included in the reponse.
     * <p/>
     * Must succeed.
     */
    @Test
    public void marshallErrorResponseWithAssertion() throws EIDASSAMLEngineException {

        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();
        includeAssertionApplicationIdentifiers.add("CEF:eIDAS-ref:2.2");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(1, actualNumberAssertion);
    }


    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier doe not matches the one where assertion must be included in the response.
     * <p/>
     * Must succeed.
     */
    @Test
    public void marshallErrorResponseWithoutAssertion() throws EIDASSAMLEngineException {

        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();
        includeAssertionApplicationIdentifiers.add("CEF:eIDAS-ref:2.1");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }


    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier list is null.
     * <p/>
     * Must succeed.
     */
    @Test
    public void marshallErrorResponseApplicationIdentifiersNull() throws EIDASSAMLEngineException {

        List<String> includeAssertionApplicationIdentifiers = null;

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier list is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void marshallErrorResponseApplicationIdentifiersEmpty() throws EIDASSAMLEngineException {

        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }


    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when metadata fetched contains protocol versioning elements in another order.
     * when the application identifier matches the one where assertion must be included in the response.
     * <p/>
     * Must succeed.
     */
    @Test
    public void marshallErrorResponseOtherAttributesInMetadataMatchesApplicationIdentifier() throws EIDASSAMLEngineException {

        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();
        includeAssertionApplicationIdentifiers.add("CEF:eIDAS-ref:2.2");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngineWrongOrderMetadataElementsProtocolVersioning.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(1, actualNumberAssertion);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when metadata fetched contains protocol versioning elements in another order.
     * when the application identifier does not match any of the ones where assertion must be included in the response.
     * <p/>
     * Must succeed.
     */
    @Test
    public void marshallErrorResponseOtherAttributesInMetadataNoMatchedApplicationIdentifier() throws EIDASSAMLEngineException {

        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();
        includeAssertionApplicationIdentifiers.add("CEF:eIDAS-ref:2.1");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngineWrongOrderMetadataElementsProtocolVersioning.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }


    /**
     * Creates a {@link DefaultCoreProperties}
     *
     * @return the {@link DefaultCoreProperties} instance
     */
    private DefaultCoreProperties createDefaultCoreProperties() {
        Properties properties = new Properties();
        properties.setProperty(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        properties.setProperty(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        properties.setProperty("timeNotOnOrAfter", "0");
        return new DefaultCoreProperties(properties);
    }


    /**
     * Methods that builds a {@link IAuthenticationRequest}
     *
     * @return the {@link IAuthenticationRequest}
     */
    private IAuthenticationRequest buildIAuthenticationRequest() {

        final EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();

        eidasAuthenticationRequestBuilder.id(TestingConstants.SAML_ID_CONS.toString())
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString());

        return eidasAuthenticationRequestBuilder.build();
    }


    /**
     * Methods that builds a {@link IAuthenticationResponse}
     *
     * @return the {@link IAuthenticationResponse}
     */
    private IAuthenticationResponse buildIAuthenticateResponse() {
        AuthenticationResponse.Builder eidasAuthnResponse = new AuthenticationResponse.Builder();
        eidasAuthnResponse.country("UK");
        eidasAuthnResponse.id("QDS2QFD");
        eidasAuthnResponse.audienceRestriction("PUBLIC");
        eidasAuthnResponse.inResponseTo("6E97069A1754ED");
        eidasAuthnResponse.failure(false);
        eidasAuthnResponse.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        eidasAuthnResponse.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        eidasAuthnResponse.statusMessage("TEST");
        eidasAuthnResponse.notBefore(new DateTime());
        eidasAuthnResponse.notOnOrAfter(new DateTime());
        eidasAuthnResponse.ipAddress("123.123.123.123");
        eidasAuthnResponse.issuer("issuer");
        eidasAuthnResponse.levelOfAssurance("assuranceLevel");

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();
        mapBuilder.put(PERSON_IDENTIFIER, Collections.<AttributeValue<String>>emptySet());
        eidasAuthnResponse.attributes(mapBuilder.build());

        return eidasAuthnResponse.build();
    }


}