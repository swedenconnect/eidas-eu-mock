package eu.eidas.auth.engine.core.eidas;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.SamlBindingUri;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.xml.opensaml.*;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @deprecated ()
 */
@Deprecated
@NotThreadSafe
public final class EidasExtensionProcessor implements ExtensionProcessorI {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasExtensionProcessor.class.getName());

    private static final String LOA_START = "http://eidas.europa.eu/loa/";

    private static final String EIDAS_REQUEST_VALIDATOR_SUITE_ID = "eidasRequestValidatorSuiteId";

    public static final QName EIDAS_REQUESTED_ATTRIBUTE_VALUE_TYPE =
            new QName(SAMLCore.EIDAS10_SAML_NS.getValue(), "AttributeValue", SAMLCore.EIDAS10_PREFIX.getValue());

    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");

    private static final String EIDAS_RESPONSE_VALIDATOR_SUITE_ID = "eidasResponseValidatorSuiteId";

    /**
     * The default instance only implements the eIDAS specification without any additional attribute.
     */
    public static final EidasExtensionProcessor INSTANCE = new EidasExtensionProcessor(null, null);

    static {
        INSTANCE.configureExtension();
    }

    @Nonnull
    private final AttributeRegistry eidasAttributeRegistry;

    @Nonnull
    private final AttributeRegistry additionalAttributeRegistry;

    @Nullable
    private final MetadataEncryptionHelper metadataEncryptionHelper;

    @Nullable
    private final MetadataSignatureHelper metadataSignatureHelper;

    @Nullable
    private final MetadataFetcherI metadataFetcher;

    @Nullable
    private final MetadataSignerI metadataSigner;

    public static final AttributeRegistry.AttributeDefinitionFilter MANDATORY_LEGAL_FILTER =
            new AttributeRegistry.AttributeDefinitionFilter() {
                @Override
                public boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition) {
                    return attributeDefinition.isRequired()
                            && attributeDefinition.getPersonType() == PersonType.LEGAL_PERSON;
                }
            };

    public static final AttributeRegistry.AttributeDefinitionFilter MANDATORY_NATURAL_FILTER =
            new AttributeRegistry.AttributeDefinitionFilter() {
                @Override
                public boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition) {
                    return attributeDefinition.isRequired()
                            && attributeDefinition.getPersonType() == PersonType.NATURAL_PERSON;
                }
            };

    public EidasExtensionProcessor(@Nonnull String eidasAttributesFileNameVal,
                                   @Nonnull String additionalAttributesFileNameVal,
                                   @Nullable String defaultPath,
                                   @Nullable MetadataFetcherI metadataFetcherVal,
                                   @Nullable MetadataSignerI metadataSignerVal) {
        Preconditions.checkNotNull(eidasAttributesFileNameVal, "eidasAttributesFileName");
        Preconditions.checkNotNull(additionalAttributesFileNameVal, "additionalAttributesFileName");
        eidasAttributeRegistry = AttributeRegistries.fromFile(eidasAttributesFileNameVal, defaultPath);
        additionalAttributeRegistry = AttributeRegistries.fromFile(additionalAttributesFileNameVal, defaultPath);
        metadataFetcher = metadataFetcherVal;
        metadataSigner = metadataSignerVal;
        if (null == metadataFetcher || null == metadataSigner) {
            metadataEncryptionHelper = null;
            metadataSignatureHelper = null;
        } else {
            metadataEncryptionHelper = new MetadataEncryptionHelper(metadataFetcher, metadataSigner);
            metadataSignatureHelper = new MetadataSignatureHelper(metadataFetcher, metadataSigner);
        }
    }

    public EidasExtensionProcessor(@Nonnull AttributeRegistry eidasAttributeRegistryVal,
                                   @Nonnull AttributeRegistry additionalAttributeRegistryVal,
                                   @Nullable MetadataFetcherI metadataFetcherVal,
                                   @Nullable MetadataSignerI metadataSignerVal) {
        Preconditions.checkNotNull(eidasAttributeRegistryVal, "eidasAttributeRegistry");
        Preconditions.checkNotNull(additionalAttributeRegistryVal, "additionalAttributeRegistry");
        eidasAttributeRegistry = eidasAttributeRegistryVal;
        additionalAttributeRegistry = additionalAttributeRegistryVal;
        metadataFetcher = metadataFetcherVal;
        metadataSigner = metadataSignerVal;
        if (null == metadataFetcher || null == metadataSigner) {
            metadataEncryptionHelper = null;
            metadataSignatureHelper = null;
        } else {
            metadataEncryptionHelper = new MetadataEncryptionHelper(metadataFetcher, metadataSigner);
            metadataSignatureHelper = new MetadataSignatureHelper(metadataFetcher, metadataSigner);
        }
    }

    @SuppressWarnings("squid:S2637")
    public EidasExtensionProcessor(@Nonnull AttributeRegistry additionalAttributeRegistry,
                                   @Nullable MetadataFetcherI metadataFetcher,
                                   @Nullable MetadataSignerI metadataSigner) {
        this(EidasSpec.REGISTRY, additionalAttributeRegistry, metadataFetcher, metadataSigner);
    }

    @SuppressWarnings("squid:S2637")
    public EidasExtensionProcessor(@Nullable MetadataFetcherI metadataFetcher,
                                   @Nullable MetadataSignerI metadataSigner) {
        this(EidasSpec.REGISTRY, AttributeRegistries.empty(), metadataFetcher, metadataSigner);
    }

    @Override
    public String getRequestValidatorId() {
        return EIDAS_REQUEST_VALIDATOR_SUITE_ID;
    }

    @Override
    public String getResponseValidatorId() {
        return EIDAS_RESPONSE_VALIDATOR_SUITE_ID;
    }

    @Nonnull
    @Override
    public AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest request,
                                        @Nonnull String serviceIssuer,
                                        @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

        // Validate mandatory parameters
        IAuthenticationRequest validRequest = validateAuthenticationRequest(request, serviceIssuer);

        String id = SAMLEngineUtils.generateNCName();

        AuthnRequest samlRequest =
                BuilderFactoryUtil.generateAuthnRequest(id, SAMLVersion.VERSION_20, SAMLEngineUtils.getCurrentTime());

        // Set name spaces.
        registerRequestNamespace(samlRequest);

        // Add parameter Mandatory
        samlRequest.setForceAuthn(Boolean.TRUE);

        // Add parameter Mandatory
        samlRequest.setIsPassive(Boolean.FALSE);

        samlRequest.setAssertionConsumerServiceURL(validRequest.getAssertionConsumerServiceURL());

        samlRequest.setProviderName(validRequest.getProviderName());

        // Add protocol binding
        samlRequest.setProtocolBinding(getProtocolBinding(validRequest, coreProperties));

        // Add parameter optional
        // Destination is mandatory
        // The application must to know the destination
        if (StringUtils.isNotBlank(validRequest.getDestination())) {
            samlRequest.setDestination(validRequest.getDestination());
        }

        // Consent is optional. Set from SAMLEngine.xml - consent.
        samlRequest.setConsent(coreProperties.getConsentAuthnRequest());

        Issuer issuer = BuilderFactoryUtil.generateIssuer();

        if (validRequest.getIssuer() != null) {
            issuer.setValue(SAMLEngineUtils.getValidIssuerValue(validRequest.getIssuer()));
        } else {
            issuer.setValue(coreProperties.getRequester());
        }

        // Optional
        String formatEntity = coreProperties.getFormatEntity();
        if (StringUtils.isNotBlank(formatEntity)) {
            issuer.setFormat(formatEntity);
        }

        samlRequest.setIssuer(issuer);
        addRequestedAuthnContext(validRequest, samlRequest);

        // Generate format extensions.
        Extensions formatExtensions = generateExtensions(coreProperties, validRequest);
        // add the extensions to the SAMLAuthnRequest
        samlRequest.setExtensions(formatExtensions);
        addNameIDPolicy(samlRequest, validRequest.getNameIdFormat());

        return samlRequest;
    }

    private void addNameIDPolicy(AuthnRequest request, String selectedNameID) throws EIDASSAMLEngineException {
        if (StringUtils.isNotEmpty(selectedNameID)) {
            NameIDPolicy policy = (NameIDPolicy) BuilderFactoryUtil.buildXmlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
            policy.setFormat(selectedNameID);
            policy.setAllowCreate(true);
            request.setNameIDPolicy(policy);
        }
    }

    /**
     * Process all elements XMLObjects from the extensions.
     *
     * @param samlRequest the authentication request.
     * @return the EIDAS authentication request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    @Nonnull
    @Deprecated
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
                                                    @Nullable String originCountryCode)
            throws EIDASSAMLEngineException {
        LOG.debug("Process the extensions for EIDAS 1.0 messageFormat");
        Extensions extensions = samlRequest.getExtensions();
        RequestedAttributes requestedAttr =
                (RequestedAttributes) extensions.getUnknownXMLObjects(RequestedAttributes.DEF_ELEMENT_NAME).get(0);

        List<RequestedAttribute> reqAttrs = requestedAttr.getAttributes();

        ImmutableAttributeMap.Builder attributeMapBuilder = new ImmutableAttributeMap.Builder();
        for (RequestedAttribute attribute : reqAttrs) {
            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNullable(attribute.getName());
            if (null == attributeDefinition) {
                LOG.info(AbstractProtocolEngine.SAML_EXCHANGE,
                         "BUSINESS EXCEPTION : Attribute name: {} was not found. It will be removed from the request object",
                         attribute.getName());
                continue;
            }

            List<String> stringValues = new ArrayList<>();
            for (XMLObject xmlObject : attribute.getOrderedChildren()) {
                // Process simple attributes.
                // An AuthenticationRequest must contain simple values only.
                String value;
                if (xmlObject instanceof XSStringImpl) {
                    XSStringImpl xmlString = (XSStringImpl) xmlObject;
                    value = xmlString.getValue();
                } else {
                    XSAnyImpl xmlString = (XSAnyImpl) xmlObject;
                    value = xmlString.getTextContent();
                }
                stringValues.add(value);
            }
            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
            ImmutableSet.Builder<eu.eidas.auth.commons.attribute.AttributeValue<?>> setBuilder = ImmutableSet.builder();
            for (final String value : stringValues) {
                eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue;
                try {
                    attributeValue = attributeValueMarshaller.unmarshal(value, false);
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("Illegal attribute value: " + e, e);
                    throw new EIDASSAMLEngineException(
                            EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                            EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()), e);
                }
                setBuilder.add(attributeValue);
            }
            attributeMapBuilder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) setBuilder.build());
        }

        EidasAuthenticationRequest.Builder builder = new EidasAuthenticationRequest.Builder();
        builder.originCountryCode(originCountryCode);
        builder.assertionConsumerServiceURL(samlRequest.getAssertionConsumerServiceURL());
        builder.binding(SAMLEngineUtils.getBindingMethod(samlRequest.getProtocolBinding()));
        builder.citizenCountryCode(citizenCountryCode);
        builder.destination(samlRequest.getDestination());
        builder.id(samlRequest.getID());
        builder.issuer(samlRequest.getIssuer().getValue());
        builder.levelOfAssurance(extractLevelOfAssurance(samlRequest));
        builder.nameIdFormat(null == samlRequest.getNameIDPolicy() ? null : samlRequest.getNameIDPolicy().getFormat());
        builder.providerName(samlRequest.getProviderName());
        builder.requestedAttributes(attributeMapBuilder.build());
        // eIDAS only:
        builder.levelOfAssuranceComparison(LevelOfAssuranceComparison.MINIMUM.stringValue());
        builder.spType(getNullableSPTypeFromExtension(extensions));

        try {
            return builder.build();
        } catch (IllegalArgumentException e) {
            throw new EIDASSAMLEngineException(
                    EidasErrors.get(EidasErrorKey.ILLEGAL_ARGUMENTS_IN_BUILDER.errorCode()) + " - " + e.getMessage(),
                    e);
        }
    }

    @Override
    public void addRequestedAuthnContext(IAuthenticationRequest request, AuthnRequest authnRequestAux)
            throws EIDASSAMLEngineException {
        if (request == null || StringUtils.isEmpty(request.getLevelOfAssurance())) {
            return;
        }
        if (LevelOfAssurance.getLevel(request.getLevelOfAssurance()) == null) {
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode()),
                                               EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorMessage()));
        }
        RequestedAuthnContext authnContext =
                (RequestedAuthnContext) BuilderFactoryUtil.buildXmlObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        if (authnContext == null) {
            throw new EIDASSAMLEngineException("Unable to create SAML Object DEFAULT_ELEMENT_NAME");
        }
        authnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        AuthnContextClassRef authnContextClassRef =
                (AuthnContextClassRef) BuilderFactoryUtil.buildXmlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(request.getLevelOfAssurance());
        authnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        authnRequestAux.setRequestedAuthnContext(authnContext);
    }

    /**
     * Extracts the level of assurance in the RequestedAuthContext of the authRequest
     *
     * @param authnRequest
     * @return nullable level
     * @throws EIDASSAMLEngineException in case of invalid level of assurance value
     */
    @Nullable
    private String extractLevelOfAssurance(AuthnRequest authnRequest) throws EIDASSAMLEngineException {
        if (authnRequest.getRequestedAuthnContext() != null && !authnRequest.getRequestedAuthnContext()
                .getAuthnContextClassRefs()
                .isEmpty()) {
            RequestedAuthnContext rac = authnRequest.getRequestedAuthnContext();
            if (null == rac.getComparison()) {
                throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()),
                                                   EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()));
            }
            String comparison = rac.getComparison().toString();
            List<AuthnContextClassRef> authnContexts = rac.getAuthnContextClassRefs();
            for (AuthnContextClassRef contextRef : authnContexts) {
                LevelOfAssurance level = LevelOfAssurance.getLevel(contextRef.getAuthnContextClassRef());
                if (level != null && LevelOfAssuranceComparison.fromString(comparison) != null) {
                    return level.stringValue();
                } else if (!StringUtils.isEmpty(contextRef.getAuthnContextClassRef())) {
                    throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()),
                                                       EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()));

                }
            }
        }
        return null;
    }

    @Nullable
    private static String getNullableSPTypeFromExtension(final Extensions extensions) {
        List<XMLObject> optionalElements = extensions.getUnknownXMLObjects(SPType.DEF_ELEMENT_NAME);

        if (!optionalElements.isEmpty()) {
            SPType type = (SPType) extensions.getUnknownXMLObjects(SPType.DEF_ELEMENT_NAME).get(0);
            return type.getSPType();
        }
        return null;
    }

    /**
     * Generate extensions.
     *
     * @param request the request
     * @return the extensions
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Override
    public Extensions generateExtensions(SamlEngineCoreProperties samlCoreProperties, IAuthenticationRequest request)
            throws EIDASSAMLEngineException {
        LOG.trace("Generate EIDAS Extensions");

        Extensions extensions = BuilderFactoryUtil.generateExtension();

        addExtensionSPType((IEidasAuthenticationRequest) request, extensions);

        // Add information about requested attributes.
        LOG.trace("Generate RequestedAttributes.");
        RequestedAttributes reqAttributes =
                (RequestedAttributes) BuilderFactoryUtil.buildXmlObject(RequestedAttributes.DEF_ELEMENT_NAME);

        fillRequestedAttributes(request, reqAttributes);
        // Add requested attributes.
        extensions.getUnknownXMLObjects().add(reqAttributes);

        return extensions;
    }

    private static void addExtensionSPType(IEidasAuthenticationRequest request, Extensions extensions)
            throws EIDASSAMLEngineException {
        // if SpType is provided for the request
        if (StringUtils.isNotBlank(request.getSpType())) {
            SpType spType = SpType.fromString(request.getSpType());
            if (spType != null) {
                LOG.trace("Generate SPType provided by light request");
                final SPType spTypeObj = (SPType) BuilderFactoryUtil.buildXmlObject(SPType.DEF_ELEMENT_NAME);
                if (spTypeObj != null) {
                    spTypeObj.setSPType(spType.getValue());
                    extensions.getUnknownXMLObjects().add(spTypeObj);
                } else {
                    LOG.error("Unable to create SPType Object by SAML engine");
                }
            } else {
                LOG.error("Unable to create SPType Object by SAML engine - wrong value supplied in light request : " + request.getSpType());
                throw new EIDASSAMLEngineException("Unable to create SPType Object by SAML engine - wrong value supplied in light request : " + request.getSpType());
            }
        }
    }

    private void fillRequestedAttributes(IAuthenticationRequest request, RequestedAttributes reqAttributes)
            throws EIDASSAMLEngineException {
        LOG.trace("SAML Engine configuration properties load.");
        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : request
                .getRequestedAttributes()
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();

            attributeDefinition = checkRequestedAttribute(attributeDefinition);

            LOG.trace("Generate requested attribute: " + attributeDefinition);
            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (final eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue : entry.getValue()) {
                try {
                    String marshalledValue = attributeValueMarshaller.marshal(
                            (eu.eidas.auth.commons.attribute.AttributeValue) attributeValue);
                    builder.add(marshalledValue);
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("Illegal attribute value: " + e, e);
                    throw new EIDASSAMLEngineException(
                            EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                            EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()), e);
                }
            }

            RequestedAttribute requestedAttr = generateReqAuthnAttributeSimple(attributeDefinition, builder.build());

            // Add requested attribute.
            reqAttributes.getAttributes().add(requestedAttr);
        }
    }

    /**
     * In eIDAS, the "required" flag from the request is ignored and it is the registry which prevails for all attribute
     * definitions from the eIDAS specification.
     *
     * @param requestedAttribute the requested attribute definition
     */
    @Nullable
    public AttributeDefinition<?> checkRequestedAttribute(@Nonnull AttributeDefinition<?> requestedAttribute)
            throws EIDASSAMLEngineException {
        // Return the static definition from the registry and ignores the changes in the request if the attribute is from the spec
        URI requestedNameUri = requestedAttribute.getNameUri();
        AttributeDefinition<?> specAttribute = getMinimumDataSetAttributes().getByName(requestedNameUri);
        if (null != specAttribute) {
            if (!specAttribute.equals(requestedAttribute)) {
                LOG.trace("Eidas Attribute: {} does not comply with the eIDAS specification ({}).", requestedAttribute,
                          specAttribute);
                throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                                   EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                                   "Eidas Attribute: " + requestedAttribute
                                                           + " does not comply with the eIDAS specification("
                                                           + specAttribute + ").");
            }
            return specAttribute;
        }
        // else the attribute is not from the spec

//        AttributeDefinition additionalAttribute = getAdditionalAttributes().getByName(requestedNameUri);
//        if (null == additionalAttribute) {
//            LOG.trace("Eidas Attribute name: {} was not found.", requestedAttribute.getNameUri());
//            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
//                                               EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
//                                               "Attribute name: " + requestedAttribute.getNameUri()
//                                                       + " was not found.");
//        }

        return requestedAttribute;
    }

    private RequestedAttribute generateReqAuthnAttributeSimple(AttributeDefinition<?> attributeDefinition,
                                                               Set<String> values) throws EIDASSAMLEngineException {
        LOG.debug("Generate the requested attribute.");

        RequestedAttribute requested =
                (RequestedAttribute) BuilderFactoryUtil.buildXmlObject(RequestedAttribute.DEF_ELEMENT_NAME);
        String name = attributeDefinition.getNameUri().toASCIIString();
        requested.setName(name);
        requested.setNameFormat(RequestedAttribute.URI_REFERENCE);

        requested.setFriendlyName(attributeDefinition.getFriendlyName());

        requested.setIsRequired(String.valueOf(attributeDefinition.isRequired()));

        List<XMLObject> attributeValues = requested.getAttributeValues();

        if (!values.isEmpty()) {
            for (final String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    XMLObject attributeValueForRequest = createAttributeValueForRequest(attributeDefinition, value);
                    attributeValues.add(attributeValueForRequest);
                }
            }
        }
        return requested;
    }

    @Override
    public SAMLExtensionFormat getFormat() {
        return SAMLExtensionFormat.EIDAS10;
    }

    @Override
    public void configureExtension() {
        EidasExtensionConfiguration.configureExtension(this);
    }

    @Override
    public AttributeRegistry getMinimumDataSetAttributes() {
        return eidasAttributeRegistry;
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

    public ImmutableSortedSet<AttributeDefinition<?>> getFilteredAttributes(
            @Nonnull AttributeRegistry.AttributeDefinitionFilter filter) {

        AttributeRegistry minimumDataSetAttributes = getMinimumDataSetAttributes();
        AttributeRegistry additionalAttributes = getAdditionalAttributes();
        ImmutableSortedSet.Builder<AttributeDefinition<?>> builder =
                new ImmutableSortedSet.Builder<>(Ordering.<AttributeDefinition<?>>natural());
        for (AttributeDefinition<?> attributeDefinition : minimumDataSetAttributes.getAttributes()) {
            if (filter.accept(attributeDefinition)) {
                builder.add(attributeDefinition);
            }
        }
        for (AttributeDefinition<?> attributeDefinition : additionalAttributes.getAttributes()) {
            if (filter.accept(attributeDefinition)) {
                builder.add(attributeDefinition);
            }
        }
        return builder.build();

    }

    @Override
    public boolean isValidRequest(AuthnRequest samlRequest) {
        // TODO this method is not a validation at all!
        String spType = getNullableSPTypeFromExtension(samlRequest.getExtensions());
        if (StringUtils.isNotBlank(spType)) {
            return true;
        }
        if (samlRequest.getRequestedAuthnContext() != null && !samlRequest.getRequestedAuthnContext()
                .getAuthnContextClassRefs()
                .isEmpty()) {
            for (AuthnContextClassRef accr : samlRequest.getRequestedAuthnContext().getAuthnContextClassRefs()) {
                if (accr.getAuthnContextClassRef() != null && accr.getAuthnContextClassRef()
                        .toLowerCase(Locale.ENGLISH)
                        .startsWith(LOA_START)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validate parameters from authentication request.
     *
     * @param request the request.
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Nonnull
    @Override
    public IAuthenticationRequest validateAuthenticationRequest(@Nonnull IAuthenticationRequest request,
                                                                @Nonnull String serviceIssuer)
            throws EIDASSAMLEngineException {
        LOG.trace("Validate parameters from authentication request.");

        if (!(request instanceof IEidasAuthenticationRequest)) {
            final String newErrorDetail =
                    "ProtocolEngine: Request does not implement IEidasAuthenticationRequest: " + request;
            LOG.error(SAML_EXCHANGE, newErrorDetail);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage()),
                                               newErrorDetail);
        }

        // the name of the original service provider requesting the
        // authentication.
        if (StringUtils.isBlank(request.getProviderName())) {
            final String newErrorDetail = "ProtocolEngine: Service Provider is mandatory.";
            LOG.error(SAML_EXCHANGE, newErrorDetail);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage()),
                                               newErrorDetail);
        }

        Set<String> supportedAttributes = getSupportedAttributes(serviceIssuer);
        IAuthenticationRequest filteredRequest;
        if (CollectionUtils.isEmpty(supportedAttributes)) {
            filteredRequest = request;
        } else {
            filteredRequest = filterSupportedAttributeNames(request, supportedAttributes, serviceIssuer);
        }

        // object that contain all attributes requesting.
        if (filteredRequest.getRequestedAttributes().isEmpty()) {
            String newErrorDetail =
                    "No requested attribute (request issuer: " + request.getIssuer() + " - serviceIssuer: "
                            + serviceIssuer + ")";
            LOG.error(SAML_EXCHANGE, newErrorDetail);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL.errorCode()),
                                               EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL.errorMessage()),
                                               newErrorDetail);
        }

        return filteredRequest;
    }

    /**
     * Copies the attributes contained in an {@link  IAuthenticationRequest} that fullName are supported attribute
     * names
     *
     * @param request the instance which contains the attributes
     * @param supportedAttributeNames the supported attribute names
     * @return a builder for an {@link eu.eidas.auth.commons.attribute.ImmutableAttributeMap}
     */
    private IAuthenticationRequest filterSupportedAttributeNames(IAuthenticationRequest request,
                                                                 Set<String> supportedAttributeNames,
                                                                 String serviceMetadataURL)
            throws EIDASSAMLEngineException {
        ImmutableAttributeMap requestedAttributes = request.getRequestedAttributes();
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();
        boolean modified = false;
        for (Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : requestedAttributes
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();
            String fullName = attributeDefinition.getNameUri().toASCIIString();
            if (supportedAttributeNames.contains(fullName)) {
                builder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) entry.getValue());
            } else if (attributeDefinition.isRequired()) {
                // TODO use a new error code: the Metadata of the partner does not understand a requested mandatory attribute:
                // Failfast, refuse this request as it cannot be met
                String message =
                        "The Metadata of the Service does not contain the requested mandatory attribute \"" + fullName
                                + "\" (request issuer: " + request.getIssuer() + " - Service metadata URL: "
                                + serviceMetadataURL + ")";
                LOG.error(message);
                throw new EIDASSAMLEngineException(
                        EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                        EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()), message);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The Metadata of the Service does not contain the requested optional attribute \""
                                      + fullName + "\" (request issuer: " + request.getIssuer()
                                      + " - ProxyService metadata URL: " + serviceMetadataURL
                                      + "): it will be ignored");
                }
                modified = true;
            }
        }
        if (!modified) {
            return request;
        }
        return EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) request)
                .requestedAttributes(builder.build())
                .build();
    }

    @Override
    public void checkRequestSanity(IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (StringUtils.isBlank(request.getIssuer())) {
            final String newErrorDetail = "Request Issuer must not be blank.";
            LOG.error(SAML_EXCHANGE, newErrorDetail);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage()),
                                               newErrorDetail);
        }

        if (StringUtils.isBlank(request.getId())) {
            final String newErrorDetail = "Request ID must not be blank.";
            LOG.error(SAML_EXCHANGE, newErrorDetail);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage()),
                                               newErrorDetail);
        }
    }

    @Override
    @Nonnull
    public Attribute generateAttrSimple(@Nonnull AttributeDefinition<?> attributeDefinition,
                                        @Nonnull Collection<String> values) throws EIDASSAMLEngineException {
        LOG.trace("Generate attribute simple: {}", attributeDefinition.toString());
        Attribute attribute = (Attribute) BuilderFactoryUtil.buildXmlObject(Attribute.DEFAULT_ELEMENT_NAME);

        if (null != attribute) {
            String name = attributeDefinition.getNameUri().toASCIIString();
            attribute.setName(name);
            attribute.setFriendlyName(attributeDefinition.getFriendlyName());
            attribute.setNameFormat(Attribute.URI_REFERENCE);

            if (null != values && !values.isEmpty()) {
                LOG.trace("Add attribute values.");
                List<XMLObject> attributeValues = attribute.getAttributeValues();
                for (String value : values) {
                    if (StringUtils.isNotBlank(value)) {
                        // Create the attribute statement
                        XMLObject attrValue = createAttributeValueForResponse(attributeDefinition, value);
                        attributeValues.add(attrValue);
                    }
                }
            }
            return attribute;
        } else {
            throw new EIDASSAMLEngineException("Unable to create SAML object DEFAULT_ELEMENT_NAME");
        }
    }

    @Override
    public Attribute generateAttrComplex(AttributeDefinition<?> attributeDefinition,
                                         String status,
                                         Map<String, String> values,
                                         boolean isHashing) throws UnsupportedOperationException {
        // used only in STORK
        throw new UnsupportedOperationException();
    }

    private XMLObject createAttributeValueForRequest(AttributeDefinition<?> attributeDefinition, String value) {
        return createAttributeValue(attributeDefinition, EIDAS_REQUESTED_ATTRIBUTE_VALUE_TYPE, value);
    }

    private XMLObject createAttributeValueForResponse(AttributeDefinition<?> attributeDefinition, String value) {
        return createAttributeValue(attributeDefinition, AttributeValue.DEFAULT_ELEMENT_NAME, value);
    }

    private XMLObject createAttributeValue(AttributeDefinition<?> attributeDefinition,
                                           QName attributeValueType,
                                           String value) {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        QName xmlType = attributeDefinition.getXmlType();

        if (XSString.TYPE_NAME.equals(xmlType)) {
            XMLObjectBuilder<XSString> xmlObjectBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
            XSString xsString = xmlObjectBuilder.buildObject(attributeValueType, xmlType);
            xsString.setValue(value);
            return xsString;
        }

        XMLObjectBuilder<XSAny> xmlObjectBuilder = builderFactory.getBuilder(XSAny.TYPE_NAME);
        XSAny anyValue = xmlObjectBuilder.buildObject(attributeValueType, xmlType);

        anyValue.getNamespaceManager().registerNamespace(new Namespace(xmlType.getNamespaceURI(), xmlType.getPrefix()));

        // Create the attribute statement
        anyValue.setTextContent(value);

        // eIDAS transliteration attribute:
        if (attributeDefinition.isTransliterationMandatory() && ProtocolEngine.needsTransliteration(value)) {
            anyValue.getUnknownAttributes()
                    .put(new QName(xmlType.getNamespaceURI(), "LatinScript", xmlType.getPrefix()), "false");
        }
        return anyValue;
    }

    @Override
    @Nullable
    public AttributeDefinition<?> getAttributeDefinitionNullable(@Nonnull String name) {
        if (StringUtils.isBlank(name)) {
            LOG.info(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : {}",
                     ProtocolEngine.ATTRIBUTE_EMPTY_LITERAL);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.INTERNAL_ERROR.errorCode()),
                                                  ProtocolEngine.ATTRIBUTE_EMPTY_LITERAL);
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

    @Nullable
    @Override
    public String getProtocolBinding(@Nonnull IAuthenticationRequest request,
                                     @Nonnull SamlEngineCoreProperties defaultValues) {
        return null;
    }

    /**
     * Checks whether the attribute list contains at least one of the mandatory eIDAS attribute set (either for a
     * natural [person or for a legal person)
     *
     * @param immutableAttributeMap
     */
    @Override
    public boolean checkMandatoryAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap) {
        boolean requestedLegalSet = false;
        boolean requestedNaturalSet = false;
        if (null == immutableAttributeMap || immutableAttributeMap.isEmpty()) {
            return false;
        }

        Set<AttributeDefinition<?>> mandatoryLegalAttributes =
                new HashSet<>(getFilteredAttributes(EidasExtensionProcessor.MANDATORY_LEGAL_FILTER));

        Set<AttributeDefinition<?>> mandatoryNaturalAttributes =
                new HashSet<>(getFilteredAttributes(EidasExtensionProcessor.MANDATORY_NATURAL_FILTER));

        for (AttributeDefinition<?> attributeDefinition : immutableAttributeMap.getDefinitions()) {
            if (null == attributeDefinition) {
                continue;
            }
            if (attributeDefinition.getPersonType() == PersonType.LEGAL_PERSON) {
                mandatoryLegalAttributes.remove(attributeDefinition);
                requestedLegalSet = true;
            }
            if (attributeDefinition.getPersonType() == PersonType.NATURAL_PERSON) {
                mandatoryNaturalAttributes.remove(attributeDefinition);
                requestedNaturalSet = true;
            }
        }
        if (requestedLegalSet) {
            LOG.info("Mandatory legalPerson attributes not requested : " + mandatoryLegalAttributes.toString());
        }
        if (requestedNaturalSet) {
            LOG.info("Mandatory naturalPerson attributes not requested : " + mandatoryNaturalAttributes.toString());
        }
        // either all the legal or all the natural mandatory attributes MUST be requested/returned:
        return (!requestedLegalSet || mandatoryLegalAttributes.isEmpty()) && (!requestedNaturalSet
                || mandatoryNaturalAttributes.isEmpty());

    }

    @Override
    @Nonnull
    public IAuthenticationRequest updateRequestWithConsent(@Nonnull IAuthenticationRequest authnRequest,
                                                           @Nonnull ImmutableAttributeMap consentedAttributes) {
        return EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest)
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
                .registerNamespace(
                        new Namespace(SAMLCore.EIDAS10_SAML_NS.getValue(), SAMLCore.EIDAS10_SAML_PREFIX.getValue()));
    }

    @Nullable
    @Override
    public X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException {
        if (null == metadataEncryptionHelper) {
            return null;
        }
        return metadataEncryptionHelper.getEncryptionCertificate(requestIssuer);
    }

    @Nullable
    @Override
    public X509Certificate getRequestSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
        if (null == metadataSignatureHelper) {
            return null;
        }
        return metadataSignatureHelper.getRequestSignatureCertificate(issuer);
    }

    @Nullable
    @Override
    public X509Certificate getResponseSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
        if (null == metadataSignatureHelper) {
            return null;
        }
        return metadataSignatureHelper.getResponseSignatureCertificate(issuer);
    }

    @Nullable
    public MetadataEncryptionHelper getMetadataEncryptionHelper() {
        return metadataEncryptionHelper;
    }

    @Nullable
    public MetadataSignatureHelper getMetadataSignatureHelper() {
        return metadataSignatureHelper;
    }

    @Nullable
    @Override
    @SuppressWarnings("squid:S2583")
    public String getServiceUrl(@Nonnull String issuer, @Nonnull SamlBindingUri bindingUri)
            throws EIDASSAMLEngineException {
        if (null == metadataFetcher || null == metadataSigner) {
            return null;
        }
        EntityDescriptor entityDescriptor = metadataFetcher.getEntityDescriptor(issuer, metadataSigner);
        if (null == entityDescriptor) {
            return null;
        }
        IDPSSODescriptor idpSsoDescriptor = MetadataUtil.getIDPSSODescriptor(entityDescriptor);
        return MetadataUtil.getSingleSignOnUrl(idpSsoDescriptor, bindingUri);
    }

    /**
     * Returns the full name URIs of all the attributes the given issuer supports (optional operation).
     * <p>
     * Implementations of this method can for instance consult the metadata of the given issuer to return the supported
     * attributes.
     * <p>
     * This method can return an empty {@link Set} when the supported attributes are unknown.
     *
     * @param issuer the issuer URI
     * @return a {@link Set} of attribute name URIs as {@link String}s.
     * @throws EIDASSAMLEngineException
     * @since 1.1
     */
    @Nonnull
    @SuppressWarnings("squid:S2583")
    private Set<String> getSupportedAttributes(@Nonnull String issuer) throws EIDASSAMLEngineException {
        if (null == metadataFetcher || null == metadataSigner || null == issuer) {
            return Collections.emptySet();
        }
        EntityDescriptor entityDescriptor = metadataFetcher.getEntityDescriptor(issuer, metadataSigner);
        if (null == entityDescriptor) {
            return Collections.emptySet();
        }
        IDPSSODescriptor idpSsoDescriptor = MetadataUtil.getIDPSSODescriptor(entityDescriptor);
        if (null == idpSsoDescriptor || null == idpSsoDescriptor.getAttributes() || idpSsoDescriptor.getAttributes()
                .isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> supportedAttrNames = new HashSet<String>();
        for (Attribute a : idpSsoDescriptor.getAttributes()) {
            supportedAttrNames.add(a.getName());
        }
        return supportedAttrNames;
    }

    @Override
    @SuppressWarnings("squid:S2583")
    public boolean isAcceptableHttpRequest(@Nonnull IAuthenticationRequest authnRequest, @Nullable String httpMethod) {
        try {
            if (metadataFetcher == null || null == metadataSigner) {
                return true;
            }

            String issuer = authnRequest.getIssuer();
            EntityDescriptor entityDescriptor = metadataFetcher.getEntityDescriptor(issuer, metadataSigner);
            if (null == entityDescriptor) {
                return true;
            }

            SPSSODescriptor spDesc = MetadataUtil.getSPSSODescriptor(entityDescriptor);

            String metadataAssertionUrl = MetadataUtil.getAssertionConsumerUrl(spDesc);
            if (StringUtils.isEmpty(metadataAssertionUrl) || (authnRequest.getAssertionConsumerServiceURL() != null
                    && !authnRequest.getAssertionConsumerServiceURL().equals(metadataAssertionUrl))) {
                throw new InternalErrorEIDASException(
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
            }

            if (httpMethod != null && StringUtils.isNotBlank(httpMethod)) {
                boolean isBindingValid = false;
                for (AssertionConsumerService asc : spDesc.getAssertionConsumerServices()) {
                    if (httpMethod.equalsIgnoreCase(SAMLEngineUtils.getBindingMethod(asc.getBinding()))) {
                        isBindingValid = true;
                        break;
                    }
                }
                if (!isBindingValid) {
                    LOG.info("The issuer {} does not support {}", issuer, httpMethod);
                    throw new InternalErrorEIDASException(
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()),
                            new InternalErrorEIDASException(
                                    EidasErrors.get(EidasErrorKey.INVALID_PROTOCOL_BINDING.errorCode()),
                                    EidasErrors.get(EidasErrorKey.INVALID_PROTOCOL_BINDING.errorMessage())));
                }
            }

            //exactly one of requestSpType, metadataSpType should be non empty
            String metadataSpType = MetadataUtil.getSPTypeFromMetadata(entityDescriptor);
            if (authnRequest.getSpType() != null) {
                SpType requestSpType = SpType.fromString(authnRequest.getSpType());
                // both Metadata and Request supplies SP type - not allowed
                if (requestSpType != null && StringUtils.isNotBlank(metadataSpType)) {
                    LOG.error("SPType both in Connector Metadata and Request");
                    throw new EIDASServiceException(
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INCONSISTENT_SPTYPE.errorCode()),
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INCONSISTENT_SPTYPE.errorMessage()), "");
                }
            } else {
                // neither of them has SPType available
                if (!StringUtils.isNotBlank(metadataSpType)) {
                    LOG.error("SPType not provided");
                    throw new EIDASServiceException(
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_MISSING_SPTYPE.errorCode()),
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_MISSING_SPTYPE.errorMessage()), "");
                }
            }

        } catch (EIDASSAMLEngineException e) {
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                                                  EidasErrors.get(
                                                          EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()), e);
        }
        return true;
    }

    @Nonnull
    @Override
    @SuppressWarnings("squid:S2583")
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
    @Override
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
    @Override
    public void registerResponseNamespace(@Nonnull XMLObject xmlObject) {
        LOG.trace("Set namespaces.");
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlObject.getNamespaceManager().registerNamespace(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));

        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLCore.EIDAS10_RESPONSESAML_NS.getValue(),
                                                 SAMLCore.EIDAS10_SAML_PREFIX.getValue()));
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
    @SuppressWarnings("squid:S2583")
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

            ImmutableSet.Builder<eu.eidas.auth.commons.attribute.AttributeValue<?>> setBuilder =
                    new ImmutableSet.Builder<>();

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

    private String computeSimpleValue(XSAnyImpl xsAny) throws EIDASSAMLEngineException {
        if (null != xsAny) {
            List<XMLObject> unknownXMLObjects = xsAny.getUnknownXMLObjects();
            if (null != unknownXMLObjects && !unknownXMLObjects.isEmpty()) {
                try {
                    return DocumentBuilderFactoryUtil.toString(unknownXMLObjects.get(0).getDOM());
                } catch (TransformerException e) {
                    LOG.error(SAML_EXCHANGE, "ERROR :  transformer exception: " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                                       EidasErrorKey.INTERNAL_ERROR.errorCode(), e);
                }
            }
            return xsAny.getTextContent();
        }
        return null;
    }
}
