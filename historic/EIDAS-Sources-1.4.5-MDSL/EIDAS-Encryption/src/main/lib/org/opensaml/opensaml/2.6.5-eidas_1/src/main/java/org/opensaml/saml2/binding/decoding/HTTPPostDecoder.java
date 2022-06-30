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

package org.opensaml.saml2.binding.decoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Message decoder implementing the SAML 2.0 HTTP POST binding. */
public class HTTPPostDecoder extends BaseSAML2MessageDecoder {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(HTTPPostDecoder.class);

    /** Constructor. */
    public HTTPPostDecoder() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param pool parser pool used to deserialize messages
     */
    public HTTPPostDecoder(ParserPool pool) {
        super(pool);
    }

    /** {@inheritDoc} */
    public String getBindingURI() {
        return SAMLConstants.SAML2_POST_BINDING_URI;
    }

    /** {@inheritDoc} */
    protected boolean isIntendedDestinationEndpointURIRequired(SAMLMessageContext samlMsgCtx) {
        return isMessageSigned(samlMsgCtx);
    }

    /** {@inheritDoc} */
    protected void doDecode(MessageContext messageContext) throws MessageDecodingException {
        if (!(messageContext instanceof SAMLMessageContext)) {
            log.error("Invalid message context type, this decoder only support SAMLMessageContext");
            throw new MessageDecodingException(
                    "Invalid message context type, this decoder only support SAMLMessageContext");
        }

        if (!(messageContext.getInboundMessageTransport() instanceof HTTPInTransport)) {
            log.error("Invalid inbound message transport type, this decoder only support HTTPInTransport");
            throw new MessageDecodingException(
                    "Invalid inbound message transport type, this decoder only support HTTPInTransport");
        }

        SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;

        HTTPInTransport inTransport = (HTTPInTransport) samlMsgCtx.getInboundMessageTransport();
        if (!inTransport.getHTTPMethod().equalsIgnoreCase("POST")) {
            throw new MessageDecodingException("This message decoder only supports the HTTP POST method");
        }

        String relayState = inTransport.getParameterValue("RelayState");
        samlMsgCtx.setRelayState(relayState);
        log.debug("Decoded SAML relay state of: {}", relayState);

        InputStream base64DecodedMessage = getBase64DecodedMessage(inTransport);
        SAMLObject inboundMessage = (SAMLObject) unmarshallMessage(base64DecodedMessage);
        samlMsgCtx.setInboundMessage(inboundMessage);
        samlMsgCtx.setInboundSAMLMessage(inboundMessage);
        log.debug("Decoded SAML message");

        populateMessageContext(samlMsgCtx);
    }

    /**
     * Gets the Base64 encoded message from the request and decodes it.
     * 
     * @param transport inbound message transport
     * 
     * @return decoded message
     * 
     * @throws MessageDecodingException thrown if the message does not contain a base64 encoded SAML message
     */
    protected InputStream getBase64DecodedMessage(HTTPInTransport transport) throws MessageDecodingException {
        log.debug("Getting Base64 encoded message from request");
        String encodedMessage = transport.getParameterValue("SAMLRequest");
        if (DatatypeHelper.isEmpty(encodedMessage)) {
            encodedMessage = transport.getParameterValue("SAMLResponse");
        }

        if (DatatypeHelper.isEmpty(encodedMessage)) {
            log.error("Request did not contain either a SAMLRequest or "
                    + "SAMLResponse paramter.  Invalid request for SAML 2 HTTP POST binding.");
            throw new MessageDecodingException("No SAML message present in request");
        }

        log.trace("Base64 decoding SAML message:\n{}", encodedMessage);
        byte[] decodedBytes = Base64.decode(encodedMessage);
        if(decodedBytes == null){
            log.error("Unable to Base64 decode SAML message");
            throw new MessageDecodingException("Unable to Base64 decode SAML message");
        }

        log.trace("Decoded SAML message:\n{}", new String(decodedBytes));
        return new ByteArrayInputStream(decodedBytes);
    }
}