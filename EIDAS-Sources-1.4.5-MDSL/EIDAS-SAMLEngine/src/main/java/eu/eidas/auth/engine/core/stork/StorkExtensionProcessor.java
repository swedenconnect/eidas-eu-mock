/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core.stork;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.protocol.impl.SamlBindingUri;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.impl.StorkAuthenticationRequest;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.eidas.GenericEidasAttributeType;
import eu.eidas.auth.engine.xml.opensaml.*;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @deprecated since 1.1
 */
@Deprecated
@SuppressWarnings("all")
public class StorkExtensionProcessor implements ExtensionProcessorI {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StorkExtensionProcessor.class);

    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");

    public static final QName STORK_ATTRIBUTE_STATUS =
            new QName(SAMLCore.STORK10_NS.getValue(), "AttributeStatus", SAMLCore.STORK10_PREFIX.getValue());

    public static final QName STORK_REQUESTED_ATTRIBUTE_VALUE_TYPE =
            new QName(SAMLCore.STORK10_NS.getValue(), "AttributeValue", SAMLCore.STORK10_PREFIX.getValue());

    public static final QName XML_VALUE_TYPE =
            new QName(SAMLCore.STORK10_NS.getValue(), "XMLValue", SAMLCore.STORK10_PREFIX.getValue());

    private static final String STORK_REQUEST_VALIDATOR_SUITE_ID = "storkRequestValidatorSuiteId";

    @Override
    public String getRequestValidatorId() {
        return STORK_REQUEST_VALIDATOR_SUITE_ID;
    }

    private static final String STORK_RESPONSE_VALIDATOR_SUITE_ID = "storkResponseValidatorSuiteId";

    @Override
    public String getResponseValidatorId() {
        return STORK_RESPONSE_VALIDATOR_SUITE_ID;
    }

    public static final String STORK_IDENTIFIER = "http://www.stork.gov.eu/1.0/eIdentifier";

    private static final String STORK_ATTRIBUTES_FILE = "saml-engine-stork-attributes.xml";

    //TODO vargata - apply the default path when relocating this engine
    private static final AttributeRegistry STORK_ATTRIBUTE_REGISTRY =
            AttributeRegistries.fromFile(STORK_ATTRIBUTES_FILE, null);

    /**
     * The default instance only implements the STORK specification without any additional attribute.
     */
    public static final StorkExtensionProcessor INSTANCE =
            new StorkExtensionProcessor(STORK_ATTRIBUTE_REGISTRY,
                                        AttributeRegistries.empty());

    static {
        INSTANCE.configureExtension();
    }

    @Nonnull
    private final AttributeRegistry storkAttributeRegistry;

    @Nonnull
    private final AttributeRegistry additionalAttributeRegistry;

    public StorkExtensionProcessor(@Nonnull String storkAttributesFileName,
                                   @Nonnull String additionalAttributesFileName,
                                   @Nullable String defaultPath) {
        Preconditions.checkNotNull(storkAttributesFileName, "storkAttributesFileName");
        Preconditions.checkNotNull(additionalAttributesFileName, "additionalAttributesFileName");
        this.storkAttributeRegistry = AttributeRegistries.fromFile(storkAttributesFileName, defaultPath);
        this.additionalAttributeRegistry = AttributeRegistries.fromFile(additionalAttributesFileName, defaultPath);
    }

    public StorkExtensionProcessor(@Nonnull AttributeRegistry storkAttributeRegistry,
                                   @Nonnull AttributeRegistry additionalAttributeRegistry) {
        Preconditions.checkNotNull(storkAttributeRegistry, "storkAttributeRegistry");
        Preconditions.checkNotNull(additionalAttributeRegistry, "additionalAttributeRegistry");
        this.storkAttributeRegistry = storkAttributeRegistry;
        this.additionalAttributeRegistry = additionalAttributeRegistry;
    }

    public StorkExtensionProcessor() {
        this(STORK_ATTRIBUTE_REGISTRY, AttributeRegistries.empty());
    }

    private void validateExtension(final Extensions extensions) throws EIDASSAMLEngineException {
        if (extensions.getUnknownXMLObjects(RequestedAttributes.DEF_ELEMENT_NAME) == null) {
            LOG.info(AbstractProtocolEngine.SAML_EXCHANGE,
                     "BUSINESS EXCEPTION : Extensions not contains any requested attribute.");
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               "Extensions not contains any requested attribute.");
        }
    }

    @Override
    public void addRequestedAuthnContext(IAuthenticationRequest request, AuthnRequest authnRequestAux)
            throws EIDASSAMLEngineException {
        // nothing to do for STORK
    }

    @Nonnull
    @Override
    public AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest request,
                                        @Nonnull String serviceIssuer,
                                        @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

        // Validate mandatory parameters
        request = validateAuthenticationRequest(request, serviceIssuer);

        String id = SAMLEngineUtils.generateNCName();

        AuthnRequest samlRequest =
                BuilderFactoryUtil.generateAuthnRequest(id, SAMLVersion.VERSION_20, SAMLEngineUtils.getCurrentTime());

        // Set name spaces.
        registerRequestNamespace(samlRequest);

        // Add parameter Mandatory
        samlRequest.setForceAuthn(Boolean.TRUE);

        // Add parameter Mandatory
        samlRequest.setIsPassive(Boolean.FALSE);

        samlRequest.setAssertionConsumerServiceURL(request.getAssertionConsumerServiceURL());

        samlRequest.setProviderName(request.getProviderName());

        // Add protocol binding
        samlRequest.setProtocolBinding(getProtocolBinding(request, coreProperties));

        // Add parameter optional
        // Destination is mandatory
        // The application must to know the destination
        if (StringUtils.isNotBlank(request.getDestination())) {
            samlRequest.setDestination(request.getDestination());
        }

        // Consent is optional. Set from SAMLEngine.xml - consent.
        samlRequest.setConsent(coreProperties.getConsentAuthnRequest());

        Issuer issuer = BuilderFactoryUtil.generateIssuer();

        if (request.getIssuer() != null) {
            issuer.setValue(SAMLEngineUtils.getValidIssuerValue(request.getIssuer()));
        } else {
            issuer.setValue(coreProperties.getRequester());
        }

        // Optional
        String formatEntity = coreProperties.getFormatEntity();
        if (StringUtils.isNotBlank(formatEntity)) {
            issuer.setFormat(formatEntity);
        }

        samlRequest.setIssuer(issuer);
        addRequestedAuthnContext(request, samlRequest);

        // Generate format extensions.
        Extensions formatExtensions = generateExtensions(coreProperties, request);
        // add the extensions to the SAMLAuthnRequest
        samlRequest.setExtensions(formatExtensions);
        addNameIDPolicy(samlRequest, request.getNameIdFormat());

        return samlRequest;
    }

    private void addNameIDPolicy(AuthnRequest request, String selectedNameID) throws EIDASSAMLEngineException {
        // TODO: check this
//        if (StringUtils.isNotEmpty(selectedNameID)) {
//            NameIDPolicy policy = (NameIDPolicy) BuilderFactoryUtil.buildXmlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
//            policy.setFormat(selectedNameID);
//            policy.setAllowCreate(true);
//            request.setNameIDPolicy(policy);
//        }
    }

    /**
     * Process all elements XMLObjects from the extensions.
     *
     * @return the STORK authentication request
     * @throws EIDASSAMLEngineException the STORKSAML engine exception
     */
    @Override
    @Nonnull
    public IAuthenticationRequest processExtensions(@Nonnull String citizenCountryCode,
                                                    @Nonnull AuthnRequest samlRequest,
                                                    @Nonnull String originCountryCode,
                                                    @Nullable X509Certificate trustedCertificate)
            throws EIDASSAMLEngineException {
        return unmarshallRequest(citizenCountryCode, samlRequest, originCountryCode);
    }

    @Override
    @Nonnull
    public IAuthenticationRequest unmarshallRequest(@Nonnull String citizenCountryCode,
                                                    @Nonnull AuthnRequest samlRequest,
                                                    @Nonnull String originCountryCode)
            throws EIDASSAMLEngineException {
        LOG.debug("Process the extensions for Stork 1.0");
        Extensions extensions = samlRequest.getExtensions();
        validateExtension(extensions);

        QAAAttribute qaa = (QAAAttribute) extensions.getUnknownXMLObjects(QAAAttribute.DEF_ELEMENT_NAME).get(0);

        if (null == qaa) {
            LOG.trace("QAA was not found in extensions.");
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "QAA was not found in extensions.");
        }

        ImmutableAttributeMap.Builder attributeMapBuilder = new ImmutableAttributeMap.Builder();

        RequestedAttributes requestedAttr =
                (RequestedAttributes) extensions.getUnknownXMLObjects(RequestedAttributes.DEF_ELEMENT_NAME).get(0);
        List<RequestedAttribute> reqAttrs = requestedAttr.getAttributes();
        for (RequestedAttribute attribute : reqAttrs) {
            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNullable(attribute.getName());
            if (null == attributeDefinition) {
                LOG.info(AbstractProtocolEngine.SAML_EXCHANGE,
                         "BUSINESS EXCEPTION : Attribute name: {} was not found. It will be removed from the request object",
                         attribute.getName());
                continue;
            }

            // For STORK, isRequired comes from the Request, not from the registry
            boolean isRequired = attribute.isRequired();

            attributeDefinition = new AttributeDefinition.Builder(attributeDefinition).required(isRequired).build();

            // Gets the value
            List<String> outputValue = new ArrayList<String>();
            List<XMLObject> values = attribute.getOrderedChildren();
            for (int nextSimpleValue = 0; nextSimpleValue < values.size(); nextSimpleValue++) {
                // Process attributes simples. An AuthenticationRequest only
                // must contains simple values.
                XMLObject xmlObject = values.get(nextSimpleValue);
                if (xmlObject instanceof XSStringImpl) {
                    final XSStringImpl xmlString = (XSStringImpl) values.get(nextSimpleValue);
                    outputValue.add(xmlString.getValue());
                } else {
                    if ("http://www.stork.gov.eu/1.0/signedDoc".equals(attribute.getName())) {
                        outputValue.add(extractEDocValue((XSAnyImpl) values.get(nextSimpleValue)));
                    } else {
                        final XSAnyImpl xmlString = (XSAnyImpl) values.get(nextSimpleValue);
                        outputValue.add(xmlString.getTextContent());
                    }
                }
            }
            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
            ImmutableSet.Builder<eu.eidas.auth.commons.attribute.AttributeValue<?>> setBuilder = ImmutableSet.builder();
            for (final String value : outputValue) {
                eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue;
                try {
                    attributeValue = attributeValueMarshaller.unmarshal(value, false);
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("Illegal attribute value: " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), e);
                }
                setBuilder.add(attributeValue);
            }

            attributeMapBuilder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) setBuilder.build());
        }

        StorkAuthenticationRequest.Builder builder = new StorkAuthenticationRequest.Builder();
        builder.originCountryCode(originCountryCode);
        builder.assertionConsumerServiceURL(samlRequest.getAssertionConsumerServiceURL());
        builder.binding(SAMLEngineUtils.getBindingMethod(samlRequest.getProtocolBinding()));
        String citizenCountry = getNullableCitizenCodeFromExtension(extensions);
        if (null == citizenCountry) {
            citizenCountry = citizenCountryCode;
        }
        builder.citizenCountryCode(citizenCountry);
        builder.destination(samlRequest.getDestination());
        builder.id(samlRequest.getID());
        builder.issuer(samlRequest.getIssuer().getValue());
        builder.levelOfAssurance(qaa.getQaaLevel());
        builder.nameIdFormat(SamlNameIdFormat.UNSPECIFIED.toString());
        builder.providerName(samlRequest.getProviderName());
        builder.requestedAttributes(attributeMapBuilder.build());
        builder.serviceProviderCountryCode(getNullableSPCountryFromExtension(extensions));

        // STORK only
        builder.spApplication(getNullableSPApplicationFromExtension(extensions));
        builder.spId(getNullableSpIDFromExtension(extensions));
        builder.spSector(getNullableSPSectorFromExtension(extensions));
        // STORK only
        builder.eidCrossBorderShare(getNullableEIDCrossBorderShareFromExtension(extensions));
        builder.eidCrossSectorShare(getNullableEIDCrossSectorShareFromExtension(extensions));
        builder.eidSectorShare(getNullableEIDSectorShareFromExtension(extensions));
        // STORK only
        builder.qaa(Integer.parseInt(qaa.getQaaLevel()));

        return builder.build();
    }

    @Nullable
    private String getNullableSPSectorFromExtension(final Extensions extensions) {
        List optionalElements = extensions.getUnknownXMLObjects(SPSector.DEF_ELEMENT_NAME);

        if (!optionalElements.isEmpty()) {
            final SPSector sector = (SPSector) extensions.getUnknownXMLObjects(SPSector.DEF_ELEMENT_NAME).get(0);
            return sector.getSPSector();
        }
        return null;
    }

    @Nullable
    private String getNullableSPApplicationFromExtension(final Extensions extensions) {
        List optionalElements = extensions.getUnknownXMLObjects(SPApplication.DEF_ELEMENT_NAME);

        if (!optionalElements.isEmpty()) {
            final SPApplication application =
                    (SPApplication) extensions.getUnknownXMLObjects(SPApplication.DEF_ELEMENT_NAME).get(0);
            return application.getSPApplication();
        }
        return null;
    }

    @Nullable
    private String getNullableSPCountryFromExtension(final Extensions extensions) {
        List optionalElements = extensions.getUnknownXMLObjects(SPCountry.DEF_ELEMENT_NAME);

        if (!optionalElements.isEmpty()) {
            final SPCountry application =
                    (SPCountry) extensions.getUnknownXMLObjects(SPCountry.DEF_ELEMENT_NAME).get(0);
            return application.getSPCountry();
        }
        return null;
    }

    @Nullable
    private Boolean getNullableEIDCrossBorderShareFromExtension(final Extensions extensions) {
        List listCrossBorderShare = extensions.getUnknownXMLObjects(EIDCrossBorderShare.DEF_ELEMENT_NAME);

        if (!listCrossBorderShare.isEmpty()) {
            final EIDCrossBorderShare crossBorderShare = (EIDCrossBorderShare) listCrossBorderShare.get(0);
            return Boolean.parseBoolean(crossBorderShare.getEIDCrossBorderShare());
        }
        return null;

    }

    @Nullable
    private Boolean getNullableEIDCrossSectorShareFromExtension(final Extensions extensions) {
        List listCrosSectorShare = extensions.getUnknownXMLObjects(EIDCrossSectorShare.DEF_ELEMENT_NAME);

        if (!listCrosSectorShare.isEmpty()) {
            final EIDCrossSectorShare crossSectorShare = (EIDCrossSectorShare) listCrosSectorShare.get(0);
            return Boolean.parseBoolean(crossSectorShare.getEIDCrossSectorShare());
        }
        return null;
    }

    @Nullable
    private Boolean getNullableEIDSectorShareFromExtension(final Extensions extensions) {
        List listSectorShareExtension = extensions.getUnknownXMLObjects(EIDSectorShare.DEF_ELEMENT_NAME);
        if (!listSectorShareExtension.isEmpty()) {
            final EIDSectorShare sectorShare = (EIDSectorShare) listSectorShareExtension.get(0);
            return Boolean.parseBoolean(sectorShare.getEIDSectorShare());
        }
        return null;
    }

    @Nullable
    private String getNullableCitizenCodeFromExtension(final Extensions extensions) {
        List<XMLObject> authAttrs = extensions.getUnknownXMLObjects(AuthenticationAttributes.DEF_ELEMENT_NAME);

        if (authAttrs != null && !authAttrs.isEmpty()) {
            CitizenCountryCode citizenCountryCodeElement = null;
            final AuthenticationAttributes authnAttr = (AuthenticationAttributes) authAttrs.get(0);
            VIDPAuthenticationAttributes vidpAuthnAttr =
                    authnAttr == null ? null : authnAttr.getVIDPAuthenticationAttributes();
            if (vidpAuthnAttr != null) {
                citizenCountryCodeElement = vidpAuthnAttr.getCitizenCountryCode();
            }

            String citizenCountryCode = null;
            if (citizenCountryCodeElement != null) {
                citizenCountryCode = citizenCountryCodeElement.getCitizenCountryCode();
            }
            return citizenCountryCode;
        }
        return null;
    }

    @Nullable
    private String getNullableSpIDFromExtension(final Extensions extensions) {
        List<XMLObject> authAttrs = extensions.getUnknownXMLObjects(AuthenticationAttributes.DEF_ELEMENT_NAME);
        if (authAttrs != null && !authAttrs.isEmpty()) {
            final AuthenticationAttributes authnAttr = (AuthenticationAttributes) authAttrs.get(0);
            VIDPAuthenticationAttributes vidpAuthnAttr =
                    authnAttr == null ? null : authnAttr.getVIDPAuthenticationAttributes();
            SPInformation spInformation = vidpAuthnAttr == null ? null : vidpAuthnAttr.getSPInformation();
            SPID spidElement = null;
            if (spInformation != null) {
                spidElement = spInformation.getSPID();
            }

            String spid = null;
            if (spidElement != null) {
                spid = spidElement.getSPID();
            }
            return spid;
        }
        return null;
    }

    private String extractEDocValue(final XSAnyImpl xmlString) {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (TransformerConfigurationException e) {
            LOG.warn(AbstractProtocolEngine.SAML_EXCHANGE, "Error transformer configuration exception", e.getMessage());
            LOG.debug(AbstractProtocolEngine.SAML_EXCHANGE, "Error transformer configuration exception", e);
        }
        StringWriter buffer = new StringWriter();
        try {
            if (transformer != null && xmlString != null && xmlString.getUnknownXMLObjects() != null
                    && !xmlString.getUnknownXMLObjects().isEmpty()) {
                transformer.transform(new DOMSource(xmlString.getUnknownXMLObjects().get(0).getDOM()),
                                      new StreamResult(buffer));
            }
        } catch (TransformerException e) {
            LOG.info(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Error transformer exception",
                     e.getMessage());
            LOG.debug(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Error transformer exception", e);
        }
        return buffer.toString();

    }

    /**
     * Generate stork extensions.
     *
     * @param samlCoreProperties
     * @param authRequest the request
     * @return the extensions
     * @throws EIDASSAMLEngineException the STORKSAML engine exception
     */
    @Override
    public Extensions generateExtensions(SamlEngineCoreProperties samlCoreProperties,
                                         IAuthenticationRequest authRequest) throws EIDASSAMLEngineException {
        LOG.trace("Generate STORKExtensions");

        IStorkAuthenticationRequest request = (IStorkAuthenticationRequest) authRequest;

        Extensions extensions = BuilderFactoryUtil.generateExtension();

        LOG.trace("Generate QAAAttribute");
        QAAAttribute qaaAttribute = generateQAAAttribute(request.getQaa());
        extensions.getUnknownXMLObjects().add(qaaAttribute);

        addExtensionSpSector(request, extensions);

        addExtensionSPInstitution(request, extensions);

        addExtensionSpApplication(request, extensions);
        addExtensionSpCountry(request, extensions);

        //eIDSectorShare: optional; default value: false.
        String valueSectorShare = samlCoreProperties.isEidSectorShare();

        if (StringUtils.isNotEmpty(valueSectorShare)) {
            // Add information about the use of the SAML message.
            LOG.trace("Generate EIDSectorShare");
            EIDSectorShare eIdSectorShare =
                    (EIDSectorShare) BuilderFactoryUtil.buildXmlObject(EIDSectorShare.DEF_ELEMENT_NAME);

            eIdSectorShare.setEIDSectorShare(String.valueOf(Boolean.valueOf(valueSectorShare)));

            extensions.getUnknownXMLObjects().add(eIdSectorShare);
        }

        String valueCrossSectorShare = samlCoreProperties.isEidCrossSectorShare();

        if (StringUtils.isNotEmpty(valueCrossSectorShare)) {
            LOG.trace("Generate EIDCrossSectorShare");
            EIDCrossSectorShare eIdCrossSecShare =
                    (EIDCrossSectorShare) BuilderFactoryUtil.buildXmlObject(EIDCrossSectorShare.DEF_ELEMENT_NAME);
            eIdCrossSecShare.setEIDCrossSectorShare(String.valueOf(Boolean.valueOf(valueCrossSectorShare)));
            extensions.getUnknownXMLObjects().add(eIdCrossSecShare);
        }

        String valueCrossBorderShare = samlCoreProperties.isEidCrossBorderShare();

        if (StringUtils.isNotEmpty(valueCrossBorderShare)) {
            LOG.trace("Generate EIDCrossBorderShare");
            EIDCrossBorderShare eIdCrossBordShare =
                    (EIDCrossBorderShare) BuilderFactoryUtil.buildXmlObject(EIDCrossBorderShare.DEF_ELEMENT_NAME);
            eIdCrossBordShare.setEIDCrossBorderShare(String.valueOf(Boolean.valueOf(valueCrossBorderShare)));
            extensions.getUnknownXMLObjects().add(eIdCrossBordShare);
        }

        // Add information about requested attributes.
        LOG.trace("Generate RequestedAttributes.");
        RequestedAttributes reqAttributes =
                (RequestedAttributes) BuilderFactoryUtil.buildXmlObject(RequestedAttributes.DEF_ELEMENT_NAME);

        fillRequestedAttributes(request, reqAttributes);
        // Add requested attributes.
        extensions.getUnknownXMLObjects().add(reqAttributes);

        CitizenCountryCode citizenCountryCode = null;
        if (request.getCitizenCountryCode() != null && StringUtils.isNotBlank(request.getCitizenCountryCode())) {
            LOG.trace("Generate CitizenCountryCode");
            citizenCountryCode =
                    (CitizenCountryCode) BuilderFactoryUtil.buildXmlObject(CitizenCountryCode.DEF_ELEMENT_NAME);

            citizenCountryCode.setCitizenCountryCode(request.getCitizenCountryCode().toUpperCase(Locale.ENGLISH));
        }

        SPID spid = null;
        if (request.getSpId() != null && StringUtils.isNotBlank(request.getSpId())) {
            LOG.trace("Generate SPID");
            spid = (SPID) BuilderFactoryUtil.buildXmlObject(SPID.DEF_ELEMENT_NAME);

            spid.setSPID(request.getSpId().toUpperCase(Locale.ENGLISH));
        }

        AuthenticationAttributes authenticationAttr =
                (AuthenticationAttributes) BuilderFactoryUtil.buildXmlObject(AuthenticationAttributes.DEF_ELEMENT_NAME);
        // Regarding the specs & xsd, the SPID can be absent
        if (spid != null) {
            VIDPAuthenticationAttributes vIDPauthenticationAttr =
                    (VIDPAuthenticationAttributes) BuilderFactoryUtil.buildXmlObject(
                            VIDPAuthenticationAttributes.DEF_ELEMENT_NAME);

            SPInformation spInformation =
                    (SPInformation) BuilderFactoryUtil.buildXmlObject(SPInformation.DEF_ELEMENT_NAME);

            if (citizenCountryCode != null) {
                vIDPauthenticationAttr.setCitizenCountryCode(citizenCountryCode);
            }

            spInformation.setSPID(spid);

            vIDPauthenticationAttr.setSPInformation(spInformation);

            authenticationAttr.setVIDPAuthenticationAttributes(vIDPauthenticationAttr);
        }
        extensions.getUnknownXMLObjects().add(authenticationAttr);

        return extensions;

    }

    /**
     * Generate the quality authentication assurance level.
     *
     * @param qaal the level of quality authentication assurance.
     * @return the quality authentication assurance attribute
     * @throws EIDASSAMLEngineException the STORKSAML engine exception
     */
    public static QAAAttribute generateQAAAttribute(final int qaal) throws EIDASSAMLEngineException {
        LOG.debug("Generate QAAAttribute.");
        XMLObject obj = BuilderFactoryUtil.buildXmlObject(QAAAttribute.DEF_ELEMENT_NAME);
        final QAAAttribute qaaAttribute = (QAAAttribute) obj;
        qaaAttribute.setQaaLevel(String.valueOf(qaal));
        return qaaAttribute;
    }

    private static void addExtensionSpSector(IStorkAuthenticationRequest request, Extensions extensions)
            throws EIDASSAMLEngineException {
        if (StringUtils.isNotEmpty(request.getSpSector())) {
            // Add information about service provider.
            LOG.trace("Generate SPSector");
            final SPSector sector = generateSPSector(request.getSpSector());
            extensions.getUnknownXMLObjects().add(sector);
        }

    }

    private static void addExtensionSPInstitution(IStorkAuthenticationRequest request, Extensions extensions)
            throws EIDASSAMLEngineException {
        //Delete from specification. Kept for compatibility with Provider Name value
        LOG.trace("Generate SPInstitution");
        final SPInstitution institution = generateSPInstitution(request.getProviderName());
        extensions.getUnknownXMLObjects().add(institution);
    }

    private static void addExtensionSpApplication(IStorkAuthenticationRequest request, Extensions extensions)
            throws EIDASSAMLEngineException {
        if (StringUtils.isNotEmpty(request.getSpApplication())) {
            LOG.trace("Generate SPApplication");
            final SPApplication application = generateSPApplication(request.getSpApplication());
            extensions.getUnknownXMLObjects().add(application);
        }
    }

    private static void addExtensionSpCountry(IStorkAuthenticationRequest request, Extensions extensions)
            throws EIDASSAMLEngineException {
        if (StringUtils.isNotEmpty(request.getServiceProviderCountryCode())) {
            LOG.trace("Generate SPCountry");
            final SPCountry country = generateSPCountry(request.getServiceProviderCountryCode());
            extensions.getUnknownXMLObjects().add(country);
        }
    }

    protected void fillRequestedAttributes(IAuthenticationRequest request, RequestedAttributes reqAttributes)
            throws EIDASSAMLEngineException {
        LOG.trace("SAML Engine configuration properties load.");

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : request
                .getRequestedAttributes()
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();

            AttributeDefinition<?> requestedAttribute = getRequestedAttribute(attributeDefinition);

            // Verify if the attribute name exists.
            if (null == requestedAttribute) {
                LOG.trace("Attribute name: {} was not found.", attributeDefinition.getNameUri());
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   "Attribute name: " + attributeDefinition.getNameUri()
                                                           + " was not found.");
            }

            LOG.trace("Generate requested attribute: " + requestedAttribute);
            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (final eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue : entry.getValue()) {
                try {
                    String marshalledValue = attributeValueMarshaller.marshal((eu.eidas.auth.commons.attribute.AttributeValue)attributeValue);
                    builder.add(marshalledValue);
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("Illegal attribute value: " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                       e);
                }
            }

            RequestedAttribute requestedAttr = generateReqAuthnAttributeSimple(requestedAttribute, builder.build());

            // Add requested attribute.
            reqAttributes.getAttributes().add(requestedAttr);
        }
    }

    /**
     * Generate requested attribute.
     *
     * @param attributeDefinition the attributeDefinition
     * @param values the value
     * @return the requested attribute
     */
    protected static RequestedAttribute generateReqAuthnAttributeSimple(AttributeDefinition<?> attributeDefinition,
                                                                        Set<String> values)
            throws EIDASSAMLEngineException {
        LOG.debug("Generate the requested attribute.");

        RequestedAttribute requested =
                (RequestedAttribute) BuilderFactoryUtil.buildXmlObject(RequestedAttribute.DEF_ELEMENT_NAME);
        requested.setName(attributeDefinition.getNameUri().toASCIIString());
        requested.setFriendlyName(attributeDefinition.getFriendlyName());
        requested.setNameFormat(RequestedAttribute.URI_REFERENCE);
        requested.setIsRequired(String.valueOf(attributeDefinition.isRequired()));
        fillRequestAttributeValues(attributeDefinition.getNameUri().toASCIIString(), values,
                                   requested.getAttributeValues());
        // The value is optional in an authentication request.

        return requested;
    }

    protected static void fillRequestAttributeValues(String name, Set<String> values, List<XMLObject> attributeValues) {
        if (!values.isEmpty()) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {

                    if (!(SAMLCore.STORK10_BASE_URI.getValue() + "signedDoc").equals(name)) {

                        // Create the attribute statement
                        final XSAny attrValue =
                                (XSAny) BuilderFactoryUtil.buildXmlObject(STORK_REQUESTED_ATTRIBUTE_VALUE_TYPE,
                                                                          XSAny.TYPE_NAME);

                        attrValue.setTextContent(value.trim());
                        attributeValues.add(attrValue);

                    } else {
                        parseSignedDoc(attributeValues, value);

                    }

                }
            }
        }

    }

    private static void parseSignedDoc(List<XMLObject> attributeValues, String value) {
        Document document;

        // Parse the signedDoc value into an XML DOM Document
        try {
            document = DocumentBuilderFactoryUtil.parse(value);
        } catch (SAXException e1) {
            LOG.info("ERROR : SAX Error while parsing signModule attribute", e1.getMessage());
            LOG.debug("ERROR : SAX Error while parsing signModule attribute", e1);
            throw new EIDASSAMLEngineRuntimeException(e1);
        } catch (ParserConfigurationException e2) {
            LOG.info("ERROR : Parser Configuration Error while parsing signModule attribute", e2.getMessage());
            LOG.debug("ERROR : Parser Configuration Error while parsing signModule attribute", e2);
            throw new EIDASSAMLEngineRuntimeException(e2);
        } catch (IOException e4) {
            LOG.info("ERROR : IO Error while parsing signModule attribute", e4.getMessage());
            LOG.debug("ERROR : IO Error while parsing signModule attribute", e4);
            throw new EIDASSAMLEngineRuntimeException(e4);
        }

        // Create the XML statement(this will be overwritten with the previous DOM structure)
        final XSAny xmlValue = (XSAny) BuilderFactoryUtil.buildXmlObject(XML_VALUE_TYPE, XSAny.TYPE_NAME);

        //Set the signedDoc XML content to this element
        xmlValue.setDOM(document.getDocumentElement());

        // Create the attribute statement
        final XSAny attrValue =
                (XSAny) BuilderFactoryUtil.buildXmlObject(STORK_REQUESTED_ATTRIBUTE_VALUE_TYPE, XSAny.TYPE_NAME);

        //Add previous signedDocXML to the AttributeValue Element
        attrValue.getUnknownXMLObjects().add(xmlValue);

        attributeValues.add(attrValue);
    }

    @Override
    public SAMLExtensionFormat getFormat() {
        return SAMLExtensionFormat.STORK10;
    }

    @Override
    public void configureExtension() {
        StorkExtensionConfiguration.configureExtension();
    }

    @Override
    public AttributeRegistry getMinimumDataSetAttributes() {
        return storkAttributeRegistry;
    }

    @Override
    public AttributeRegistry getAdditionalAttributes() {
        return additionalAttributeRegistry;
    }

    @Override
    public ImmutableSortedSet<AttributeDefinition<?>> getAllSupportedAttributes() {
        AttributeRegistry minimumDataSetAttributes = getMinimumDataSetAttributes();
        AttributeRegistry additionalAttributes = getAdditionalAttributes();
        ImmutableSortedSet.Builder<AttributeDefinition<?>> builder =
                new ImmutableSortedSet.Builder<>(Ordering.<AttributeDefinition<?>>natural());
        builder.addAll(minimumDataSetAttributes.getAttributes());
        builder.addAll(additionalAttributes.getAttributes());
        return builder.build();
    }

    @Override
    public boolean isValidRequest(AuthnRequest request) {
        return true;
    }

    /**
     * Validate parameters from authentication request.
     *
     * @param request the request.
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Nonnull
    @Override
    public IAuthenticationRequest validateAuthenticationRequest(@Nonnull IAuthenticationRequest request, @Nonnull String serviceIssuer) throws EIDASSAMLEngineException {
        LOG.trace("Validate parameters from authentication request.");

        if (!(request instanceof IStorkAuthenticationRequest)) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "ProtocolEngine: Request does not implement IEidasAuthenticationRequest: "
                                                       + request);
        }

        // URL to which Authentication Response must be sent.
        if (StringUtils.isBlank(request.getAssertionConsumerServiceURL())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "ProtocolEngine: Assertion Consumer Service URL is mandatory.");
        }

        // the name of the original service provider requesting the
        // authentication.
        if (StringUtils.isBlank(request.getProviderName())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "ProtocolEngine: Service Provider is mandatory.");
        }

        // object that contain all attributes requesting.
        if (request.getRequestedAttributes() == null || request.getRequestedAttributes().isEmpty()) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "attributeQueries is null or empty.");
        }

        IStorkAuthenticationRequest storkAuthenticationRequest = (IStorkAuthenticationRequest) request;

        // Quality authentication assurance level.
        int qaa = storkAuthenticationRequest.getQaa();
        if (qaa < QAAAttribute.MIN_VALUE || qaa > QAAAttribute.MAX_VALUE) {
            throw new EIDASSAMLEngineException(EidasErrorKey.QAALEVEL.errorCode(), EidasErrorKey.QAALEVEL.errorCode(),
                                               "QAA level: " + qaa + ", is invalid.");
        }

        return request;
    }

    @Override
    public void checkRequestSanity(IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (StringUtils.isBlank(request.getIssuer())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Request Issuer must not be blank.");
        }

        if (StringUtils.isBlank(request.getAssertionConsumerServiceURL())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Request AssertionConsumerServiceURL must not be blank.");
        }

        if (StringUtils.isBlank(request.getId())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                               "Request ID must not be blank.");
        }
    }

    @Override
    @Nonnull
    public Attribute generateAttrSimple(@Nonnull AttributeDefinition<?> attributeDefinition,
                                        @Nonnull Collection<String> values) throws EIDASSAMLEngineException {
        LOG.trace("Generate attribute simple: {}", attributeDefinition.toString());
        final Attribute attribute = (Attribute) BuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);

        String name = attributeDefinition.getNameUri().toASCIIString();
        attribute.setName(name);
        attribute.setFriendlyName(attributeDefinition.getFriendlyName());
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        attribute.getUnknownAttributes()
                .put(STORK_ATTRIBUTE_STATUS, StorkAttributeStatus.fromValues(values).getValue());

        if (null != values && !values.isEmpty()) {
            LOG.trace("Add attribute values.");
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    XSAny attrValue;
                    if (!"http://www.stork.gov.eu/1.0/signedDoc".equals(name)) {
                        // Create the attribute statement
                        attrValue = createAttributeValueForNonSignedDoc(value);
                    } else {
                        attrValue = createAttributeValueForSignedDoc(value);
                        attribute.getAttributeValues().add(attrValue);
                    }
                    attribute.getAttributeValues().add(attrValue);
                }
            }
        }
        return attribute;
    }

    @Override
    public Attribute generateAttrComplex(AttributeDefinition<?> attributeDefinition,
                                         String status,
                                         Map<String, String> values,
                                         boolean isHashing) throws EIDASSAMLEngineException {
        LOG.debug("Generate attribute complex: " + attributeDefinition.toString());
        final Attribute attribute = (Attribute) BuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);

        attribute.setName(attributeDefinition.getNameUri().toASCIIString());
        attribute.setFriendlyName(attributeDefinition.getFriendlyName());
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        attribute.getUnknownAttributes().put(STORK_ATTRIBUTE_STATUS, status);

        if (!values.isEmpty()) {
            LOG.debug("Add attribute values.");

            // Create an attribute that contains all XSAny elements.
            final XSAny attrValue =
                    (XSAny) BuilderFactoryUtil.buildXmlObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);

            final Iterator<Map.Entry<String, String>> iterator = values.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<String, String> pairs = iterator.next();

                final String value = pairs.getValue();

                if (StringUtils.isNotBlank(value)) {
                    // Create the attribute statement
                    final XSAny attrValueSimple = (XSAny) BuilderFactoryUtil.buildXmlObject(
                            new QName(SAMLCore.STORK10_NS.getValue(), pairs.getKey(),
                                      SAMLCore.STORK10_PREFIX.getValue()), XSAny.TYPE_NAME);

                    // if it's necessary encode the information.
                    if (isHashing) {
                        attrValueSimple.setTextContent(SAMLEngineUtils.encode(value, SAMLEngineUtils.SHA_512));
                    } else {
                        attrValueSimple.setTextContent(value);
                    }

                    attrValue.getUnknownXMLObjects().add(attrValueSimple);
                    attribute.getAttributeValues().add(attrValue);
                }
            }

        }
        return attribute;
    }

    private XSAny createAttributeValueForNonSignedDoc(String value) throws EIDASSAMLEngineException {
        // Create the attribute statement
        final XSAny attrValue =
                (XSAny) BuilderFactoryUtil.buildXmlObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);
        attrValue.setTextContent(value);
        if (ProtocolEngine.needsTransliteration(value)) {
            attrValue.getUnknownAttributes().put(new QName("LatinScript"), "false");
        }
        return attrValue;
    }

    private XSAny createAttributeValueForSignedDoc(String value) throws EIDASSAMLEngineException {
        Document document;
        // Parse the signedDoc value into an XML DOM Document
        try {
            document = DocumentBuilderFactoryUtil.parse(value);
        } catch (SAXException e1) {
            LOG.info(ProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : SAX Error while parsing signModule attribute",
                     e1.getMessage());
            LOG.debug(ProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : SAX Error while parsing signModule attribute",
                      e1);
            throw new EIDASSAMLEngineRuntimeException(e1);
        } catch (ParserConfigurationException e2) {
            LOG.info(ProtocolEngine.SAML_EXCHANGE,
                     "BUSINESS EXCEPTION : Parser Configuration Error while parsing signModule attribute",
                     e2.getMessage());
            LOG.debug(ProtocolEngine.SAML_EXCHANGE,
                      "BUSINESS EXCEPTION : Parser Configuration Error while parsing signModule attribute", e2);
            throw new EIDASSAMLEngineRuntimeException(e2);
        } catch (IOException e4) {
            LOG.info(ProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : IO Error while parsing signModule attribute",
                     e4.getMessage());
            LOG.debug(ProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : IO Error while parsing signModule attribute", e4);
            throw new EIDASSAMLEngineRuntimeException(e4);
        }

        // Create the attribute statement
        final XSAny xmlValue =
                (XSAny) BuilderFactoryUtil.buildXmlObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);

        //Set the signedDoc XML content to this element
        xmlValue.setDOM(document.getDocumentElement());

        // Create the attribute statement
        final XSAny attrValue =
                (XSAny) BuilderFactoryUtil.buildXmlObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);

        //Add previous signedDocXML to the AttributeValue Element
        attrValue.getUnknownXMLObjects().add(xmlValue);
        return attrValue;
    }

    @Override
    @Nullable
    public AttributeDefinition<?> getAttributeDefinitionNullable(@Nonnull String name) {
        if (StringUtils.isBlank(name)) {
            LOG.info(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : {}", ProtocolEngine.ATTRIBUTE_EMPTY_LITERAL);
            throw new EIDASSAMLEngineRuntimeException(ProtocolEngine.ATTRIBUTE_EMPTY_LITERAL);
        }
        AttributeDefinition<?> attributeDefinition = getMinimumDataSetAttributes().getByName(name);
        if (null != attributeDefinition) {
            return attributeDefinition;
        }
        attributeDefinition = getAdditionalAttributes().getByName(name);
        if (null != attributeDefinition) {
            return attributeDefinition;
        }

        return null;
    }

    /**
     * In STORK, the "required" flag is set by the request not by the registry.
     *
     * @param requestedAttribute the requested attribute definition
     */
    @Nullable
    public AttributeDefinition<?> getRequestedAttribute(@Nonnull AttributeDefinition<?> requestedAttribute) {
        // Apply the changes in the request and customize the registry accordingly
        URI requestedNameUri = requestedAttribute.getNameUri();
        AttributeDefinition<?> existingAttribute = getMinimumDataSetAttributes().getByName(requestedNameUri);
        if (null == existingAttribute) {
            existingAttribute = getAdditionalAttributes().getByName(requestedNameUri);
        }
        if (null == existingAttribute) {
            return null;
        }
        return customizeDefinition(existingAttribute, requestedAttribute);
    }

    private AttributeDefinition<?> customizeDefinition(@Nonnull AttributeDefinition<?> existingAttribute,
                                                    @Nonnull AttributeDefinition<?> requestedAttribute) {
        if (existingAttribute.isRequired() != requestedAttribute.isRequired()) {
            return AttributeDefinition.builder(existingAttribute).required(requestedAttribute.isRequired()).build();
        }
        return existingAttribute;
    }

    @Override
    public String getProtocolBinding(@Nonnull IAuthenticationRequest request,
                                     @Nonnull SamlEngineCoreProperties defaultValues) {
        String bindingName = request.getBinding();
        if (null != bindingName) {
            EidasSamlBinding eidasSamlBinding = EidasSamlBinding.fromName(bindingName);
            if (null != eidasSamlBinding) {
                SamlBindingUri bindingUri = eidasSamlBinding.getBindingUri();
                if (null != bindingUri) {
                    return bindingUri.getBindingUri();
                }
                return null;
            }
        }
        // use default
        return defaultValues.getProtocolBinding();
    }


    @Override
    public boolean checkMandatoryAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap) {
        if (null == immutableAttributeMap || immutableAttributeMap.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    @Nonnull
    public IAuthenticationRequest updateRequestWithConsent(@Nonnull IAuthenticationRequest authnRequest,
                                                           @Nonnull ImmutableAttributeMap consentedAttributes) {
        return StorkAuthenticationRequest.builder((IStorkAuthenticationRequest) authnRequest)
                .requestedAttributes(consentedAttributes)
                .build();
    }

    @Override
    public void registerRequestNamespace(@Nonnull XMLObject xmlObject) {
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlObject.getNamespaceManager().registerNamespace(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));

        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLCore.STORK10_NS.getValue(), SAMLCore.STORK10_PREFIX.getValue()));
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLCore.STORK10P_NS.getValue(), SAMLCore.STORK10P_PREFIX.getValue()));
    }

    @Nullable
    @Override
    public X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException {
        return null;
    }

    @Nullable
    @Override
    public X509Certificate getRequestSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
        return null;
    }

    @Nullable
    @Override
    public X509Certificate getResponseSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
        return null;
    }

    /**
     * Generate service provider application.
     *
     * @param spApplication the service provider application
     * @return the sP application
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private static SPApplication generateSPApplication(final String spApplication) throws EIDASSAMLEngineException {
        LOG.debug("Generate SPApplication.");

        final SPApplication applicationAttr =
                (SPApplication) BuilderFactoryUtil.buildXmlObject(SPApplication.DEF_ELEMENT_NAME);
        applicationAttr.setSPApplication(spApplication);
        return applicationAttr;
    }

    /**
     * Generate service provider country.
     *
     * @param spCountry the service provider country
     * @return the service provider country
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private static SPCountry generateSPCountry(final String spCountry) throws EIDASSAMLEngineException {
        LOG.debug("Generate SPApplication.");

        final SPCountry countryAttribute = (SPCountry) BuilderFactoryUtil.buildXmlObject(SPCountry.DEF_ELEMENT_NAME);
        countryAttribute.setSPCountry(spCountry);
        return countryAttribute;
    }

    /**
     * Generate service provider institution.
     *
     * @param spInstitution the service provider institution
     * @return the service provider institution
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private static SPInstitution generateSPInstitution(final String spInstitution) throws EIDASSAMLEngineException {
        LOG.debug("Generate SPInstitution.");

        final SPInstitution institutionAttr =
                (SPInstitution) BuilderFactoryUtil.buildXmlObject(SPInstitution.DEF_ELEMENT_NAME);
        institutionAttr.setSPInstitution(spInstitution);
        return institutionAttr;
    }

    /**
     * Generate service provider sector.
     *
     * @param spSector the service provider sector
     * @return the service provider sector
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    private static SPSector generateSPSector(final String spSector) throws EIDASSAMLEngineException {
        LOG.debug("Generate SPSector.");

        final SPSector sectorAttribute = (SPSector) BuilderFactoryUtil.buildXmlObject(SPSector.DEF_ELEMENT_NAME);
        sectorAttribute.setSPSector(spSector);
        return sectorAttribute;
    }

    @Override
    public boolean isAcceptableHttpRequest(IAuthenticationRequest authnRequest, String httpMethod)
            throws EIDASSAMLEngineException {
        return true;
    }

    @Nullable
    @Override
    public String getServiceUrl(@Nonnull String issuer, @Nonnull SamlBindingUri bindingUri)
            throws EIDASSAMLEngineException {
        return null;
    }

    @Nonnull
    @Override
    public Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                          @Nonnull IAuthenticationResponse response,
                                          @Nonnull String ipAddress,
                                          @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

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
        Response responseFail =
                newResponse(status, request.getAssertionConsumerServiceURL(), request.getId(), coreProperties);

        String responseIssuer = response.getIssuer();
        if (responseIssuer != null && !responseIssuer.isEmpty()) {
            responseFail.getIssuer().setValue(responseIssuer);
        }
        DateTime notOnOrAfter = new DateTime();

        notOnOrAfter = notOnOrAfter.plusSeconds(coreProperties.getTimeNotOnOrAfter());

        Assertion assertion =
                AssertionUtil.generateResponseAssertion(true, ipAddress, request, responseFail.getIssuer(),
                                                        ImmutableAttributeMap.of(), notOnOrAfter,
                                                        coreProperties.getFormatEntity(), coreProperties.getResponder(),
                                                        getFormat(), coreProperties.isOneTimeUse());
        addResponseAuthnContextClassRef(response, assertion);
        responseFail.getAssertions().add(assertion);

        return responseFail;

    }

    @Nonnull
    public IAuthenticationResponse unmarshallErrorResponse(@Nonnull IAuthenticationResponse errorResponse,
                                                           @Nonnull Response samlErrorResponse,
                                                           @Nonnull String ipAddress,
                                                           @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {
        AuthenticationResponse.Builder builder = new AuthenticationResponse.Builder();
        builder.id(samlErrorResponse.getID());
        builder.issuer(samlErrorResponse.getIssuer().getValue());
        builder.ipAddress(ipAddress);
        builder.inResponseTo(samlErrorResponse.getInResponseTo());
        builder.responseStatus(ResponseUtil.extractResponseStatus(samlErrorResponse));
        return builder.build();
    }

    /**
     * Register the namespace on the response SAML xml token
     *
     * @param xmlObject
     */
    public void registerResponseNamespace(@Nonnull XMLObject xmlObject) {
        LOG.trace("Set namespaces.");
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlObject.getNamespaceManager().registerNamespace(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));

        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLCore.STORK10_NS.getValue(), SAMLCore.STORK10_PREFIX.getValue()));
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLCore.STORK10P_NS.getValue(), SAMLCore.STORK10P_PREFIX.getValue()));

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
     * Generates authentication response in one of the supported formats.
     *
     * @param request the request
     * @param response the authentication response from the IdP
     * @param ipAddress the IP address
     * @return the authentication response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    @Nonnull
    public Response marshallResponse(@Nonnull IAuthenticationRequest request,
                                     @Nonnull IAuthenticationResponse response,
                                     @Nonnull String ipAddress,
                                     @Nonnull SamlEngineCoreProperties coreProperties) throws EIDASSAMLEngineException {
        LOG.trace("marshallResponse");

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

        Response samlResponse =
                newResponse(status, request.getAssertionConsumerServiceURL(), request.getId(), coreProperties);

        if (StringUtils.isNotBlank(response.getIssuer()) && null != samlResponse.getIssuer()) {
            samlResponse.getIssuer().setValue(SAMLEngineUtils.getValidIssuerValue(response.getIssuer()));
        }
        DateTime notOnOrAfter = new DateTime();

        notOnOrAfter = notOnOrAfter.plusSeconds(coreProperties.getTimeNotOnOrAfter().intValue());

        Assertion assertion =
                AssertionUtil.generateResponseAssertion(false, ipAddress, request, samlResponse.getIssuer(),
                                                        response.getAttributes(), notOnOrAfter,
                                                        coreProperties.getFormatEntity(), coreProperties.getResponder(),
                                                        getFormat(), coreProperties.isOneTimeUse());

        AttributeStatement attrStatement = generateResponseAttributeStatement(response.getAttributes());

        assertion.getAttributeStatements().add(attrStatement);

        addResponseAuthnContextClassRef(response, assertion);

        samlResponse.getAssertions().add(assertion);

        return samlResponse;
    }

    /**
     * Instantiates a new authentication response.
     *
     * @param status the status
     * @param assertConsumerURL the assert consumer URL.
     * @param inResponseTo the in response to
     * @return the response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Nonnull
    private Response newResponse(@Nonnull Status status,
                                 @Nullable String assertConsumerURL,
                                 @Nonnull String inResponseTo,
                                 @Nonnull SamlEngineCoreProperties coreProperties) throws EIDASSAMLEngineException {
        LOG.debug("Generate Authentication Response base.");
        Response response =
                BuilderFactoryUtil.generateResponse(SAMLEngineUtils.generateNCName(), SAMLEngineUtils.getCurrentTime(),
                                                    status);

        // Set name Spaces
        registerResponseNamespace(response);

        // Mandatory EIDAS
        LOG.debug("Generate Issuer");
        Issuer issuer = BuilderFactoryUtil.generateIssuer();
        issuer.setValue(coreProperties.getResponder());

        // Format Entity Optional EIDAS
        issuer.setFormat(coreProperties.getFormatEntity());

        response.setIssuer(issuer);

        // destination Mandatory EIDAS
        if (assertConsumerURL != null) {
            response.setDestination(assertConsumerURL.trim());
        }

        // inResponseTo Mandatory
        response.setInResponseTo(inResponseTo.trim());

        // Optional
        response.setConsent(coreProperties.getConsentAuthnResponse());

        return response;
    }

    private void addResponseAuthnContextClassRef(@Nonnull IAuthenticationResponse response,
                                                 @Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        if (!StringUtils.isEmpty(response.getLevelOfAssurance())) {
            AuthnContextClassRef authnContextClassRef =
                    assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef();
            if (authnContextClassRef == null) {
                authnContextClassRef = (AuthnContextClassRef) BuilderFactoryUtil.buildXmlObject(
                        AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
                assertion.getAuthnStatements().get(0).getAuthnContext().setAuthnContextClassRef(authnContextClassRef);
            }
            authnContextClassRef.setAuthnContextClassRef(response.getLevelOfAssurance());
        }
    }

    /**
     * Generates one attribute statement for the response.
     *
     * @param attributeMap the personal attribute map
     * @return the attribute statement for the response
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    private AttributeStatement generateResponseAttributeStatement(@Nonnull ImmutableAttributeMap attributeMap)
            throws EIDASSAMLEngineException {
        LOG.trace("Generate attribute statement");

        AttributeStatement attrStatement =
                (AttributeStatement) BuilderFactoryUtil.buildXmlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
        List<Attribute> list = attrStatement.getAttributes();

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : attributeMap
                .getAttributeMap()
                .entrySet()) {
            // Verification that only one value is permitted, simple or
            // complex, not both.

            ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>> value = entry.getValue();
            addToAttributeList(list, entry.getKey(), value);
        }
        return attrStatement;
    }

    private void addToAttributeList(@Nonnull List<Attribute> list,
                                    @Nonnull AttributeDefinition<?> attributeDefinition,
                                    @Nonnull
                                            ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>> values)
            throws EIDASSAMLEngineException {
        // TODO take transliteration into account

        AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (final eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue : values) {
            try {
                String marshalledValue = attributeValueMarshaller.marshal(
                        (eu.eidas.auth.commons.attribute.AttributeValue) attributeValue);
                builder.add(marshalledValue);
            } catch (AttributeValueMarshallingException e) {
                LOG.error("Illegal attribute value: " + e, e);
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                                                   EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(), e);
            }
        }

        list.add(generateAttrSimple(attributeDefinition, builder.build()));
    }

    @Nonnull
    @Override
    public IAuthenticationResponse unmarshallResponse(@Nonnull Response response,
                                                      boolean verifyBearerIpAddress,
                                                      @Nullable String userIpAddress,
                                                      long beforeSkewTimeInMillis,
                                                      long afterSkewTimeInMillis,
                                                      @Nonnull DateTime now,
                                                      @Nullable String audienceRestriction)
            throws EIDASSAMLEngineException {

        AuthenticationResponse.Builder builder = createResponseBuilder(response);

        IResponseStatus responseStatus = ResponseUtil.extractResponseStatus(response);

        builder.responseStatus(responseStatus);

        LOG.trace("validateEidasResponse");
        Assertion assertion =
                ResponseUtil.extractVerifiedAssertion(response, verifyBearerIpAddress, userIpAddress, beforeSkewTimeInMillis,
                                                      afterSkewTimeInMillis, now, audienceRestriction);

        if (null != assertion) {
            LOG.trace("Set notOnOrAfter.");
            builder.notOnOrAfter(assertion.getConditions().getNotOnOrAfter());

            LOG.trace("Set notBefore.");
            builder.notBefore(assertion.getConditions().getNotBefore());

            builder.audienceRestriction((assertion.getConditions().getAudienceRestrictions().get(0)).getAudiences()
                                                .get(0)
                                                .getAudienceURI());
            if (!assertion.getAuthnStatements().isEmpty()
                    && assertion.getAuthnStatements().get(0).getAuthnContext() != null &&
                    assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef() != null) {
                builder.levelOfAssurance(assertion.getAuthnStatements()
                                                 .get(0)
                                                 .getAuthnContext()
                                                 .getAuthnContextClassRef()
                                                 .getAuthnContextClassRef());
            }
        }

        // Case no error.
        if (null != assertion && !ResponseUtil.isFailure(responseStatus)) {
            LOG.trace("Status Success. Set PersonalAttributeList.");
            builder.attributes(convertToAttributeMap(assertion));
        } else {
            LOG.trace("Status Fail.");
        }
        LOG.trace("Return result.");
        return builder.build();
    }

    private AuthenticationResponse.Builder createResponseBuilder(Response samlResponse) {
        LOG.trace("Create EidasAuthResponse.");
        AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();

        responseBuilder.country(CertificateUtil.getCountry(samlResponse.getSignature().getKeyInfo()));

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
     * Converts an assertion to an attribute map.
     *
     * @param assertion the assertion
     * @return the attribute map
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    @Nonnull
    private ImmutableAttributeMap convertToAttributeMap(@Nonnull Assertion assertion) throws EIDASSAMLEngineException {
        LOG.trace("Generate personal attribute list from XMLObject.");

        AttributeStatement attributeStatement = ResponseUtil.findAttributeStatement(assertion);

        List<Attribute> attributes = attributeStatement.getAttributes();

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        // Process the attributes.
        for (final Attribute attribute : attributes) {
            String attributeName = attribute.getName();

            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNotNull(attributeName);

            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();

            ImmutableSet.Builder<eu.eidas.auth.commons.attribute.AttributeValue<?>> setBuilder = new ImmutableSet.Builder<>();

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

            mapBuilder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) setBuilder.build());
        }

        return mapBuilder.build();
    }

    @Nonnull
    private AttributeDefinition<?> getAttributeDefinitionNotNull(@Nonnull String name) throws EIDASSAMLEngineException {
        AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNullable(name);
        if (null == attributeDefinition) {
            LOG.error("BUSINESS EXCEPTION : Attribute name: {} is not known.", name);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               "Attribute name: " + name + " is not known.");
        }
        return attributeDefinition;
    }

    private Map<String, String> computeComplexValue(XSAnyImpl complexValue) {
        Map<String, String> multiValues = new HashMap<String, String>();
        for (final XMLObject xmlObject : complexValue.getUnknownXMLObjects()) {
            XSAnyImpl simple = (XSAnyImpl) xmlObject;

            multiValues.put(simple.getElementQName().getLocalPart(), simple.getTextContent());
        }
        return multiValues;
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
}
