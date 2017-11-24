/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.engine.core.eidas;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
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
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.auth.engine.core.eidas.spec.LegalPersonSpec;
import eu.eidas.auth.engine.core.eidas.spec.RepresentativeLegalPersonSpec;
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
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Implements the eIDAS protocol.
 *
 * @since 1.1
 */
public class EidasProtocolProcessor implements ProtocolProcessorI {

    public static final QName EIDAS_REQUESTED_ATTRIBUTE_VALUE_TYPE =
            new QName(SAMLCore.EIDAS10_SAML_NS.getValue(), "AttributeValue", SAMLCore.EIDAS10_PREFIX.getValue());

    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");

    /**
     * The default instance only implements the eIDAS specification without any additional attribute.
     */
    public static final EidasProtocolProcessor INSTANCE = new EidasProtocolProcessor(null, null);

    /**
     * The LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasProtocolProcessor.class);

    private static final String EIDAS_REQUEST_VALIDATOR_SUITE_ID = "eidasRequestValidatorSuiteId";

    private static final String EIDAS_RESPONSE_VALIDATOR_SUITE_ID = "eidasResponseValidatorSuiteId";

    static {
        INSTANCE.configure();
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

    @SuppressWarnings("squid:S2637")
    public EidasProtocolProcessor(@Nullable MetadataFetcherI metadataFetcher,
                                  @Nullable MetadataSignerI metadataSigner) {
        this(EidasSpec.REGISTRY, AttributeRegistries.empty(), metadataFetcher, metadataSigner);
    }

    @SuppressWarnings("squid:S2637")
    public EidasProtocolProcessor(@Nonnull AttributeRegistry additionalAttributeRegistry,
                                  @Nullable MetadataFetcherI metadataFetcher,
                                  @Nullable MetadataSignerI metadataSigner) {
        this(EidasSpec.REGISTRY, additionalAttributeRegistry, metadataFetcher, metadataSigner);
    }

    public EidasProtocolProcessor(@Nonnull String eidasAttributesFileNameVal,
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

    public EidasProtocolProcessor(@Nullable AttributeRegistry eidasAttributeRegistryVal,
                                  @Nullable AttributeRegistry additionalAttributeRegistryVal,
                                  @Nullable MetadataFetcherI metadataFetcherVal,
                                  @Nullable MetadataSignerI metadataSignerVal) {
        if (null == eidasAttributeRegistryVal) {
            eidasAttributeRegistry = EidasSpec.REGISTRY;
        } else {
            eidasAttributeRegistry = eidasAttributeRegistryVal;
        }
        if (null == additionalAttributeRegistryVal) {
            additionalAttributeRegistry = AttributeRegistries.empty();
        } else {
            additionalAttributeRegistry = additionalAttributeRegistryVal;
        }
        this.metadataFetcher = metadataFetcherVal;
        this.metadataSigner = metadataSignerVal;
        if (null == metadataFetcher || null == metadataSigner) {
            metadataEncryptionHelper = null;
            metadataSignatureHelper = null;
        } else {
            metadataEncryptionHelper = new MetadataEncryptionHelper(metadataFetcher, metadataSigner);
            metadataSignatureHelper = new MetadataSignatureHelper(metadataFetcher, metadataSigner);
        }
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

    @Nullable
    protected static String getNullableSPTypeFromExtension(final Extensions extensions) {
        List<XMLObject> optionalElements = extensions.getUnknownXMLObjects(SPType.DEF_ELEMENT_NAME);

        if (!optionalElements.isEmpty()) {
            SPType type = (SPType) extensions.getUnknownXMLObjects(SPType.DEF_ELEMENT_NAME).get(0);
            return type.getSPType();
        }
        return null;
    }

    protected void addNameIDPolicy(AuthnRequest request, String selectedNameID) throws EIDASSAMLEngineException {
        if (StringUtils.isNotEmpty(selectedNameID)) {
            NameIDPolicy policy = (NameIDPolicy) BuilderFactoryUtil.buildXmlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
            policy.setFormat(selectedNameID);
            policy.setAllowCreate(true);
            request.setNameIDPolicy(policy);
        }
    }

    protected void addRequestedAuthnContext(IAuthenticationRequest request, AuthnRequest authnRequestAux)
            throws EIDASSAMLEngineException {

        validateLevelOfAssurance(request);

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

    private void validateRequestType(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (!(request instanceof IEidasAuthenticationRequest)) {
            String errorDetail = "ProtocolEngine: Request does not implement IEidasAuthenticationRequest: " + request;
            throwValidationException(errorDetail);
        }
    }

    private void validateProviderName(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {
        // the name of the original service provider requesting the authentication.
        if (StringUtils.isBlank(request.getProviderName())) {
            String errorDetail = "ProtocolEngine: Service Provider is mandatory.";
            throwValidationException(errorDetail);
        }
    }

    private void validateLevelOfAssurance(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (StringUtils.isEmpty(request.getLevelOfAssurance())) {
            return;
        }
        if (LevelOfAssurance.getLevel(request.getLevelOfAssurance()) == null) {
            String errorDetail =
                    "Invalid level of assurance: \"" + request.getLevelOfAssurance() + "\" in request: " + request;
            throwValidationException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA, errorDetail);
        }
    }

    @Nonnull
    private String getValidIssuerValue(@Nonnull IAuthenticationRequest request,
                                       @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {
        String issuer;
        if (null != request.getIssuer()) {
            issuer = SAMLEngineUtils.getValidIssuerValue(request.getIssuer());
        } else {
            issuer = coreProperties.getRequester();
        }
        return issuer;
    }

    @Nonnull
    protected <T> AttributeDefinition<T> validateRequestedDefinition(
            @Nonnull AttributeDefinition<T> requestedDefinition) {
        // If a requested definition is from the minimum data set of the specification, it cannot be modified.
        // On the contrary, if an attribute is an additional attribute, then its definition can be modified by the request
        AttributeDefinition<?> dataSetDefinition =
                getMinimumDataSetAttributes().getByName(requestedDefinition.getNameUri());
        if (null != dataSetDefinition) {
            // the attribute is part of the specification
            return (AttributeDefinition<T>) dataSetDefinition;
        }
        // the attribute is an additional attribute, therefore it does not have to comply with a static definition:
        return requestedDefinition;
    }

    @Nonnull
    protected <T> AttributeDefinition<T> validateRequestedAttribute(@Nonnull RequestedAttribute requestedAttribute,
                                                                    @Nonnull AttributeDefinition<T> staticDefinition) {
        // If a requested definition is from the minimum data set of the specification, it cannot be modified.
        // On the contrary, if an attribute is an additional attribute, then its definition can be modified by the request

        AttributeDefinition<?> dataSetDefinition =
                getMinimumDataSetAttributes().getByName(staticDefinition.getNameUri());
        if (null != dataSetDefinition) {
            // the attribute is part of the specification
            return (AttributeDefinition<T>) dataSetDefinition;
        }

        Boolean requestedIsRequired = requestedAttribute.isRequired();

        if (null == requestedIsRequired || requestedIsRequired.booleanValue() == staticDefinition.isRequired()) {
            return staticDefinition;
        }
        // the attribute is an additional attribute, therefore it does not have to comply with a static definition
        // Update the required flag according to the request instead of using the local registry:
        return AttributeDefinition.builder(staticDefinition).required(requestedIsRequired.booleanValue()).build();
    }

    private void validateId(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (StringUtils.isBlank(request.getId())) {
            String errorDetail = "Request ID must not be blank.";
            throwValidationException(errorDetail);
        }
    }

    private void validateIssuer(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (StringUtils.isBlank(request.getIssuer())) {
            String errorDetail = "Request Issuer must not be blank.";
            throwValidationException(errorDetail);
        }
    }

    private void throwValidationException(@Nonnull String errorDetail) throws EIDASSAMLEngineException {
        throwValidationException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, errorDetail);
    }

    private void throwValidationException(@Nonnull EidasErrorKey errorKey, @Nonnull String errorDetail)
            throws EIDASSAMLEngineException {
        LOG.error(SAML_EXCHANGE, errorDetail);
        throw new EIDASSAMLEngineException(EidasErrors.get(errorKey.errorCode()),
                                           EidasErrors.get(errorKey.errorMessage()), errorDetail);
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
                new HashSet<>(getFilteredAttributes(EidasProtocolProcessor.MANDATORY_LEGAL_FILTER));

        Set<AttributeDefinition<?>> mandatoryNaturalAttributes =
                new HashSet<>(getFilteredAttributes(EidasProtocolProcessor.MANDATORY_NATURAL_FILTER));

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

    /**
     * Checks whether the attribute list fulfills the reuqiremnts of representative scenario
     *
     * According to Specs 1.1 representative attributes MUST no be requested
     *
     * @param immutableAttributeMap
     */
    @Override
    public boolean checkRepresentativeAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap) {
        boolean checkResult = true;

        for (AttributeDefinition<?> attributeDefinition : immutableAttributeMap.getDefinitions()) {
            if (null != attributeDefinition &&
                    (attributeDefinition.getPersonType() == PersonType.REPV_LEGAL_PERSON ||
                            attributeDefinition.getPersonType() == PersonType.REPV_NATURAL_PERSON)) {
                checkResult = false;
            }
        }
        return checkResult;
    }

    @Override
    public void checkRequestSanity(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {

        validateIssuer(request);

        validateId(request);
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

    @Override
    public void configure() {
        EidasExtensionConfiguration.configureExtension(this);
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

    private XMLObject createAttributeValueForRequest(AttributeDefinition<?> attributeDefinition, String value) {
        return createAttributeValue(attributeDefinition, EIDAS_REQUESTED_ATTRIBUTE_VALUE_TYPE, value);
    }

    private XMLObject createAttributeValueForResponse(AttributeDefinition<?> attributeDefinition, String value) {
        return createAttributeValue(attributeDefinition, AttributeValue.DEFAULT_ELEMENT_NAME, value);
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
     * Extracts the level of assurance in the RequestedAuthContext of the authRequest
     *
     * @param authnRequest
     * @return nullable level
     * @throws EIDASSAMLEngineException in case of invalid level of assurance value
     */
    @Nullable
    protected String extractLevelOfAssurance(AuthnRequest authnRequest) throws EIDASSAMLEngineException {
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

    /* This is a temporary function to replace the correct LegalPerson attributes with the old erroneous ones, according to other party's metadata
    * it is implemented in the Engine, because this is the place where we have Metadata */
    //TODO remove check of erroneous attributes after transition period of EID-423
    private boolean requestAlignLegalAttributes(AttributeDefinition<?> attributeDefinition, Set<String> supportedAttributeNames, ImmutableAttributeMap.Builder builder,
                                                 Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry, String serviceMetadataURL) {
        boolean modified = false;
        if (attributeDefinition.equals(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS)
                && supportedAttributeNames.contains(LegalPersonSpec.Definitions.LEGAL_ADDRESS.getNameUri().toASCIIString())) {
            builder.put((AttributeDefinition) LegalPersonSpec.Definitions.LEGAL_ADDRESS, (ImmutableSet) entry.getValue());
            LOG.warn("Switching requested LegalPersonAddress to LegalAddress for " + serviceMetadataURL);
            modified = true;
        }
        if (attributeDefinition.equals(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER)
                && supportedAttributeNames.contains(LegalPersonSpec.Definitions.VAT_REGISTRATION.getNameUri().toASCIIString())) {
            builder.put((AttributeDefinition) LegalPersonSpec.Definitions.VAT_REGISTRATION, (ImmutableSet) entry.getValue());
            LOG.warn("Switching requested VATRegistrationNumber to VATRegistration for " + serviceMetadataURL);
            modified = true;
        }
        return modified;
    }

    /**
     * Copies the attributes contained in an {@link  IAuthenticationRequest} that fullName are supported attribute
     * names
     *
     * @param requestedAttributes the requested attributes
     * @param supportedAttributeNames the supported attribute names
     * @return a builder for an {@link ImmutableAttributeMap}
     */
    @Nonnull
    private ImmutableAttributeMap filterSupportedAttributeNames(@Nonnull ImmutableAttributeMap requestedAttributes,
                                                                @Nonnull Set<String> supportedAttributeNames,
                                                                @Nonnull String requestIssuer,
                                                                @Nonnull String serviceMetadataURL)
            throws EIDASSAMLEngineException {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();
        boolean modified = false;
        for (Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : requestedAttributes
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();
            String fullName = attributeDefinition.getNameUri().toASCIIString();
            if (supportedAttributeNames.contains(fullName)) {
                AttributeDefinition<?> validatedDefinition = validateRequestedDefinition(attributeDefinition);
                if (!validatedDefinition.equals(attributeDefinition)) {
                    modified = true;
                }
                builder.put((AttributeDefinition) validatedDefinition, (ImmutableSet) entry.getValue());
                //TODO remove the below check of erroneous attributes after transition period of EID-423
            } else if (  (attributeDefinition.equals(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS) && supportedAttributeNames.contains(LegalPersonSpec.Definitions.LEGAL_ADDRESS.getNameUri().toASCIIString())) ||
                            (attributeDefinition.equals(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER) && supportedAttributeNames.contains(LegalPersonSpec.Definitions.LEGAL_ADDRESS.getNameUri().toASCIIString()))) {
                modified |= requestAlignLegalAttributes(attributeDefinition, supportedAttributeNames, builder, entry, serviceMetadataURL);
            } else if (attributeDefinition.isRequired()) {
                // TODO use a new error code: the Metadata of the partner does not understand a requested mandatory attribute:
                // Failfast, refuse this request as it cannot be met
                String message =
                        "The Metadata of the Service does not contain the requested mandatory attribute \"" + fullName
                                + "\" (request issuer: " + requestIssuer + " - Service metadata URL: "
                                + serviceMetadataURL + ")";
                LOG.error(message);
                throw new EIDASSAMLEngineException(
                        EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                        EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()), message);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The Metadata of the Service does not contain the requested optional attribute \""
                                      + fullName + "\" (request issuer: " + requestIssuer
                                      + " - ProxyService metadata URL: " + serviceMetadataURL
                                      + "): it will be ignored");
                }
                modified = true;
            }
        }
        if (!modified) {
            return requestedAttributes;
        }
        return builder.build();
    }

    @Nonnull
    private Attribute generateAttrSimple(@Nonnull AttributeDefinition<?> attributeDefinition,
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

    /**
     * Generate extensions.
     *
     * @param request the request
     * @return the extensions
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    protected Extensions generateExtensions(IAuthenticationRequest request) throws EIDASSAMLEngineException {
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


    /**
     * Checks if the incoming EIDAS attribute request has Required flag set same as the Definition
      */
    @Nonnull
    private void checkRequiredAttributeCompiles(@Nullable AttributeDefinition attributeDef, @Nonnull RequestedAttribute requestedAttribute) throws EIDASSAMLEngineException {
        if (attributeDef != null && eidasAttributeRegistry.contains(attributeDef)) {
            if (attributeDef.isRequired() != requestedAttribute.isRequired()) {
                String name = requestedAttribute.getName();

                LOG.error("BUSINESS EXCEPTION : Attribute: {} 'required' flag does not matches definition.", name);
                throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                        EidasErrorKey.INTERNAL_ERROR.errorCode(),
                        "Attribute : " + name + " required flag does not matches definition.");
            }
        }
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
    public X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException {
        if (null == metadataEncryptionHelper) {
            return null;
        }
        return metadataEncryptionHelper.getEncryptionCertificate(requestIssuer);
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
    public SAMLExtensionFormat getFormat() {
        return SAMLExtensionFormat.EIDAS10;
    }

    @Nullable
    public MetadataEncryptionHelper getMetadataEncryptionHelper() {
        return metadataEncryptionHelper;
    }

    @Nullable
    public MetadataSignatureHelper getMetadataSignatureHelper() {
        return metadataSignatureHelper;
    }

    @Override
    public AttributeRegistry getMinimumDataSetAttributes() {
        return eidasAttributeRegistry;
    }

    @Nullable
    @Override
    public String getProtocolBinding(@Nonnull IAuthenticationRequest request,
                                     @Nonnull SamlEngineCoreProperties defaultValues) {
        return null;
    }

    @Nullable
    @Override
    public X509Certificate getRequestSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
        if (null == metadataSignatureHelper) {
            return null;
        }
        return metadataSignatureHelper.getRequestSignatureCertificate(issuer);
    }

    @Override
    public String getRequestValidatorId() {
        return EIDAS_REQUEST_VALIDATOR_SUITE_ID;
    }

    @Nullable
    @Override
    public X509Certificate getResponseSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
        if (null == metadataSignatureHelper) {
            return null;
        }
        return metadataSignatureHelper.getResponseSignatureCertificate(issuer);
    }

    @Override
    public String getResponseValidatorId() {
        return EIDAS_RESPONSE_VALIDATOR_SUITE_ID;
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
    protected Set<String> getSupportedAttributes(@Nonnull String issuer) throws EIDASSAMLEngineException {
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

            if (httpMethod!=null && StringUtils.isNotBlank(httpMethod)) {
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


    /**
     * TODO to be removed
     *
     * @deprecated since 1.4
     * Use {@link ProtocolProcessorI#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime)}
     *
     */
    @Nonnull
    @Override
    @SuppressWarnings("squid:S2583")
    @Deprecated
    public Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                          @Nonnull IAuthenticationResponse response,
                                          @Nonnull String ipAddress,
                                          @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

        //temporary solution for maintaining deprecated method
        final DateTime currentTime = new DateTime();

        return marshallErrorResponse(request,response,ipAddress,coreProperties,currentTime);
    }

    @Nonnull
    @Override
    @SuppressWarnings("squid:S2583")
    public Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                          @Nonnull IAuthenticationResponse response,
                                          @Nonnull String ipAddress,
                                          @Nonnull SamlEngineCoreProperties coreProperties,
                                          @Nonnull final DateTime currentTime)
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
                newResponse(status, request.getAssertionConsumerServiceURL(), request.getId(), coreProperties, currentTime);

        String responseIssuer = response.getIssuer();
        if (responseIssuer != null && !responseIssuer.isEmpty()) {
            responseFail.getIssuer().setValue(responseIssuer);
        }
        DateTime notOnOrAfter = currentTime;

        notOnOrAfter = notOnOrAfter.plusSeconds(coreProperties.getTimeNotOnOrAfter());

        Assertion assertion =
                AssertionUtil.generateResponseAssertion(true, ipAddress, request, responseFail.getIssuer(),
                                                        ImmutableAttributeMap.of(), notOnOrAfter,
                                                        coreProperties.getFormatEntity(), coreProperties.getResponder(),
                                                        getFormat(), coreProperties.isOneTimeUse(), currentTime);
        addResponseAuthnContextClassRef(response, assertion);
        responseFail.getAssertions().add(assertion);

        return responseFail;
    }

    @Nonnull
    @Override
    public IAuthenticationRequest createProtocolRequestToBeSent(@Nonnull IAuthenticationRequest requestToBeSent,
                                                                @Nonnull String serviceIssuer,
                                                                @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

        // Validate mandatory parameters
        return validateRequestAgainstMetadata(requestToBeSent, serviceIssuer, coreProperties);
    }

    /**
     * TODO to be removed
     *
     * @deprecated since 1.4
     * Use {@link ProtocolProcessorI#marshallRequest(IAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     *
     */
    @Nonnull
    @Override
    public AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest requestToBeSent,
                                        @Nonnull String serviceIssuer,
                                        @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

        //temporary solution for maintaining deprecated method
        final DateTime currentTime = new DateTime();

        return marshallRequest(requestToBeSent, serviceIssuer, coreProperties, currentTime);
    }

    @Nonnull
    @Override
    public AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest requestToBeSent,
                                        @Nonnull String serviceIssuer,
                                        @Nonnull SamlEngineCoreProperties coreProperties,
                                        @Nonnull final DateTime currentTime)
            throws EIDASSAMLEngineException {

        AuthnRequest samlRequest =
                BuilderFactoryUtil.generateAuthnRequest(requestToBeSent.getId(), SAMLVersion.VERSION_20,
                        currentTime);

        // Set name spaces.
        registerRequestNamespace(samlRequest);

        // Add parameter Mandatory
        samlRequest.setForceAuthn(Boolean.TRUE);

        // Add parameter Mandatory
        samlRequest.setIsPassive(Boolean.FALSE);

        samlRequest.setAssertionConsumerServiceURL(requestToBeSent.getAssertionConsumerServiceURL());

        samlRequest.setProviderName(requestToBeSent.getProviderName());

        // Add protocol binding
        samlRequest.setProtocolBinding(getProtocolBinding(requestToBeSent, coreProperties));

        // Add parameter optional
        // Destination is mandatory
        // The application must to know the destination
        if (StringUtils.isNotBlank(requestToBeSent.getDestination())) {
            samlRequest.setDestination(requestToBeSent.getDestination());
        }

        // Consent is optional. Set from SAMLEngine.xml - consent.
        samlRequest.setConsent(coreProperties.getConsentAuthnRequest());

        Issuer issuer = BuilderFactoryUtil.generateIssuer();
        issuer.setValue(requestToBeSent.getIssuer());

        // Optional
        String formatEntity = coreProperties.getFormatEntity();
        if (StringUtils.isNotBlank(formatEntity)) {
            issuer.setFormat(formatEntity);
        }

        samlRequest.setIssuer(issuer);
        addRequestedAuthnContext(requestToBeSent, samlRequest);

        // Generate format extensions.
        Extensions formatExtensions = generateExtensions(requestToBeSent);
        // add the extensions to the SAMLAuthnRequest
        samlRequest.setExtensions(formatExtensions);
        addNameIDPolicy(samlRequest, requestToBeSent.getNameIdFormat());

        return samlRequest;
    }

    /**
     *
     * TODO to be removed
     *
     * @deprecated since 1.4
     * Use {@link ProtocolProcessorI#marshallResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime)}
     *
     */
    @Override
    @Nonnull
    @Deprecated
    public Response marshallResponse(@Nonnull IAuthenticationRequest request,
                                     @Nonnull IAuthenticationResponse response,
                                     @Nonnull String ipAddress,
                                     @Nonnull SamlEngineCoreProperties coreProperties) throws EIDASSAMLEngineException {

        //temporary solution for maintaining deprecated method
        final DateTime currentTime = new DateTime();

        return marshallResponse(request,response,ipAddress,coreProperties,currentTime);
    }

    /**
     * @param request the request
     * @param response the authentication response from the IdP
     * @param ipAddress the IP address
     * @param coreProperties the saml engine core properties
     * @param currentTime the current time
     * @return the authentication response
     * @throws EIDASSAMLEngineException
     */
    @Override
    @Nonnull
    public Response marshallResponse(@Nonnull IAuthenticationRequest request,
                                     @Nonnull IAuthenticationResponse response,
                                     @Nonnull String ipAddress,
                                     @Nonnull SamlEngineCoreProperties coreProperties,
                                     @Nonnull final DateTime currentTime) throws EIDASSAMLEngineException {
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
                newResponse(status, request.getAssertionConsumerServiceURL(), request.getId(), coreProperties, currentTime);

        if (StringUtils.isNotBlank(response.getIssuer()) && null != samlResponse.getIssuer()) {
            samlResponse.getIssuer().setValue(SAMLEngineUtils.getValidIssuerValue(response.getIssuer()));
        }

        ImmutableAttributeMap attributes = response.getAttributes();

        //TODO START remove check of erroneous attributes after transition period of EID-423
        /* correcting the marshaled response in order to contain (erroneous) definitions supported by the other party */
        ImmutableSet<AttributeDefinition<?>> supportedAttributes = request.getRequestedAttributes().getDefinitions();
        // cpu consuming fix, restrict it to the special cases
        if ((attributes.getDefinitions().contains(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER) && !supportedAttributes.contains(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER)) ||
                (attributes.getDefinitions().contains(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS) && !supportedAttributes.contains(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS))) {
            ImmutableAttributeMap.Builder correctedAttrs = ImmutableAttributeMap.builder();
            UnmodifiableIterator<ImmutableAttributeMap.ImmutableAttributeEntry<?>> attrIterator = attributes.entrySet().iterator();
            while (attrIterator.hasNext()) {
                ImmutableAttributeMap.ImmutableAttributeEntry<?> attr = attrIterator.next();
                if (attr.getKey().equals(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER) && !supportedAttributes.contains(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER)) {
                    LOG.warn("Switching VATRegistrationNumber to VATRegistration for " + request.getIssuer());
                    ImmutableSet values = attr.getValues();
                    correctedAttrs.put(LegalPersonSpec.Definitions.VAT_REGISTRATION, values);
                } else if (attr.getKey().equals(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS) && !supportedAttributes.contains(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS)) {
                    LOG.warn("Switching LegalPersonAddress to LegalAddress for " + request.getIssuer());
                    ImmutableSet values = attr.getValues();
                    correctedAttrs.put(LegalPersonSpec.Definitions.LEGAL_ADDRESS, values);
                } else if (attr.getKey().equals(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER)) {
                    LOG.warn("Switching LegalPersonAddress to RepresentativeVATRegistration for " + request.getIssuer());
                    ImmutableSet values = attr.getValues();
                    correctedAttrs.put(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER, values);
                } else if (attr.getKey().equals(RepresentativeLegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS)) {
                    LOG.warn("Switching RepresentativeLegalPersonAddress to RepresentativeLegalAddress for " + request.getIssuer());
                    ImmutableSet values = attr.getValues();
                    correctedAttrs.put(RepresentativeLegalPersonSpec.Definitions.LEGAL_ADDRESS, values);
                } else {
                    ImmutableSet values = attr.getValues();
                    correctedAttrs.put(attr.getKey(), values);
                }
            }
            attributes = correctedAttrs.build();
        }
        //TODO END remove check of erroneous attributes after transition period of EID-423

        DateTime notOnOrAfter = currentTime.plusSeconds(coreProperties.getTimeNotOnOrAfter().intValue());

        Assertion assertion =
                AssertionUtil.generateResponseAssertion(false, ipAddress, request, samlResponse.getIssuer(),
                                                        attributes, notOnOrAfter,
                                                        coreProperties.getFormatEntity(), coreProperties.getResponder(),
                                                        getFormat(), coreProperties.isOneTimeUse(), currentTime);

        AttributeStatement attrStatement = generateResponseAttributeStatement(attributes);

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
     * @param currentTime the saml engine clock
     * @return the response
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Nonnull
    private Response newResponse(@Nonnull Status status,
                                 @Nullable String assertConsumerURL,
                                 @Nonnull String inResponseTo,
                                 @Nonnull SamlEngineCoreProperties coreProperties,
                                 @Nonnull final DateTime currentTime) throws EIDASSAMLEngineException {
        LOG.debug("Generate Authentication Response base.");
        Response response =
                BuilderFactoryUtil.generateResponse(SAMLEngineUtils.generateNCName(), currentTime,
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

    protected void registerRequestNamespace(@Nonnull XMLObject xmlObject) {
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlObject.getNamespaceManager().registerNamespace(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlObject.getNamespaceManager()
                .registerNamespace(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));

        xmlObject.getNamespaceManager()
                .registerNamespace(
                        new Namespace(SAMLCore.EIDAS10_SAML_NS.getValue(), SAMLCore.EIDAS10_SAML_PREFIX.getValue()));
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
                .registerNamespace(new Namespace(SAMLCore.EIDAS10_RESPONSESAML_NS.getValue(),
                                                 SAMLCore.EIDAS10_SAML_PREFIX.getValue()));
    }

    @Override
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

    @Override
    @Nonnull
    public IAuthenticationRequest unmarshallRequest(@Nonnull String citizenCountryCode,
                                                    @Nonnull AuthnRequest samlRequest,
                                                    @Nullable String originCountryCode)
            throws EIDASSAMLEngineException {
        LOG.debug("Process the extensions for EIDAS messageFormat");
        Extensions extensions = samlRequest.getExtensions();
        RequestedAttributes requestedAttr =
                (RequestedAttributes) extensions.getUnknownXMLObjects(RequestedAttributes.DEF_ELEMENT_NAME).get(0);

        List<RequestedAttribute> reqAttrs = requestedAttr.getAttributes();

        ImmutableAttributeMap.Builder attributeMapBuilder = new ImmutableAttributeMap.Builder();
        //TODO START postprocess erroneous attributes of EID-423, remove this one after transition period
        boolean addressProcessed = false;
        boolean vatProcessed = false;
        //TODO END postprocess erroneous attributes of EID-423, remove this one after transition period
        for (RequestedAttribute requestedAttribute : reqAttrs) {
            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNullable(requestedAttribute.getName());
            checkRequiredAttributeCompiles(attributeDefinition, requestedAttribute);
            if (null != attributeDefinition) {
                // Check if friendlyName matches when provided -- TODO temoprary disabled due to validator failure
/*              String friendlyName = requestedAttribute.getFriendlyName();
                if (StringUtils.isNotEmpty(friendlyName) &&
                        attributeDefinition != null &&
                        !friendlyName.equals(attributeDefinition.getFriendlyName())) {
                    LOG.error("BUSINESS EXCEPTION : Illegal Attribute friendlyName for " + attributeDefinition.getNameUri().toString() +
                            " expected " +  attributeDefinition.getFriendlyName() + " got " + friendlyName);
                    throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                            EidasErrorKey.INTERNAL_ERROR.errorCode(), "Illegal Attribute friendlyName for " + attributeDefinition.getNameUri().toString() +
                            " expected " +  attributeDefinition.getFriendlyName() + " got " + friendlyName);
                }*/
                //TODO START process erroneous attributes of EID-423, remove this one after transition period
                /* optional fix: replace erroneous attributes at ProtocolProcessor layer, if both of them are requested, just discard the erroneous ones
                   normally it is done in CitizenConsentServlet in the Node, this code is only for those who are simply reusing the engine
                if (attributeDefinition.equals(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS)) {
                    if (addressProcessed) {
                        LOG.warn("Removing LegalPersonAddress from Requested attributes because it's already added instead of LegalAddress");
                        continue;
                    } else {
                        addressProcessed = true;
                    }
                }
                if (attributeDefinition.equals(LegalPersonSpec.Definitions.LEGAL_ADDRESS)) {
                    if (addressProcessed) {
                        LOG.warn("Removing LegalAddress from Requested attributes because LegalPersonAddress is already there");
                        continue;
                    } else {
                        LOG.warn("Replacing LegalAddress with LegalPersonAddress in the Request");
                        attributeDefinition = LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS;
                        addressProcessed = true;
                    }
                }
                if (attributeDefinition.equals(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER)) {
                    if (vatProcessed) {
                        LOG.warn("Removing VATRegistrationNumber from Requested attributes beacuse it's already added instead of VATRegisration");
                        continue;
                    } else {
                        vatProcessed = true;
                    }
                }
                if (attributeDefinition.equals(LegalPersonSpec.Definitions.VAT_REGISTRATION)) {
                    if (vatProcessed) {
                        LOG.warn("Removing VATRegistration from Requested attributes because VATRegistrationNumber is already there");
                        continue;
                    } else {
                        LOG.warn("Replacing VATRegistration with VATRegistrationNumber in the Request");
                        attributeDefinition = LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER;
                        vatProcessed = true;
                    }
                }*/
                //TODO END process erroneous attributes of EID-423, remove this one after transition period

                List<String> stringValues = new ArrayList<>();
                for (XMLObject xmlObject : requestedAttribute.getOrderedChildren()) {
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
                attributeDefinition = validateRequestedAttribute(requestedAttribute, attributeDefinition);

                AttributeValueMarshaller<?> attributeValueMarshaller =
                        attributeDefinition.getAttributeValueMarshaller();
                ImmutableSet.Builder<eu.eidas.auth.commons.attribute.AttributeValue<?>> setBuilder =
                        ImmutableSet.builder();
                for (final String value : stringValues) {
                    eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue;
                    try {
                        attributeValue = attributeValueMarshaller.unmarshal(value, false);
                        setBuilder.add(attributeValue);
                    } catch (AttributeValueMarshallingException e) {
                        LOG.error("Illegal attribute value: " + e, e);
                        throw new EIDASSAMLEngineException(
                                EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()), e);
                    }
                }
                attributeMapBuilder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) setBuilder.build());
            } else {
                LOG.info(AbstractProtocolEngine.SAML_EXCHANGE,
                         "BUSINESS EXCEPTION : Attribute name: {} was not found. It will be removed from the request object",
                         requestedAttribute.getName());
            }
        }

        EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder();
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

        EidasAuthenticationRequest request;
        try {
            request = builder.build();
        } catch (IllegalArgumentException e) {
            throw new EIDASSAMLEngineException(
                    EidasErrors.get(EidasErrorKey.ILLEGAL_ARGUMENTS_IN_BUILDER.errorCode()) + " - " + e.getMessage(),
                    e);
        }

        validateReceivedRequest(request);

        return request;
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
            LOG.trace("Set ipAddress.");
            String ipAddress = ResponseUtil.extractSubjectConfirmationIPAddress(assertion);
            builder.ipAddress(ipAddress);
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

    @Override
    @Nonnull
    public IAuthenticationRequest updateRequestWithConsent(@Nonnull IAuthenticationRequest request,
                                                           @Nonnull ImmutableAttributeMap consentedAttributes) {
        return EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) request)
                .requestedAttributes(consentedAttributes)
                .build();
    }

    /**
     * Validates an input {@link IAuthenticationRequest} received by this processor (incoming).
     *
     * @param request the request.
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    protected void validateReceivedRequest(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {

        validateRequestType(request);

        checkRequestSanity(request);

        validateProviderName(request);

        validateLevelOfAssurance(request);
    }

    /**
     * Validates an input {@link IAuthenticationRequest} when it is being sent by this processor (e.g. the
     * Connector-side) (outgoing).
     * <p>
     * For instance, the given {@link IAuthenticationRequest} could come from the specific at the SP-side.
     *
     * @param request the request.
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Nonnull
    private IAuthenticationRequest validateRequestAgainstMetadata(@Nonnull IAuthenticationRequest request,
                                                                  @Nonnull String serviceIssuerMetadataUrl,
                                                                  @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException {

        LOG.trace("Validate parameters from authentication request.");

        validateRequestType(request);

        validateProviderName(request);

        validateLevelOfAssurance(request);

        String issuer = getValidIssuerValue(request, coreProperties);

        String bindingMethod = SAMLEngineUtils.getBindingMethod(getProtocolBinding(request, coreProperties));

        // Always generate a new ID whatever the incoming input
        String id = SAMLEngineUtils.generateNCName();

        EidasAuthenticationRequest.Builder updatedRequestBuilder =
                EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) request)
                        .id(id)
                        .issuer(issuer)
                        .binding(bindingMethod)
                        .levelOfAssuranceComparison(LevelOfAssuranceComparison.MINIMUM);

        Set<String> supportedAttributes = getSupportedAttributes(serviceIssuerMetadataUrl);
        if (CollectionUtils.isNotEmpty(supportedAttributes)) {
            ImmutableAttributeMap filteredAttributes =
                    filterSupportedAttributeNames(request.getRequestedAttributes(), supportedAttributes,
                                                  request.getIssuer(), serviceIssuerMetadataUrl);

            updatedRequestBuilder.requestedAttributes(filteredAttributes);
        }

        IAuthenticationRequest updatedRequest;

        try {
            updatedRequest = updatedRequestBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new EIDASSAMLEngineException(
                    EidasErrors.get(EidasErrorKey.ILLEGAL_ARGUMENTS_IN_BUILDER.errorCode()) + " - "
                            + e.getMessage(), e);
        }

        if (updatedRequest.getRequestedAttributes().isEmpty()) {
            String errorDetail = "No requested attribute (request issuer: " + request.getIssuer() + " - serviceIssuer: "
                    + serviceIssuerMetadataUrl + ")";
            LOG.error(SAML_EXCHANGE, errorDetail);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL.errorCode()),
                                               EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL.errorMessage()),
                                               errorDetail);
        }

        return updatedRequest;
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
}
