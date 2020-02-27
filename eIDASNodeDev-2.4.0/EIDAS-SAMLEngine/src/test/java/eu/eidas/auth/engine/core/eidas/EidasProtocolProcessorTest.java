/*
 * Copyright (c) 2018 by European Commission
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
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

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


    private static ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceProtocolVersioning");

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
        AuthenticationResponse.Builder eidasAuthnResponseBuilder = new AuthenticationResponse.Builder();
        eidasAuthnResponseBuilder
                .country("UK")
                .id("QDS2QFD")
                .audienceRestriction("PUBLIC")
                .inResponseTo("6E97069A1754ED")
                .failure(false)
                .statusCode(EIDASStatusCode.REQUESTER_URI.toString())
                .subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString())
                .statusMessage("TEST")
                .notBefore(new DateTime())
                .notOnOrAfter(new DateTime())
                .ipAddress("123.123.123.123")
                .issuer("issuer")
                .levelOfAssurance("assuranceLevel")
                .subject("subject")
                .subjectNameIdFormat("subjectNameIdFormat");

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();
        mapBuilder.put(PERSON_IDENTIFIER, Collections.<AttributeValue<String>>emptySet());
        eidasAuthnResponseBuilder.attributes(mapBuilder.build());

        return eidasAuthnResponseBuilder.build();
    }


}