/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.engine;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.BinaryRequestMessage;
import eu.eidas.auth.commons.protocol.impl.BinaryResponseMessage;
import eu.eidas.auth.engine.configuration.ConfigurationAccessor;
import eu.eidas.auth.engine.configuration.FixedConfigurationAccessor;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultConfigurationAccessor;
import eu.eidas.auth.engine.configuration.dom.DefaultSamlEngineConfigurationFactory;
import eu.eidas.auth.engine.configuration.dom.SamlEngineConfigurationFactory;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.auth.engine.core.eidas.GenericEidasAttributeType;
import eu.eidas.auth.engine.xml.opensaml.*;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.ValidatorSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Due to business constraints, this class is part of the contract with a DG Taxud project, so keep it as is.
 * <p>
 * Remove this class in 1.2.
 *
 * @deprecated since 1.1, use {@link ProtocolEngine} instead.
 */
@Deprecated
@Beta
@SuppressWarnings("all")
public final class SamlEngine extends AbstractSamlEngine implements SamlEngineI {

    private static final class LazyDefaultSamlEngines {

        private static final ImmutableMap<String, SamlEngineI> DEFAULT_SAML_ENGINES;

        static {
            SamlEngineConfigurationFactory configurationFactory = DefaultSamlEngineConfigurationFactory.getInstance();
            ImmutableMap.Builder<String, SamlEngineI> builder = ImmutableMap.builder();
            ImmutableMap<String, SamlEngineConfiguration> map;
            try {
                map = configurationFactory.getConfigurationMapAccessor().get();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            for (final String instanceName : map.keySet()) {
                builder.put(instanceName, createDefaultSamlEngine(instanceName));
            }
            DEFAULT_SAML_ENGINES = builder.build();
        }

        @Nonnull
        private static SamlEngineI createDefaultSamlEngine(@Nonnull String instanceName) {
            return new SamlEngine(new DefaultConfigurationAccessor(instanceName));
        }
    }

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SamlEngine.class);

    public static final String ATTRIBUTE_EMPTY_LITERAL = "Attribute name is null or empty.";

    /**
     * Creates an instance of SamlEngine.
     *
     * @param nameInstance the name instance
     * @return instance of SamlEngine
     */
    @Nonnull
    public static SamlEngineI createSAMLEngine(@Nonnull String nameInstance,
                                               @Nonnull ExtensionProcessorI extensionProcessor)
            throws EIDASSAMLEngineException {
        return createSAMLEngine(nameInstance, null, extensionProcessor, new SamlEngineSystemClock());
    }

    /**
     * Returns a default SamlEngine instance matching the given name retrieved from the configuration file.
     *
     * @param instanceName the instance name
     * @return the SamlEngine instance matching the given name retrieved from the configuration file
     */
    @Nullable
    public static SamlEngineI getDefaultSamlEngine(@Nonnull String instanceName) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        return LazyDefaultSamlEngines.DEFAULT_SAML_ENGINES.get(instanceName.trim());
    }

    @Nonnull
    public static SamlEngineI createSAMLEngine(@Nonnull String nameInstance,
                                               CertificateConfigurationManager configManager,
                                               @Nonnull ExtensionProcessorI extensionProcessor,
                                               @Nonnull SamlEngineClock samlEngineClock)
            throws SamlEngineConfigurationException {

        LOG.info(SAML_EXCHANGE, "create instance: {} ", nameInstance);

        SamlEngineConfiguration samlEngineConfiguration =
                SamlEngineConfigurationFactory.getConfigurationMap(configManager).get(nameInstance);

        extensionProcessor.configureExtension();

        SamlEngineConfiguration configuration = SamlEngineConfiguration.builder(samlEngineConfiguration)
                .extensionProcessor(extensionProcessor)
                .clock(samlEngineClock)
                .build();

        SamlEngineI samlEngine = new SamlEngine(new FixedConfigurationAccessor(configuration));

        LOG.info(SAML_EXCHANGE, "created instance: {} ", samlEngine);

        return samlEngine;
    }

    /**
     * Constructs a new Saml engine instance.
     *
     * @param configurationAccessor the accessor to the configuration of this instance.
     */
    public SamlEngine(@Nonnull ConfigurationAccessor configurationAccessor) {
        super(configurationAccessor);
    }

    /**
     * Generate authentication response base.
     *
     * @param status the status
     * @param assertConsumerURL the assert consumer URL.
     * @param inResponseTo the in response to
     * @return the response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private Response genAuthnRespBase(Status status, String assertConsumerURL, String inResponseTo)
            throws EIDASSAMLEngineException {
        LOG.debug("Generate Authentication Response base.");
        Response response =
                BuilderFactoryUtil.generateResponse(SAMLEngineUtils.generateNCName(), SAMLEngineUtils.getCurrentTime(),
                                                    status);

        // Set name Spaces
        registerResponseNamespace(response);

        // Mandatory EIDAS
        LOG.debug("Generate Issuer");
        Issuer issuer = BuilderFactoryUtil.generateIssuer();
        issuer.setValue(getCoreProperties().getResponder());

        // Format Entity Optional EIDAS
        issuer.setFormat(getCoreProperties().getFormatEntity());

        response.setIssuer(issuer);

        // destination Mandatory EIDAS
        if (assertConsumerURL != null) {
            response.setDestination(assertConsumerURL.trim());
        }

        // inResponseTo Mandatory
        response.setInResponseTo(inResponseTo.trim());

        // Optional
        response.setConsent(getCoreProperties().getConsentAuthnResponse());

        return response;
    }

    @Nonnull
    private AttributeDefinition getAttributeDefinitionNotNull(@Nonnull String name) throws EIDASSAMLEngineException {
        AttributeDefinition attributeDefinition = getExtensionProcessor().getAttributeDefinitionNullable(name);
        if (null == attributeDefinition) {
            LOG.info("BUSINESS EXCEPTION : Attribute name: {} is not known.", name);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               "Attribute name: " + name + " is not known.");
        }
        return attributeDefinition;
    }

    /**
     * Generates one attribute statement for the response.
     *
     * @param attributeMap the personal attribute map
     * @return the attribute statement for the response
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    private AttributeStatement generateResponseAttributeStatement(@Nonnull ImmutableAttributeMap attributeMap)
            throws EIDASSAMLEngineException {
        LOG.trace("Generate attribute statement");

        AttributeStatement attrStatement =
                (AttributeStatement) BuilderFactoryUtil.buildXmlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
        List<Attribute> list = attrStatement.getAttributes();

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : attributeMap.getAttributeMap()
                .entrySet()) {

            // Verification that only one value is permitted, simple or
            // complex, not both.

            ImmutableSet<? extends AttributeValue<?>> value = entry.getValue();
            addToAttributeList(list, entry.getKey(), value);
        }
        return attrStatement;
    }

    private void addToAttributeList(@Nonnull List<Attribute> list,
                                    @Nonnull AttributeDefinition<?> attributeDefinition,
                                    @Nonnull ImmutableSet<? extends AttributeValue<?>> values)
            throws EIDASSAMLEngineException {
        // TODO take transliteration into account

        AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (final AttributeValue<?> attributeValue : values) {
            try {
                String marshalledValue = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
                builder.add(marshalledValue);
            } catch (AttributeValueMarshallingException e) {
                LOG.error("Illegal attribute value: " + e, e);
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), e);
            }
        }

        list.add(getExtensionProcessor().generateAttrSimple(attributeDefinition, builder.build()));
    }

    public static boolean needsTransliteration(String v) {
        return AttributeValueTransliterator.needsTransliteration(v);
    }

    @Nullable
    private AttributeStatement findAttributeStatementNullable(@Nonnull Assertion assertion)
            throws EIDASSAMLEngineException {
        List<XMLObject> orderedChildren = assertion.getOrderedChildren();
        // Search the attribute statement.
        for (XMLObject child : orderedChildren) {
            if (child instanceof AttributeStatement) {
                return (AttributeStatement) child;
            }
        }
        return null;
    }

    @Nonnull
    private AttributeStatement findAttributeStatement(@Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        AttributeStatement attributeStatement = findAttributeStatementNullable(assertion);
        if (null != attributeStatement) {
            return attributeStatement;
        }

        LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : AttributeStatement not present.");
        throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                           EidasErrorKey.INTERNAL_ERROR.errorCode(), "AttributeStatement not present.");
    }

    private String computeSimpleValue(XSAnyImpl xsAny) {
        if (null != xsAny) {
            List<XMLObject> unknownXMLObjects = xsAny.getUnknownXMLObjects();
            if (null != unknownXMLObjects && !unknownXMLObjects.isEmpty()) {
                try {
                    TransformerFactory transFactory = TransformerFactory.newInstance();
                    Transformer transformer = transFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    StringWriter stringWriter = new StringWriter();
                    transformer.transform(new DOMSource(unknownXMLObjects.get(0).getDOM()),
                                          new StreamResult(stringWriter));
                    return stringWriter.toString();
                } catch (TransformerConfigurationException e) {
                    LOG.warn(SAML_EXCHANGE, "ERROR : transformer configuration exception", e);
                } catch (TransformerException e) {
                    LOG.warn(SAML_EXCHANGE, "ERROR :  transformer exception", e);
                }
            }
            return xsAny.getTextContent();
        }
        return null;
    }

    private Map<String, String> computeComplexValue(XSAnyImpl complexValue) {
        Map<String, String> multiValues = new HashMap<String, String>();
        for (final XMLObject xmlObject : complexValue.getUnknownXMLObjects()) {

            XSAnyImpl simple = (XSAnyImpl) xmlObject;

            multiValues.put(simple.getElementQName().getLocalPart(), simple.getTextContent());
        }
        return multiValues;

    }

    /**
     * Converts an assertion to an attribute map.
     *
     * @param assertion the assertion
     * @return the attribute map
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    private ImmutableAttributeMap convertToAttributeMap(@Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        LOG.trace("Generate personal attribute list from XMLObject.");

        AttributeStatement attributeStatement = findAttributeStatement(assertion);

        List<Attribute> attributes = attributeStatement.getAttributes();

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        // Process the attributes.
        for (final Attribute attribute : attributes) {

            String attributeName = attribute.getName();

            String friendlyName = attribute.getFriendlyName();

            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNotNull(attributeName);

            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();

            ImmutableSet.Builder<AttributeValue<?>> setBuilder = new ImmutableSet.Builder<>();

            List<XMLObject> values = attribute.getOrderedChildren();

            QName xmlType = attributeDefinition.getXmlType();

            QName latinScript = new QName(xmlType.getNamespaceURI(), "LatinScript", xmlType.getPrefix());

            // Process the values.
            for (XMLObject xmlObject : values) {

                try {
                    if (xmlObject instanceof XSStringImpl) {

                        // Process simple value.
                        setBuilder.add(
                                attributeValueMarshaller.unmarshal(((XSStringImpl) xmlObject).getValue(), false));

                    } else if (xmlObject instanceof XSAnyImpl) {

                        XSAnyImpl xsAny = (XSAnyImpl) xmlObject;

                        // TODO: move to STORK Extension Processor
                        if ("http://www.stork.gov.eu/1.0/signedDoc".equals(attributeName)) {

                            setBuilder.add(attributeValueMarshaller.unmarshal(computeSimpleValue(xsAny), false));

                            // TODO: move to STORK Extension Processor
                        } else if ("http://www.stork.gov.eu/1.0/canonicalResidenceAddress".equals(attributeName)) {

                            // Process complex value.
                            setBuilder.add(
                                    attributeValueMarshaller.unmarshal(computeComplexValue(xsAny).toString(), false));
                        } else {

                            boolean isNonLatinScriptAlternateVersion = false;
                            String latinScriptAttrValue = xsAny.getUnknownAttributes().get(latinScript);
                            if (StringUtils.isNotBlank(latinScriptAttrValue) && "false".equals(latinScriptAttrValue)) {
                                isNonLatinScriptAlternateVersion = true;
                            }

                            // Process simple value.
                            setBuilder.add(attributeValueMarshaller.unmarshal(xsAny.getTextContent(),
                                                                              isNonLatinScriptAlternateVersion));
                        }

                        // TODO: remove
                    } else if (xmlObject instanceof GenericEidasAttributeType) {

                        // Process simple value.
                        setBuilder.add(
                                attributeValueMarshaller.unmarshal(((GenericEidasAttributeType) xmlObject).getValue(),
                                                                   false));

                    } else {
                        LOG.info("BUSINESS EXCEPTION : attribute value is unknown in generatePersonalAttributeList.");
                        throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                                           EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                                           "Attribute value is unknown for \""
                                                                   + attributeDefinition.getNameUri().toASCIIString()
                                                                   + "\" - value: \"" + xmlObject + "\"");
                    }
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("BUSINESS EXCEPTION : Illegal Attribute Value: " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                                       EidasErrorKey.INTERNAL_ERROR.errorCode(), e);
                }
            }

            // Check if friendlyName matches when provided  - TODO Temorary removed due to validator failure
/*            if (StringUtils.isNotEmpty(friendlyName) &&
                    attributeDefinition != null &&
                    !friendlyName.equals(attributeDefinition.getFriendlyName())) {
                LOG.error("BUSINESS EXCEPTION : Illegal Attribute friendlyName for " + attributeDefinition.getNameUri().toString() +
                        " expected " +  attributeDefinition.getFriendlyName() + " got " + friendlyName);
                throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                        EidasErrorKey.INTERNAL_ERROR.errorCode(), "Illegal Attribute friendlyName for " + attributeDefinition.getNameUri().toString() +
                        " expected " +  attributeDefinition.getFriendlyName() + " got " + friendlyName);
            }*/

            mapBuilder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) setBuilder.build());
        }

        return mapBuilder.build();
    }

    /**
     * Generate the authentication request.
     *
     * @param request the request that contain all parameters for generate an authentication request.
     * @return the EIDAS authentication request that has been processed.
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public IRequestMessage generateRequestMessage(IAuthenticationRequest request) throws EIDASSAMLEngineException {
        return generateRequestMessage(request, true, true);
    }

    private IRequestMessage generateRequestMessage(IAuthenticationRequest request, boolean validate, boolean sign)
            throws EIDASSAMLEngineException {
        LOG.trace("Generate SAMLAuthnRequest.");
        if (null == request) {
            LOG.debug(SAML_EXCHANGE, "Sign and Marshall - null input");
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Sign and Marshall -null input");
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage());
        }

        if (validate) {
            // Validate mandatory parameters
            getExtensionProcessor().validateAuthenticationRequest(request, null);
        }

        String id = SAMLEngineUtils.generateNCName();
        AuthnRequest authnRequestAux =
                BuilderFactoryUtil.generateAuthnRequest(id, SAMLVersion.VERSION_20, SAMLEngineUtils.getCurrentTime());

        // Set name spaces.
        registerRequestNamespace(authnRequestAux);

        // Add parameter Mandatory
        authnRequestAux.setForceAuthn(Boolean.TRUE);

        // Add parameter Mandatory
        authnRequestAux.setIsPassive(Boolean.FALSE);

        authnRequestAux.setAssertionConsumerServiceURL(request.getAssertionConsumerServiceURL());

        authnRequestAux.setProviderName(request.getProviderName());

        // Add protocol binding
        authnRequestAux.setProtocolBinding(getExtensionProcessor().getProtocolBinding(request, getCoreProperties()));

        // Add parameter optional
        // Destination is mandatory
        // The application must to know the destination
        if (StringUtils.isNotBlank(request.getDestination())) {
            authnRequestAux.setDestination(request.getDestination());
        }

        // Consent is optional. Set from SAMLEngine.xml - consent.
        authnRequestAux.setConsent(getCoreProperties().getConsentAuthnRequest());

        Issuer issuer = BuilderFactoryUtil.generateIssuer();

        if (request.getIssuer() != null) {
            issuer.setValue(SAMLEngineUtils.getValidIssuerValue(request.getIssuer()));
        } else {
            issuer.setValue(getCoreProperties().getRequester());
        }

        // Optional
        String formatEntity = getCoreProperties().getFormatEntity();
        if (StringUtils.isNotBlank(formatEntity)) {
            issuer.setFormat(formatEntity);
        }

        authnRequestAux.setIssuer(issuer);
        getExtensionProcessor().addRequestedAuthnContext(request, authnRequestAux);

        // Generate format extensions.
        Extensions formatExtensions = getExtensionProcessor().generateExtensions(getCoreProperties(), request);
        // add the extensions to the SAMLAuthnRequest
        authnRequestAux.setExtensions(formatExtensions);
        addNameIDPolicy(authnRequestAux, request.getNameIdFormat());

        // the result contains an authentication request token (byte[]),
        // identifier of the token, and all parameters from the request.
        IAuthenticationRequest authRequestFromExtensionProcessor =
                getExtensionProcessor().processExtensions(request.getCitizenCountryCode(), authnRequestAux, null, null);
        byte[] bytes;
        try {
            if (sign) {
                bytes = signAndMarshallRequest(authnRequestAux);
            } else {
                bytes = noSignAndMarshall(authnRequestAux);
            }
        } catch (EIDASSAMLEngineException e) {
            LOG.debug(SAML_EXCHANGE, "Sign and Marshall.", e);
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Sign and Marshall.", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }

        return new BinaryRequestMessage(authRequestFromExtensionProcessor, bytes);
    }

    private void addNameIDPolicy(AuthnRequest authnRequestAux, String selectedNameID) throws EIDASSAMLEngineException {
        if (getExtensionProcessor().getFormat() == SAMLExtensionFormat.EIDAS10 && !StringUtils.isEmpty(
                selectedNameID)) {
            NameIDPolicy policy = (NameIDPolicy) BuilderFactoryUtil.buildXmlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
            policy.setFormat(selectedNameID);
            policy.setAllowCreate(true);
            authnRequestAux.setNameIDPolicy(policy);
        }
    }

    /**
     * Generate authentication response in one of the supported formats.
     *
     * @param request the request
     * @param response the response authentication request
     * @param ipAddress the IP address
     * @return the authentication response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated this method is only used in unit tests, do not use it as it does not sign the response
     */
    @Override
    @Deprecated
    @VisibleForTesting
    public IResponseMessage generateResponseMessage(IAuthenticationRequest request,
                                                    IAuthenticationResponse response,
                                                    String ipAddress) throws EIDASSAMLEngineException {
        return generateResponseMessage(request, response, false, ipAddress);
    }

    /**
     * Generate authentication response in one of the supported formats.
     *
     * @param request the request
     * @param authnResponse the authentication response from the IdP
     * @param ipAddress the IP address
     * @param signAssertion whether to sign the attribute assertion
     * @return the authentication response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public IResponseMessage generateResponseMessage(IAuthenticationRequest request,
                                                    IAuthenticationResponse authnResponse,
                                                    boolean signAssertion,
                                                    String ipAddress) throws EIDASSAMLEngineException {
        LOG.trace("generateResponseMessage");
        // Validate parameters
        validateParamResponse(request, authnResponse);

        // At this point the assertion consumer service URL is mandatory (and must have been replaced by the value from the metadata if needed)
        if (StringUtils.isBlank(request.getAssertionConsumerServiceURL())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Request AssertionConsumerServiceURL must not be blank.");
        }

        // Mandatory SAML
        LOG.trace("Generate StatusCode");
        StatusCode statusCode = BuilderFactoryUtil.generateStatusCode(StatusCode.SUCCESS_URI);

        LOG.trace("Generate Status");
        Status status = BuilderFactoryUtil.generateStatus(statusCode);

        LOG.trace("Generate StatusMessage");
        StatusMessage statusMessage = BuilderFactoryUtil.generateStatusMessage(StatusCode.SUCCESS_URI);

        status.setStatusMessage(statusMessage);

        LOG.trace("Generate Response");

        // RESPONSE
        Response response = genAuthnRespBase(status, request.getAssertionConsumerServiceURL(), request.getId());

        if (authnResponse.getIssuer() != null && !authnResponse.getIssuer().isEmpty() && response.getIssuer() != null) {
            response.getIssuer().setValue(SAMLEngineUtils.getValidIssuerValue(authnResponse.getIssuer()));
        }
        DateTime notOnOrAfter = new DateTime();

        notOnOrAfter = notOnOrAfter.plusSeconds(getCoreProperties().getTimeNotOnOrAfter());

        Assertion assertion = AssertionUtil.generateResponseAssertion(false, ipAddress, request, response.getIssuer(),
                                                                      authnResponse.getAttributes(), notOnOrAfter,
                                                                      getCoreProperties().getFormatEntity(),
                                                                      getCoreProperties().getResponder(),
                                                                      getExtensionProcessor().getFormat(),
                                                                      getCoreProperties().isOneTimeUse());

        AttributeStatement attrStatement = generateResponseAttributeStatement(authnResponse.getAttributes());

        assertion.getAttributeStatements().add(attrStatement);

        addResponseAuthnContextClassRef(authnResponse, assertion);
        // Add assertions
        Assertion signedAssertion = null;
        if (signAssertion) {
            try {
                signedAssertion = (Assertion) signAssertion(assertion);
            } catch (EIDASSAMLEngineException exc) {
                LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : cannot sign assertion: {}", exc.getMessage());
                LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : cannot sign assertion: {}", exc);
            }
        }
        response.getAssertions().add(signedAssertion == null ? assertion : signedAssertion);

        try {
            byte[] responseBytes = signAndMarshallResponse(request, response);
            return new BinaryResponseMessage(authnResponse, responseBytes);
        } catch (EIDASSAMLEngineException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Sign and Marshall.", e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : Sign and Marshall.", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    // TODO move this to ExtensionProcessors
    private void addResponseAuthnContextClassRef(IAuthenticationResponse responseAuthReq, Assertion assertion)
            throws EIDASSAMLEngineException {
        if (!StringUtils.isEmpty(responseAuthReq.getLevelOfAssurance())) {
            AuthnContextClassRef authnContextClassRef =
                    assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef();
            if (authnContextClassRef == null) {
                authnContextClassRef = (AuthnContextClassRef) BuilderFactoryUtil.buildXmlObject(
                        AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
                assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextClassRef(authnContextClassRef);
            }
            authnContextClassRef.setAuthnContextClassRef(responseAuthReq.getLevelOfAssurance());
        }
    }

    /**
     * Generate authentication response fail.
     *
     * @param request the request
     * @param response the response
     * @param ipAddress the IP address
     * @return the authentication response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public IResponseMessage generateResponseMessageFail(IAuthenticationRequest request,
                                                        IAuthenticationResponse response,
                                                        String ipAddress) throws EIDASSAMLEngineException {
        LOG.trace("generateResponseMessageFail");
        validateParamResponseFail(request, response);

        // Mandatory
        StatusCode statusCode = BuilderFactoryUtil.generateStatusCode(response.getStatusCode());

        // Mandatory SAML
        LOG.trace("Generate StatusCode.");
        // Subordinate code is optional in case not covered into next codes:
        // - urn:oasis:names:tc:SAML:2.0:status:AuthnFailed
        // - urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue
        // - urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy
        // - urn:oasis:names:tc:SAML:2.0:status:RequestDenied
        // - http://www.stork.gov.eu/saml20/statusCodes/QAANotSupported

        if (StringUtils.isNotBlank(response.getSubStatusCode())) {
            StatusCode newStatusCode = BuilderFactoryUtil.generateStatusCode(response.getSubStatusCode());
            statusCode.setStatusCode(newStatusCode);
        }

        LOG.debug("Generate Status.");
        Status status = BuilderFactoryUtil.generateStatus(statusCode);

        if (StringUtils.isNotBlank(response.getStatusMessage())) {
            StatusMessage statusMessage = BuilderFactoryUtil.generateStatusMessage(response.getStatusMessage());

            status.setStatusMessage(statusMessage);
        }

        LOG.trace("Generate Response.");
        // RESPONSE
        Response responseFail = genAuthnRespBase(status, request.getAssertionConsumerServiceURL(), request.getId());

        String responseIssuer = response.getIssuer();
        if (responseIssuer != null && !responseIssuer.isEmpty()) {
            responseFail.getIssuer().setValue(responseIssuer);
        }
        DateTime notOnOrAfter = new DateTime();

        notOnOrAfter = notOnOrAfter.plusSeconds(getCoreProperties().getTimeNotOnOrAfter());

        Assertion assertion =
                AssertionUtil.generateResponseAssertion(true, ipAddress, request, responseFail.getIssuer(),
                                                        ImmutableAttributeMap.of(), notOnOrAfter,
                                                        getCoreProperties().getFormatEntity(),
                                                        getCoreProperties().getResponder(),
                                                        getExtensionProcessor().getFormat(),
                                                        getCoreProperties().isOneTimeUse());
        addResponseAuthnContextClassRef(response, assertion);
        responseFail.getAssertions().add(assertion);

        LOG.trace("Sign and Marshall ResponseFail.");

        AuthenticationResponse.Builder eidasResponse = new AuthenticationResponse.Builder();

        try {
            byte[] responseBytes = signAndMarshallResponse(request, responseFail);
            eidasResponse.id(responseFail.getID());
            eidasResponse.issuer(responseFail.getIssuer().getValue());
            eidasResponse.ipAddress(ipAddress);
            eidasResponse.inResponseTo(responseFail.getInResponseTo());
            eidasResponse.responseStatus(extractResponseStatus(responseFail));
            return new BinaryResponseMessage(eidasResponse.build(), responseBytes);
        } catch (EIDASSAMLEngineException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : SAMLEngineException.", e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : SAMLEngineException.", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    /**
     * Gets the country from X.509 Certificate.
     *
     * @param keyInfo the key info
     * @return the country
     */
    private String getCountry(KeyInfo keyInfo) {
        LOG.trace("Recover country information.");
        try {
            org.opensaml.xml.signature.X509Certificate xmlCert =
                    keyInfo.getX509Datas().get(0).getX509Certificates().get(0);

            // Transform the KeyInfo to X509Certificate.
            X509Certificate cert = CertificateUtil.toCertificate(xmlCert.getValue());

            String distName = cert.getSubjectDN().toString();

            distName = StringUtils.deleteWhitespace(StringUtils.upperCase(distName));

            String countryCode = "C=";
            int init = distName.indexOf(countryCode);

            String result = "";
            if (init > StringUtils.INDEX_NOT_FOUND) {
                // Exist country code.
                int end = distName.indexOf(',', init);

                if (end <= StringUtils.INDEX_NOT_FOUND) {
                    end = distName.length();
                }

                if (init < end && end > StringUtils.INDEX_NOT_FOUND) {
                    result = distName.substring(init + countryCode.length(), end);
                    //It must be a two characters value
                    if (result.length() > 2) {
                        result = result.substring(0, 2);
                    }
                }
            }
            return result.trim();
        } catch (EIDASSAMLEngineException e) {
            LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : Procces getCountry from certificate: " + e.getMessage(), e);
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }

    /**
     * Sets the name spaces.
     *
     * @param xmlToken the new name spaces
     */
    private void registerRequestNamespace(@Nonnull XMLObject xmlToken) {
        LOG.trace("Set namespaces.");
        xmlToken.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlToken.getNamespaceManager().registerNamespace(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlToken.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));
        // Calling the extension processor extension
        getExtensionProcessor().registerRequestNamespace(xmlToken);
    }

    /**
     * Register the namespace on the response SAML xml token
     *
     * @param xmlToken
     */
    void registerResponseNamespace(@Nonnull XMLObject xmlToken) {
        LOG.trace("Set namespaces.");
        xmlToken.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlToken.getNamespaceManager().registerNamespace(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlToken.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));
        // Calling the extension processor extension
        getExtensionProcessor().registerResponseNamespace(xmlToken);
    }

    /**
     * Validate parameters from response.
     *
     * @param request the request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private void checkRequestSanity(IAuthenticationRequest request) throws EIDASSAMLEngineException {
        getExtensionProcessor().checkRequestSanity(request);
    }

    /**
     * Validate parameters from response.
     *
     * @param response the response authentication request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private void checkResponseSanity(IAuthenticationResponse response) throws EIDASSAMLEngineException {
        if (response.getAttributes() == null || response.getAttributes().isEmpty()) {
            LOG.error(SAML_EXCHANGE, "No attribute values in response.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "No attribute values in response.");
        }
    }

    /**
     * Validate parameters from response.
     *
     * @param request the request
     * @param response the response authentication request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private void validateParamResponse(IAuthenticationRequest request, IAuthenticationResponse response)
            throws EIDASSAMLEngineException {
        LOG.trace("Validate parameters response.");
        checkRequestSanity(request);
        checkResponseSanity(response);
    }

    /**
     * Validate parameter from response fail.
     *
     * @param request the request
     * @param response the response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private void validateParamResponseFail(IAuthenticationRequest request, IAuthenticationResponse response)
            throws EIDASSAMLEngineException {
        LOG.trace("Validate parameters response fail.");
        if (StringUtils.isBlank(response.getStatusCode())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Error Status Code is null or empty.");
        }

        if (StringUtils.isBlank(request.getAssertionConsumerServiceURL())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "assertionConsumerServiceURL is null or empty.");
        }

        if (StringUtils.isBlank(request.getId())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "request ID is null or empty.");
        }
    }

    /**
     * Process and validates the authentication request.
     *
     * @param tokenSaml the token SAML
     * @return the authentication request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public IAuthenticationRequest processValidateRequestToken(@Nonnull String citizenCountryCode, byte[] tokenSaml)
            throws EIDASSAMLEngineException {
        LOG.trace("processValidateRequestToken");

        if (tokenSaml == null) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Saml authentication request is null.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Saml authentication request is null.");
        }
        XmlSchemaUtil.validateSamlSchema(EidasStringUtil.toString(tokenSaml));

        AuthnRequest originalSamlRequest = validateRequestHelper(tokenSaml);
        LOG.trace("Generate EIDASAuthnSamlRequest.");

        String originCountryCode = (originalSamlRequest.getSignature() != null) ? getCountry(
                originalSamlRequest.getSignature().getKeyInfo()) : null;

        IAuthenticationRequest authRequestFromExtensionProcessor =
                getExtensionProcessor().unmarshallRequest(citizenCountryCode, originalSamlRequest, originCountryCode);

        checkRequestSanity(authRequestFromExtensionProcessor);

        return authRequestFromExtensionProcessor;
    }

    private AuthnRequest validateRequestHelper(byte[] tokenSaml) throws EIDASSAMLEngineException {
        LOG.trace("Validate AuthnRequest");
        AuthnRequest samlRequest;
        try {
            samlRequest = validateRequestWithSuite(tokenSaml);
        } catch (ValidationException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }

        if (samlRequest == null) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());
        }
        return samlRequest;
    }

    private AuthnRequest validateRequestWithSuite(byte[] requestBytes)
            throws EIDASSAMLEngineException, ValidationException {
        ExtensionProcessorI extensionProcessor = getExtensionProcessor();
        AuthnRequest samlRequest;
        ValidatorSuite suite = Configuration.getValidatorSuite(extensionProcessor.getRequestValidatorId());
        samlRequest = validateRequestBytes(requestBytes);
        try {
            suite.validate(samlRequest);
            if (tryProcessExtensions(extensionProcessor, samlRequest)) {
                LOG.debug("validation with " + extensionProcessor.getClass().getName() + " succeeded !!!");
            } else {
                LOG.debug("validation with " + extensionProcessor.getClass().getName()
                                  + " tryProcessExtensions() returned false");
                samlRequest = null;
            }
        } catch (ValidationException e) {
            LOG.debug("validation with " + extensionProcessor.getClass().getName() + " failed: " + e, e);
            throw e;
        }
        return samlRequest;
    }

    private Response computeAuxResponse(byte[] responseBytes) throws EIDASSAMLEngineException {
        Response samlResponseAux = null;
        try {
            samlResponseAux = validateResponseBytes(responseBytes);
            if (decryptResponse()) {
                /*
                    In the @eu.eidas.encryption.SAMLAuthnResponseDecrypter.decryptSAMLResponse method when inserting
                    the decrypted Assertions the DOM resets to null. Marsahlling it again resolves it.
                    More info in the links belows
                    https://jira.spring.io/browse/SES-148
                    http://digitaliser.dk/forum/2621692
                */
                noSignAndMarshall(samlResponseAux);
            }
        } catch (EIDASSAMLEngineException e) {
            LOG.warn("error validating the response ", e.getMessage());
            LOG.debug("error validating the response", e);
        }
        return samlResponseAux;
    }

    private void validateSamlResponse(Response samlResponse) throws EIDASSAMLEngineException {
        LOG.trace("Validate AuthnResponse");
        ValidatorSuite suite = Configuration.getValidatorSuite(getExtensionProcessor().getResponseValidatorId());
        try {
            suite.validate(samlResponse);
        } catch (ValidationException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : ValidationException: validate AuthResponse.", e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : ValidationException: validate AuthResponse.", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        } catch (Exception e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : ValidationException: validate AuthResponse.", e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : ValidationException: validate AuthResponse.", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }

    }

    private IResponseStatus extractResponseStatus(@Nonnull Response samlResponse) {

        ResponseStatus.Builder builder = ResponseStatus.builder();

        LOG.trace("Set statusCode.");
        Status status = samlResponse.getStatus();
        StatusCode statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        builder.statusCode(statusCodeValue);
        builder.failure(isFailureStatusCode(statusCodeValue));

        // Subordinate code.
        StatusCode subStatusCode = statusCode.getStatusCode();
        if (subStatusCode != null) {
            builder.subStatusCode(subStatusCode.getValue());
        }

        if (status.getStatusMessage() != null) {
            LOG.trace("Set statusMessage.");
            builder.statusMessage(status.getStatusMessage().getMessage());
        }

        return builder.build();
    }

    private boolean isFailureStatusCode(String statusCodeValue) {
        return !StatusCode.SUCCESS_URI.equals(statusCodeValue);
    }

    private AuthenticationResponse.Builder createResponseBuilder(Response samlResponse) {
        LOG.trace("Create EidasAuthResponse.");
        AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();

        responseBuilder.country(getCountry(samlResponse.getSignature().getKeyInfo()));

        LOG.trace("Set ID.");
        responseBuilder.id(samlResponse.getID());
        LOG.trace("Set InResponseTo.");
        responseBuilder.inResponseTo(samlResponse.getInResponseTo());

        responseBuilder.issuer(samlResponse.getIssuer().getValue());

        responseBuilder.encrypted(
                samlResponse.getEncryptedAssertions() != null && !samlResponse.getEncryptedAssertions().isEmpty());
        return responseBuilder;
    }

    /**
     * Marshalls the given bytes into a SAML Response.
     *
     * @param tokenSaml the SAML response bytes
     * @return the SAML response instance
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public Response marshall(@Nonnull byte[] tokenSaml) throws EIDASSAMLEngineException {
        LOG.trace("processValidateResponseToken");

        if (null == tokenSaml) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Saml authentication response is null.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Saml authentication response is null.");
        }

        XmlSchemaUtil.validateSamlSchema(EidasStringUtil.toString(tokenSaml));

        Response samlResponse = computeAuxResponse(tokenSaml);

        validateSamlResponse(samlResponse);

        return samlResponse;
    }

    /**
     * Process and validates the authentication response.
     *
     * @param responseBytes the token SAML
     * @param userIP the user IP
     * @return the authentication response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public IAuthenticationResponse processValidateResponseToken(byte[] responseBytes,
                                                                String userIP,
                                                                long skewTimeInMillis) throws EIDASSAMLEngineException {

        Response samlResponse = marshall(responseBytes);

        return validateMarshalledResponse(samlResponse, userIP, skewTimeInMillis);
    }

    /**
     * Validate authentication response.
     *
     * @param samlResponse the token SAML
     * @param userIP the user IP
     * @param skewTimeInMillis the skew time
     * @return the authentication response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public IAuthenticationResponse validateMarshalledResponse(Response samlResponse,
                                                              String userIP,
                                                              long skewTimeInMillis) throws EIDASSAMLEngineException {

        AuthenticationResponse.Builder authnResponse = createResponseBuilder(samlResponse);

        IResponseStatus responseStatus = extractResponseStatus(samlResponse);

        authnResponse.responseStatus(responseStatus);

        LOG.trace("validateEidasResponse");
        Assertion assertion = validateResponse(samlResponse, userIP, skewTimeInMillis);

        if (assertion != null) {
            LOG.trace("Set notOnOrAfter.");
            authnResponse.notOnOrAfter(assertion.getConditions().getNotOnOrAfter());

            LOG.trace("Set notBefore.");
            authnResponse.notBefore(assertion.getConditions().getNotBefore());

            authnResponse.audienceRestriction(
                    (assertion.getConditions().getAudienceRestrictions().get(0)).getAudiences()
                            .get(0)
                            .getAudienceURI());
            if (!assertion.getAuthnStatements().isEmpty()
                    && assertion.getAuthnStatements().get(0).getAuthnContext() != null &&
                    assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef() != null) {
                authnResponse.levelOfAssurance(assertion.getAuthnStatements()
                                                       .get(0)
                                                       .getAuthnContext()
                                                       .getAuthnContextClassRef()
                                                       .getAuthnContextClassRef());
            }
        }

        // Case no error.
        if (assertion != null && !isFailure(responseStatus)) {
            LOG.trace("Status Success. Set PersonalAttributeList.");
            authnResponse.attributes(convertToAttributeMap(assertion));
        } else {
            LOG.trace("Status Fail.");
        }
        LOG.trace("Return result.");
        return authnResponse.build();

    }

    private boolean isFailure(@Nonnull IResponseStatus responseStatus) {
        return responseStatus.isFailure() || isFailureStatusCode(responseStatus.getStatusCode());
    }

    /**
     * Validate response.
     *
     * @param samlResponse the SAML response
     * @param userIP the user IP
     * @return the assertion
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private Assertion validateResponse(Response samlResponse, String userIP, Long skewTimeInMillis)
            throws EIDASSAMLEngineException {
        // Exist only one Assertion
        if (samlResponse.getAssertions() == null || samlResponse.getAssertions().isEmpty()) {
            //in replace of throwing  EIDASSAMLEngineException("Assertion is null or empty.")
            LOG.info(SAML_EXCHANGE,
                     "BUSINESS EXCEPTION : Assertion is null, empty or the response is encrypted and the decryption is not active.");
            return null;
        }

        Assertion assertion = (Assertion) samlResponse.getAssertions().get(0);

        verifyMethodBearer(userIP, assertion);

        // Applying skew time conditions before testing it
        DateTime skewedNotBefore =
                new DateTime(assertion.getConditions().getNotBefore().getMillis() - skewTimeInMillis, DateTimeZone.UTC);
        DateTime skewedNotOnOrAfter =
                new DateTime(assertion.getConditions().getNotOnOrAfter().getMillis() + skewTimeInMillis,
                             DateTimeZone.UTC);
        LOG.debug(SAML_EXCHANGE, "skewTimeInMillis : {}", skewTimeInMillis);
        LOG.debug(SAML_EXCHANGE, "skewedNotBefore       : {}", skewedNotBefore);
        LOG.debug(SAML_EXCHANGE, "skewedNotOnOrAfter    : {}", skewedNotOnOrAfter);
        assertion.getConditions().setNotBefore(skewedNotBefore);
        assertion.getConditions().setNotOnOrAfter(skewedNotOnOrAfter);

        verifyConditions(assertion);

        return assertion;
    }

    private void verifyConditions(Assertion assertion) throws EIDASSAMLEngineException {
        Conditions conditions = assertion.getConditions();
        DateTime serverDate = getClock().getCurrentTime();
        LOG.debug("serverDate            : " + serverDate);

        if (conditions.getAudienceRestrictions() == null || conditions.getAudienceRestrictions().isEmpty()) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : AudienceRestriction must be present");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "AudienceRestriction must be present");
        }
        if (conditions.getOneTimeUse() == null) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : OneTimeUse must be present");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "OneTimeUse must be present");
        }
        if (conditions.getNotBefore() == null) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : NotBefore must be present");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "NotBefore must be present");
        }
        if (conditions.getNotBefore().isAfter(serverDate)) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Current time is before NotBefore condition");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Current time is before NotBefore condition");
        }
        if (conditions.getNotOnOrAfter() == null) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : NotOnOrAfter must be present");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "NotOnOrAfter must be present");
        }
        if (conditions.getNotOnOrAfter().isBeforeNow()) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Current time is after NotOnOrAfter condition");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Current time is after NotOnOrAfter condition");
        }
        if (assertion.getConditions().getNotOnOrAfter().isBefore(serverDate)) {
            LOG.info(SAML_EXCHANGE,
                     "BUSINESS EXCEPTION : Token date expired (getNotOnOrAfter =  " + assertion.getConditions()
                             .getNotOnOrAfter() + ", server_date: " + serverDate + ")");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Token date expired (getNotOnOrAfter =  " + assertion.getConditions()
                                                       .getNotOnOrAfter() + " ), server_date: " + serverDate);
        }
    }

    private void verifyMethodBearer(String userIP, Assertion assertion) throws EIDASSAMLEngineException {
        boolean ipValidate = getCoreProperties().isIpValidation();
        if (ipValidate) {
            LOG.trace("Verified method Bearer");
            Subject subject = assertion.getSubject();
            if (null == subject) {
                LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : subject is null.");
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   "subject is null.");
            }
            List<SubjectConfirmation> subjectConfirmations = subject.getSubjectConfirmations();
            if (null == subjectConfirmations || subjectConfirmations.isEmpty()) {
                LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : SubjectConfirmations are null or empty.");
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   "SubjectConfirmations are null or empty.");
            }
            for (final SubjectConfirmation element : subjectConfirmations) {
                boolean isBearer = SubjectConfirmation.METHOD_BEARER.equals(element.getMethod());
                SubjectConfirmationData subjectConfirmationData = element.getSubjectConfirmationData();
                if (null == subjectConfirmationData) {
                    LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : subjectConfirmationData is null.");
                    throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       "subjectConfirmationData is null.");
                }
                String address = subjectConfirmationData.getAddress();
                if (isBearer) {
                    if (StringUtils.isBlank(userIP)) {
                        LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : browser_ip is null or empty.");
                        throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                           EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                           "browser_ip is null or empty.");
                    } else if (StringUtils.isBlank(address)) {
                        LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : token_ip attribute is null or empty.");
                        throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                           EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                           "token_ip attribute is null or empty.");
                    }
                }
                boolean ipEqual = address.equals(userIP);
                // Validation ipUser
                if (!ipEqual) {
                    LOG.info(SAML_EXCHANGE,
                             "BUSINESS EXCEPTION : SubjectConfirmation BEARER: IPs doesn't match : token_ip [{}] browser_ip [{}]",
                             address, userIP);
                    throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       "IPs doesn't match : token_ip (" + address + ") browser_ip ("
                                                               + userIP + ")");
                }
            }

        }
    }

    private void validateAssertionSignatures(Response response) throws EIDASSAMLEngineException {
        try {
            boolean validateSign = getCoreProperties().isValidateSignature();
            if (validateSign) {
                X509Certificate signatureCertificate =
                        getExtensionProcessor().getResponseSignatureCertificate(response.getIssuer().getValue());
                for (Assertion a : response.getAssertions()) {
                    if (a.isSigned() && null != a.getSignature()) {
                        getSigner().validateSignature(a, null == signatureCertificate ? null : ImmutableSet.of(
                                signatureCertificate));
                    }
                }
            }
        } catch (EIDASSAMLEngineException e) {
            EIDASSAMLEngineException exc =
                    new EIDASSAMLEngineException(EidasErrorKey.INVALID_ASSERTION_SIGNATURE.errorCode(),
                                                 EidasErrorKey.INVALID_ASSERTION_SIGNATURE.errorMessage(), e);
            throw exc;
        }

    }

    private AuthnRequest validateSignature(AuthnRequest request) throws EIDASSAMLEngineException {
        boolean validateSign = getCoreProperties().isValidateSignature();
        if (validateSign) {
            LOG.trace("Validate request Signature.");
            if (!request.isSigned() || null == request.getSignature()) {
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), "No signature");
            }
            try {
                X509Certificate signatureCertificate =
                        getExtensionProcessor().getResponseSignatureCertificate(request.getIssuer().getValue());
                return (AuthnRequest) getSigner().validateSignature(request, null == signatureCertificate ? null
                                                                                                          : ImmutableSet
                                                                                     .of(signatureCertificate));
            } catch (EIDASSAMLEngineException e) {
                LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : SAMLEngineException validateSignature: " + e,
                          e.getMessage(), e);
                throw e;
            }
        }
        return request;
    }

    private Response validateSignatureAndAssertionSignatures(Response response) throws EIDASSAMLEngineException {
        boolean validateSign = getCoreProperties().isValidateSignature();
        if (validateSign) {
            LOG.trace("Validate response Signature.");
            if (!response.isSigned() || null == response.getSignature()) {
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), "No signature");
            }

            String country = getCountry(response.getSignature().getKeyInfo());
            LOG.debug(SAML_EXCHANGE, "Response received from country: " + country);
            try {

                response = validateSignatureAndDecrypt(response);

                validateAssertionSignatures(response);

            } catch (EIDASSAMLEngineException e) {
                LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : SAMLEngineException validateSignature: " + e,
                          e.getMessage(), e);
                throw e;
            }
        }
        return response;
    }

    /**
     * Validate SAML.
     *
     * @param requestBytes the token SAML
     * @return the signable SAML object
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private AuthnRequest validateRequestBytes(byte[] requestBytes) throws EIDASSAMLEngineException {
        LOG.trace("Validate request bytes.");

        if (null == requestBytes) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Saml request bytes are null.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Saml request bytes are null.");
        }

        LOG.trace("Generate SAML Request.");

        AuthnRequest request = (AuthnRequest) unmarshall(requestBytes);
        request = validateSignature(request);

        validateSchema(request);

        return request;

    }

    private Response validateResponseBytes(byte[] responseBytes) throws EIDASSAMLEngineException {

        LOG.trace("Validate response bytes.");

        if (null == responseBytes) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : Saml response bytes are null.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Saml response bytes are null.");
        }

        LOG.trace("Generate SAML Response.");

        Response response = (Response) unmarshall(responseBytes);
        response = validateSignatureAndAssertionSignatures(response);

        validateSchema(response);

        return response;
    }

    private static void validateSchema(SignableSAMLObject samlObject) throws EIDASSAMLEngineException {
        LOG.trace("Validate Schema.");
        ValidatorSuite validatorSuite = Configuration.getValidatorSuite("saml2-core-schema-validator");
        try {
            validatorSuite.validate(samlObject);
        } catch (ValidationException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : ValidationException.", e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : ValidationException.", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
    }

    /**
     * This is used by the validator
     *
     * @// TODO: 13/05/2016 move this to a specific extend of the SAMLengine dedicated to validator
     */
    @Override
    @Deprecated
    public IRequestMessage generateEIDASAuthnRequestWithoutValidation(IAuthenticationRequest request)
            throws EIDASSAMLEngineException {
        return generateRequestMessage(request, false, true);
    }

    /**
     * This is used by the validator
     *
     * @// TODO: 13/05/2016 move this to a specific extend of the SAMLengine dedicated to validator
     */
    @Override
    @Deprecated
    public IRequestMessage generateEIDASAuthnRequestWithoutSign(IAuthenticationRequest request)
            throws EIDASSAMLEngineException {
        return generateRequestMessage(request, false, false);
    }

    /**
     * Resign authentication request ( for validation purpose).
     *
     * @param originalRequest
     * @param changeProtocol If true will update the protocol of the resigned request with the one within {@code
     * request}
     * @param changeDestination If true will update the destination of the resigned request with the one within {@code
     * request}
     * @return the resigned request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @// TODO: 13/05/2016 move this to a specific extend of the SAMLengine dedicated to validator
     */
    @Override
    public IRequestMessage resignEIDASAuthnRequest(IRequestMessage originalRequest, boolean changeDestination)
            throws EIDASSAMLEngineException {
        LOG.trace("Getting the saml token.");
        AuthnRequest authnRequestAux = null;
        // Obtaining new saml Token
        byte[] tokenSaml = originalRequest.getMessageBytes();
        IAuthenticationRequest authenticationRequest = originalRequest.getRequest();
        authnRequestAux = (AuthnRequest) unmarshall(tokenSaml);
        authnRequestAux.setProtocolBinding(
                getExtensionProcessor().getProtocolBinding(authenticationRequest, getCoreProperties()));
        if (changeDestination) {
            authnRequestAux.setDestination(authenticationRequest.getDestination());
        }

        // copy constructor && assignment
        LOG.trace("copy contructor and assigment of token.");
        try {
            IRequestMessage resignedAuthnRequest =
                    new BinaryRequestMessage(authenticationRequest, signAndMarshallRequest(authnRequestAux));
            return resignedAuthnRequest;
        } catch (EIDASSAMLEngineException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASAuthnRequest : Sign and Marshall.{}",
                     e.getMessage());
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASAuthnRequest : Sign and Marshall.{}", e);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    /**
     * Resign a request (for validation purpose).
     *
     * @return the resigned request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated this method is only used by the validator
     */
    @Override
    @Deprecated
    public byte[] reSignRequest(byte[] requestBytes) throws EIDASSAMLEngineException {
        LOG.trace("Generate SAMLAuthnRequest.");

        AuthnRequest authnRequest = null;

        authnRequest = (AuthnRequest) unmarshall(requestBytes);
        releaseExtensionsDom(authnRequest);
        if (null == authnRequest) {
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorCode(), "invalid AuthnRequest");
        }

        try {
            return signAndMarshallRequest(authnRequest);
        } catch (EIDASSAMLEngineException e) {
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASTokenSAML : Sign and Marshall.", e);
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : resignEIDASTokenSAML : Sign and Marshall.", e.getMessage());
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    private void releaseExtensionsDom(AuthnRequest authnRequestAux) {
        if (authnRequestAux.getExtensions() == null) {
            return;
        }
        authnRequestAux.getExtensions().releaseDOM();
        authnRequestAux.getExtensions().releaseChildrenDOM(true);
    }

    /**
     * Resigns the saml token checking previously if it is encrypted
     *
     * @param requestBytes
     * @return
     * @throws EIDASSAMLEngineException
     * @deprecated information missing about whom to encrypt the response for
     */
    @Override
    @Deprecated
    public byte[] checkAndResignRequest(@Nonnull byte[] requestBytes) throws EIDASSAMLEngineException {

        AuthnRequest request = (AuthnRequest) unmarshall(requestBytes);
        request = validateSignature(request);
        if (null == request) {
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(),
                                               "BUSINESS EXCEPTION : invalid SAML Request");
        }

        try {
            return signAndMarshallRequest(request);
        } catch (EIDASSAMLEngineException e) {
            LOG.error(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall: " + e, e);
            throw e;
        }
    }

    /**
     * Decrypt and validate saml respons
     *
     * @param responseBytes
     * @return
     * @throws EIDASSAMLEngineException
     */
    @Override
    public byte[] checkAndDecryptResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException {

        Response response = null;

        response = (Response) unmarshall(responseBytes);
        response = validateSignatureAndAssertionSignatures(response);
        if (null == response) {
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(),
                                               "BUSINESS EXCEPTION : invalid SAML Response");
        }

        try {
            return marshall(response);
        } catch (EIDASSAMLEngineException e) {
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall.", e);
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall.",
                     e.getMessage());
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }

    /**
     * Returns true when the input contains an encrypted SAML Response
     *
     * @param tokenSaml
     * @return
     * @throws EIDASSAMLEngineException
     */
    @Override
    public boolean isEncryptedSamlResponse(byte[] tokenSaml) throws EIDASSAMLEngineException {
        SignableSAMLObject samlObject = null;

        samlObject = (SignableSAMLObject) unmarshall(tokenSaml);
        if (samlObject instanceof Response) {
            Response response = (Response) samlObject;
            return response.getEncryptedAssertions() != null && !response.getEncryptedAssertions().isEmpty();
        }
        return false;

    }

    private boolean tryProcessExtensions(ExtensionProcessorI extensionProcessor, AuthnRequest samlRequest)
            throws ValidationException, EIDASSAMLEngineException {
        IAuthenticationRequest request = extensionProcessor.processExtensions("BE", samlRequest, null, null);
        //format discriminator goes here
        if (request != null) {
            boolean validRequest = extensionProcessor.isValidRequest(samlRequest);
            LOG.debug("tryProcessExtensions with " + extensionProcessor.getClass().getName() + " returns: "
                              + validRequest);
            return validRequest;
        }
        return false;
    }
}
