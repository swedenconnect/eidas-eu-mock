/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.saml2.binding.encoding;

import java.security.KeyPair;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.joda.time.DateTime;
import org.opensaml.common.BaseTestCase;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.security.SecurityHelper;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for {@link HTTPPostEncoder}.
 */
public class HTTPPostSimpleSignEncoderTest extends BaseTestCase {

    /** Velocity template engine. */
    private VelocityEngine velocityEngine;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        velocityEngine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
    }

    /**
     * Tests encoding a SAML message to an servlet response.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testResponseEncoding() throws Exception {
        SAMLObjectBuilder<StatusCode> statusCodeBuilder = (SAMLObjectBuilder<StatusCode>) builderFactory
                .getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = statusCodeBuilder.buildObject();
        statusCode.setValue(StatusCode.SUCCESS_URI);

        SAMLObjectBuilder<Status> statusBuilder = (SAMLObjectBuilder<Status>) builderFactory
                .getBuilder(Status.DEFAULT_ELEMENT_NAME);
        Status responseStatus = statusBuilder.buildObject();
        responseStatus.setStatusCode(statusCode);

        SAMLObjectBuilder<Response> responseBuilder = (SAMLObjectBuilder<Response>) builderFactory
                .getBuilder(Response.DEFAULT_ELEMENT_NAME);
        Response samlMessage = responseBuilder.buildObject();
        samlMessage.setID("foo");
        samlMessage.setVersion(SAMLVersion.VERSION_20);
        samlMessage.setIssueInstant(new DateTime(0));
        samlMessage.setStatus(responseStatus);

        SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
                .getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        Endpoint samlEndpoint = endpointBuilder.buildObject();
        samlEndpoint.setLocation("http://example.org");
        samlEndpoint.setResponseLocation("http://example.org/response");

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(response, false);
        
        BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
        messageContext.setOutboundMessageTransport(outTransport);
        messageContext.setPeerEntityEndpoint(samlEndpoint);
        messageContext.setOutboundSAMLMessage(samlMessage);
        messageContext.setRelayState("relay");

        HTTPPostSimpleSignEncoder encoder = new HTTPPostSimpleSignEncoder(velocityEngine,
        "/templates/saml2-post-simplesign-binding.vm");
        encoder.encode(messageContext);

        assertEquals("Unexpected content type", "text/html", response.getContentType());
        assertEquals("Unexpected character encoding", response.getCharacterEncoding(), "UTF-8");
        assertEquals("Unexpected cache controls", "no-cache, no-store", response.getHeader("Cache-control"));
        assertEquals(138273327, response.getContentAsString().hashCode());
    }

    @SuppressWarnings("unchecked")
    public void testRequestEncoding() throws Exception {
        SAMLObjectBuilder<AuthnRequest> responseBuilder = (SAMLObjectBuilder<AuthnRequest>) builderFactory
                .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        AuthnRequest samlMessage = responseBuilder.buildObject();
        samlMessage.setID("foo");
        samlMessage.setVersion(SAMLVersion.VERSION_20);
        samlMessage.setIssueInstant(new DateTime(0));

        SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
                .getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        Endpoint samlEndpoint = endpointBuilder.buildObject();
        samlEndpoint.setLocation("http://example.org");
        samlEndpoint.setResponseLocation("http://example.org/response");

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(response, false);
        
        BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
        messageContext.setOutboundMessageTransport(outTransport);
        messageContext.setPeerEntityEndpoint(samlEndpoint);
        messageContext.setOutboundSAMLMessage(samlMessage);
        messageContext.setRelayState("relay");

        HTTPPostSimpleSignEncoder encoder = new HTTPPostSimpleSignEncoder(velocityEngine,
        "/templates/saml2-post-simplesign-binding.vm");
        encoder.encode(messageContext);

        assertEquals("Unexpected content type", "text/html", response.getContentType());
        assertEquals("Unexpected character encoding", response.getCharacterEncoding(), "UTF-8");
        assertEquals("Unexpected cache controls", "no-cache, no-store", response.getHeader("Cache-control"));
        assertEquals(-198852454, response.getContentAsString().hashCode());
    }
    
    @SuppressWarnings("unchecked")
    public void testRequestEncodingWithSimpleSign() throws Exception {
        SAMLObjectBuilder<AuthnRequest> responseBuilder = (SAMLObjectBuilder<AuthnRequest>) builderFactory
                .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        AuthnRequest samlMessage = responseBuilder.buildObject();
        samlMessage.setID("foo");
        samlMessage.setVersion(SAMLVersion.VERSION_20);
        samlMessage.setIssueInstant(new DateTime(0));

        SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
                .getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        Endpoint samlEndpoint = endpointBuilder.buildObject();
        samlEndpoint.setLocation("http://example.org");
        samlEndpoint.setResponseLocation("http://example.org/response");

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(response, false);
        
        BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
        messageContext.setOutboundMessageTransport(outTransport);
        messageContext.setPeerEntityEndpoint(samlEndpoint);
        messageContext.setOutboundSAMLMessage(samlMessage);
        messageContext.setRelayState("relay");
        
        KeyPair kp = SecurityHelper.generateKeyPair("RSA", 1024, null);
        messageContext.setOutboundSAMLMessageSigningCredential(
                SecurityHelper.getSimpleCredential(kp.getPublic(), kp.getPrivate()));

        HTTPPostSimpleSignEncoder encoder = new HTTPPostSimpleSignEncoder(velocityEngine,
        "/templates/saml2-post-simplesign-binding.vm");
        encoder.encode(messageContext);
        
        // Not elegant, but works ok for basic sanity check.
        String form = response.getContentAsString();
        int start;
        
        start = form.indexOf("name=\"Signature\"");
        assertTrue("Signature parameter not found in form control data", start != -1);
        
        start = form.indexOf("name=\"SigAlg\"");
        assertTrue("SigAlg parameter not found in form control data", start != -1);
        
        start = form.indexOf("name=\"KeyInfo\"");
        assertTrue("KeyInfo parameter not found in form control data", start != -1);
        
        // Note: to test that actual signature is cryptographically correct, really need a known good test vector.
        // Need to verify that we're signing over the right data in the right byte[] encoded form.
    }
}