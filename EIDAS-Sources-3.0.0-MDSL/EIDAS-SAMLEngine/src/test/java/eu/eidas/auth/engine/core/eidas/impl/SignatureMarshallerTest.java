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

package eu.eidas.auth.engine.core.eidas.impl;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.util.tests.TestingConstants;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.security.NoSuchAlgorithmException;

/**
 * Test class for {@link SignatureMarshaller}
 */
public class SignatureMarshallerTest {

    private SignatureMarshaller signatureMarshaller;
    private ProtocolEngineI protocolEngine;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        OpenSamlHelper.initialize();
        protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("CONF1");
        signatureMarshaller = new SignatureMarshaller(protocolEngine.getSigner());
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshallWithSignature() throws MarshallingException {
        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        signatureMarshaller.marshall(signature);
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject, Document)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshallWithSignatureAndDocument() throws MarshallingException, ParserConfigurationException {
        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        Document document = DocumentBuilderFactoryUtil.newDocument();

        signatureMarshaller.marshall(signature, document);
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject, Element)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshallWithSignatureAndElement() throws ParserConfigurationException, MarshallingException, XMLSecurityException {
        final SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        final Document document = DocumentBuilderFactoryUtil.newDocument();
        final XMLSignature xmlSignature = new XMLSignature(
                document,
                "",
                signature.getSignatureAlgorithm(),
                signature.getCanonicalizationAlgorithm()
        );

        signatureMarshaller.marshall(signature, xmlSignature.getElement());
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject, Element)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshallWithSignatureAndElementRsa() throws ParserConfigurationException, MarshallingException, XMLSecurityException, EIDASSAMLEngineException {
        final SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        final AuthnRequest requestToSign = getRequestToSign();
        requestToSign.setSignature(signature);
        final Document document = DocumentBuilderFactoryUtil.newDocument();
        final XMLSignature xmlSignature = new XMLSignature(
                document,
                "",
                signature.getSignatureAlgorithm(),
                signature.getCanonicalizationAlgorithm()
        );

        signatureMarshaller.marshall(signature, xmlSignature.getElement());
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshallWithSignatureHMac() throws MarshallingException {
        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_MAC_HMAC_SHA512);
        signature.setHMACOutputLength(1000);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        signatureMarshaller.marshall(signature);
    }


    private AuthnRequest getRequestToSign() throws EIDASSAMLEngineException {
        final IEidasAuthenticationRequest testRequest = getTestRequest();
        return protocolEngine.getProtocolProcessor().marshallRequest(
                testRequest,
                TestingConstants.SERVICE_METADATA_URL_CONS.toString(),
                protocolEngine.getCoreProperties(),
                protocolEngine.getClock().getCurrentTime()
        );
    }

    private IEidasAuthenticationRequest getTestRequest() {
        return EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString())
                .destination(TestingConstants.DESTINATION_CONS.toString())
                .serviceProviderCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .requestedAttributes(getRequestedAttributes())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.getValue())
                .build();
    }

    private ImmutableAttributeMap getRequestedAttributes() {
        return new ImmutableAttributeMap.Builder()
                .put(EidasSpec.Definitions.PERSON_IDENTIFIER)
                .build();
    }
}