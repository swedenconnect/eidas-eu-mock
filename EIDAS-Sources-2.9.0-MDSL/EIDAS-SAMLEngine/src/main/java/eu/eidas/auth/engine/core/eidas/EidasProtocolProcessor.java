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
package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.LegalPersonSpec;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.eidas.spec.EidasSAMLFormat;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.HighLevelMetadataParams;
import eu.eidas.auth.engine.metadata.HighLevelMetadataParamsI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.samlobjects.SPType;
import eu.eidas.auth.engine.xml.opensaml.AssertionUtil;
import eu.eidas.auth.engine.xml.opensaml.BuilderFactoryUtil;
import eu.eidas.auth.engine.xml.opensaml.ResponseUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.RequesterID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Scoping;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implements the eIDAS protocol.
 */
public class EidasProtocolProcessor implements ProtocolProcessorI {

    public static final QName EIDAS_REQUESTED_ATTRIBUTE_VALUE_TYPE =
            new QName(SAMLCore.EIDAS10_SAML_NS.getValue(), "AttributeValue", SAMLCore.EIDAS10_PREFIX.getValue());

    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");

    /**
     * The default instance only implements the eIDAS specification without any additional attribute.
     */
    public static final EidasProtocolProcessor INSTANCE = new EidasProtocolProcessor(null, null, null);

    /**
     * The LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasProtocolProcessor.class);

    static {
        INSTANCE.configure(null);
    }

    @Nonnull
    private final AttributeRegistry eidasAttributeRegistry;

    @Nonnull
    private final AttributeRegistry additionalAttributeRegistry;

    @Nullable
    private final MetadataFetcherI metadataFetcher;

    @Nullable
    private final MetadataSignerI metadataSigner;

    @Nullable
    private final MetadataClockI metadataClock;

    private EidasSAMLFormat formatDescriptor = new EidasSAMLFormat();

    public static final AttributeRegistry.AttributeDefinitionFilter MANDATORY_LEGAL_FILTER =
            new AttributeRegistry.AttributeDefinitionFilter() {

                @Override
                public boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition) {
                    return attributeDefinition.isRequired()
                            && attributeDefinition.getPersonType() == PersonType.LEGAL_PERSON;
                }
            };

    public static final AttributeRegistry.AttributeDefinitionFilter MANDATORY_REPRESENTATIVE_LEGAL_FILTER =
            new AttributeRegistry.AttributeDefinitionFilter() {
                @Override
                public boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition) {
                    return attributeDefinition.isRequired()
                            && attributeDefinition.getPersonType() == PersonType.REPV_LEGAL_PERSON;
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

    public static final AttributeRegistry.AttributeDefinitionFilter MANDATORY_REPRESENTATIVE_NATURAL_FILTER =
            new AttributeRegistry.AttributeDefinitionFilter() {
                @Override
                public boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition) {
                    return attributeDefinition.isRequired()
                            && attributeDefinition.getPersonType() == PersonType.REPV_NATURAL_PERSON;
                }
            };

    @SuppressWarnings("squid:S2637")
    public EidasProtocolProcessor(@Nullable MetadataFetcherI metadataFetcher,
                                  @Nullable MetadataSignerI metadataSigner,
                                  @Nullable MetadataClockI metadataClock) {
        this(EidasSpec.REGISTRY, AttributeRegistries.empty(), metadataFetcher, metadataSigner, metadataClock);
    }

    @SuppressWarnings("squid:S2637")
    public EidasProtocolProcessor(@Nonnull AttributeRegistry additionalAttributeRegistry,
                                  @Nullable MetadataFetcherI metadataFetcher,
                                  @Nullable MetadataSignerI metadataSigner,
                                  @Nullable MetadataClockI metadataClock) {
        this(EidasSpec.REGISTRY, additionalAttributeRegistry, metadataFetcher, metadataSigner, metadataClock);
    }

    public EidasProtocolProcessor(@Nonnull String eidasAttributesFileNameVal,
                                  @Nonnull String additionalAttributesFileNameVal,
                                  @Nullable String defaultPath,
                                  @Nullable MetadataFetcherI metadataFetcherVal,
                                  @Nullable MetadataSignerI metadataSignerVal,
                                  @Nullable MetadataClockI metadataClockVal) {
        Preconditions.checkNotNull(eidasAttributesFileNameVal, "eidasAttributesFileName");
        Preconditions.checkNotNull(additionalAttributesFileNameVal, "additionalAttributesFileName");
        eidasAttributeRegistry = AttributeRegistries.fromFile(eidasAttributesFileNameVal, defaultPath);
        additionalAttributeRegistry = AttributeRegistries.fromFile(additionalAttributesFileNameVal, defaultPath);
        metadataFetcher = metadataFetcherVal;
        metadataSigner = metadataSignerVal;
        metadataClock = metadataClockVal;
    }

    public EidasProtocolProcessor(@Nullable AttributeRegistry eidasAttributeRegistryVal,
                                  @Nullable AttributeRegistry additionalAttributeRegistryVal,
                                  @Nullable MetadataFetcherI metadataFetcherVal,
                                  @Nullable MetadataSignerI metadataSignerVal,
                                  @Nullable MetadataClockI metadataClockVal) {
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
        this.metadataClock = metadataClockVal;
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
                throw new EIDASSAMLEngineException(EidasErrorKey.CONNECTOR_INVALID_SPTYPE,
                        "Unable to create SPType Object by SAML engine - wrong value supplied in light request : " + request.getSpType());
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
        NameIDPolicy policy = (NameIDPolicy) BuilderFactoryUtil.buildXmlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        policy.setFormat(selectedNameID);
        policy.setAllowCreate(true);
        request.setNameIDPolicy(policy);
    }

    private RequestedAuthnContext buildRequestedAuthnContext(IEidasAuthenticationRequest eidasRequest)
            throws EIDASSAMLEngineException {
        RequestedAuthnContext authnContext =
                (RequestedAuthnContext) BuilderFactoryUtil.buildXmlObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);

        if (authnContext == null) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "Unable to create SAML Object DEFAULT_ELEMENT_NAME");
        }

        authnContext.setComparison(mapLevelOfAssuranceComparison(eidasRequest));

        List<AuthnContextClassRef> authnContextClassRefs = buildAuthnContextClassRefsList(eidasRequest);
        authnContext.getAuthnContextClassRefs().addAll(authnContextClassRefs);
        return authnContext;
    }

    private AuthnContextComparisonTypeEnumeration mapLevelOfAssuranceComparison(IEidasAuthenticationRequest eidasRequest) {

        if (LevelOfAssuranceComparison.MINIMUM.equals(eidasRequest.getLevelOfAssuranceComparison())) {
            return AuthnContextComparisonTypeEnumeration.MINIMUM;
        } else if (LevelOfAssuranceComparison.EXACT.equals(eidasRequest.getLevelOfAssuranceComparison())) {
            return AuthnContextComparisonTypeEnumeration.EXACT;
        } else {
            return AuthnContextComparisonTypeEnumeration.EXACT;
        }
    }

    private List<AuthnContextClassRef> buildAuthnContextClassRefsList(IEidasAuthenticationRequest eidasRequest)
            throws EIDASSAMLEngineException {
        List<AuthnContextClassRef> contextClassRefList = new ArrayList<>();
        AuthnContextClassRef authnContextClassRef;
        if (eidasRequest.getEidasLevelOfAssurance() != null) {
            authnContextClassRef = buildAuthnContextClassRef(eidasRequest.getEidasLevelOfAssurance().stringValue());
            contextClassRefList.add(authnContextClassRef);
            if (LevelOfAssuranceComparison.EXACT.equals(eidasRequest.getLevelOfAssuranceComparison())) {
                for (NotifiedLevelOfAssurance levelOfAssurance: eidasRequest.getEidasLevelOfAssurance().getHigherLevelsOfAssurance()) {
                    authnContextClassRef = buildAuthnContextClassRef(levelOfAssurance.stringValue());
                    contextClassRefList.add(authnContextClassRef);
                }
            }
        }

        if (eidasRequest.getNonNotifiedLevelsOfAssurance() != null) {
            for (String levelOfAssurance: eidasRequest.getNonNotifiedLevelsOfAssurance()) {
                authnContextClassRef = buildAuthnContextClassRef(levelOfAssurance);
                contextClassRefList.add(authnContextClassRef);
            }
        }
        return contextClassRefList;
    }

    private AuthnContextClassRef buildAuthnContextClassRef(String levelOfAssurance)
            throws EIDASSAMLEngineException {
        AuthnContextClassRef authnContextClassRef =
                (AuthnContextClassRef) BuilderFactoryUtil.buildXmlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setURI(levelOfAssurance);
        return authnContextClassRef;
    }

    private void validateRequestType(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException {
        if (!(request instanceof IEidasAuthenticationRequest)) {
            String errorDetail = "ProtocolEngine: Request does not implement IEidasAuthenticationRequest: " + request;
            throwValidationException(errorDetail);
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

        XSBooleanValue requestedIsRequired = requestedAttribute.isRequiredXSBoolean();

        if (null == requestedIsRequired || requestedIsRequired.getValue().booleanValue() == staticDefinition.isRequired()) {
            return staticDefinition;
        }
        // the attribute is an additional attribute, therefore it does not have to comply with a static definition
        // Update the required flag according to the request instead of using the local registry:
        return AttributeDefinition.builder(staticDefinition).required(requestedIsRequired.getValue().booleanValue()).build();
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

    private void validateRequesterId(@Nonnull String requesterId) throws EIDASSAMLEngineException {
        if (requesterId != null && requesterId.length() > 1024) {
            String errorDetail = "RequesterID value cannot be more than 1024 characters long";
            throwValidationException(errorDetail);
        }
    }

    private void throwValidationException(@Nonnull String errorDetail) throws EIDASSAMLEngineException {
        throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, errorDetail);
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
            authnContextClassRef.setURI(response.getLevelOfAssurance());
        }
    }

    private void addToAttributeList(@Nonnull List<Attribute> list,
                                    @Nonnull AttributeDefinition<?> attributeDefinition,
                                    @Nonnull
                                    Set<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>> values)
            throws EIDASSAMLEngineException {
        // TODO take transliteration into account

        AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
        Set<String> marshalledValues = new LinkedHashSet<>();
        for (final eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue : values) {
            try {
                String marshalledValue = attributeValueMarshaller.marshal(
                        (eu.eidas.auth.commons.attribute.AttributeValue) attributeValue);
                marshalledValues.add(marshalledValue);
            } catch (AttributeValueMarshallingException e) {
                LOG.error("Illegal attribute value: " + e, e);
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "Illegal attribute value: ", e);
            }
        }

        list.add(generateAttrSimple(attributeDefinition, marshalledValues));
    }

    /**
     * Checks whether the attribute list contains at least one of the mandatory eIDAS attribute set (either for a
     * natural [person or for a legal person)
     *
     * @param immutableAttributeMap the attribute map.
     */
    @Override
    public boolean checkMandatoryAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap) {
        boolean requestedLegalSet = false;
        boolean requestedNaturalSet = false;
        boolean returnedNaturalRepresentative = false;
        boolean returnedLegalRepresentative = false;
        if (null == immutableAttributeMap || immutableAttributeMap.isEmpty()) {
            return false;
        }

        Set<AttributeDefinition<?>> mandatoryLegalAttributes =
                new HashSet<>(getFilteredAttributes(EidasProtocolProcessor.MANDATORY_LEGAL_FILTER));

        Set<AttributeDefinition<?>> mandatoryRepresentativeLegalAttributes =
                new HashSet<>(getFilteredAttributes(EidasProtocolProcessor.MANDATORY_REPRESENTATIVE_LEGAL_FILTER));

        Set<AttributeDefinition<?>> mandatoryNaturalAttributes =
                new HashSet<>(getFilteredAttributes(EidasProtocolProcessor.MANDATORY_NATURAL_FILTER));

        Set<AttributeDefinition<?>> mandatoryRepresentativeNaturalAttributes =
                new HashSet<>(getFilteredAttributes(EidasProtocolProcessor.MANDATORY_REPRESENTATIVE_NATURAL_FILTER));

        for (AttributeDefinition<?> attributeDefinition : immutableAttributeMap.getDefinitions()) {
            if (null == attributeDefinition) {
                continue;
            }
            if (attributeDefinition.getPersonType() == PersonType.LEGAL_PERSON) {
                mandatoryLegalAttributes.remove(attributeDefinition);
                requestedLegalSet = true;
            }

            if (attributeDefinition.getPersonType() == PersonType.REPV_LEGAL_PERSON) {
                mandatoryRepresentativeLegalAttributes.remove(attributeDefinition);
                returnedLegalRepresentative = true;
            }

            if (attributeDefinition.getPersonType() == PersonType.NATURAL_PERSON) {
                mandatoryNaturalAttributes.remove(attributeDefinition);
                requestedNaturalSet = true;
            }
            if (attributeDefinition.getPersonType() == PersonType.REPV_NATURAL_PERSON) {
                mandatoryRepresentativeNaturalAttributes.remove(attributeDefinition);
                returnedNaturalRepresentative = true;
            }
        }

        final boolean naturalAndLegalNotRequested = !(requestedNaturalSet || requestedLegalSet);
        final boolean naturalPersonIncompleteMinimumDataSet = requestedNaturalSet && !mandatoryNaturalAttributes.isEmpty();
        final boolean legalPersonIncompleteMinimumDataSet = requestedLegalSet && !mandatoryLegalAttributes.isEmpty();
        final boolean naturalRepresentativeIncompleteMinimumDataSet = returnedNaturalRepresentative && !mandatoryRepresentativeNaturalAttributes.isEmpty();
        final boolean legalRepresentativeIncompleteMinimumDataSet = returnedLegalRepresentative && !mandatoryRepresentativeLegalAttributes.isEmpty();
        final boolean representativeNotUsed = !(returnedNaturalRepresentative || returnedLegalRepresentative);

        if (naturalPersonIncompleteMinimumDataSet) {
            LOG.info("Mandatory naturalPerson attributes not requested : {}", mandatoryNaturalAttributes);
        }
        if (legalPersonIncompleteMinimumDataSet) {
            LOG.info("Mandatory legalPerson attributes not requested : {}", mandatoryLegalAttributes);
        }
        if (naturalRepresentativeIncompleteMinimumDataSet) {
            LOG.info("Mandatory naturalPerson representative attributes not requested : {}", mandatoryRepresentativeNaturalAttributes);
        }
        if (legalRepresentativeIncompleteMinimumDataSet) {
            LOG.info("Mandatory legalPerson representative attributes not requested : {}", mandatoryRepresentativeLegalAttributes);
        }
        if (naturalAndLegalNotRequested) {
            LOG.info("naturalPerson or legalPerson not requested");
        }

        return !naturalAndLegalNotRequested
                && !(legalPersonIncompleteMinimumDataSet || naturalPersonIncompleteMinimumDataSet)
                && (representativeNotUsed || !(naturalRepresentativeIncompleteMinimumDataSet || legalRepresentativeIncompleteMinimumDataSet));
    }

    /**
     * Checks if representation response rules are respected.
     *
     * Since restriction on Maximum number of MDS was decided not to be enforced this method always returns true
     * which might change in the future therefore method was maintained.
     *
     * @param immutableAttributeMap the attribute map of attributes.
     * @return true if representation response rules are respected, false otherwise
     */
    @Override
    public boolean checkRepresentationResponse(ImmutableAttributeMap immutableAttributeMap) {
        return true;
    }

    /**
     * Checks whether the attribute list fulfills the requirements of representative scenario
     *
     * According to Specs 1.1 representative attributes MUST not be requested
     *
     * @param immutableAttributeMap the attribute map.
     */
    @Override
    public boolean checkRepresentativeAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap) {
        boolean checkResult = true;

        for (AttributeDefinition<?> attributeDefinition : immutableAttributeMap.getDefinitions()) {
            if (isRepresentativeAttribute(attributeDefinition)) {
                checkResult = false;
            }
        }
        return checkResult;
    }

    private boolean isRepresentativeAttribute(AttributeDefinition<?> attributeDefinition) {
        return (null != attributeDefinition &&
                (attributeDefinition.getPersonType() == PersonType.REPV_LEGAL_PERSON ||
                        attributeDefinition.getPersonType() == PersonType.REPV_NATURAL_PERSON));
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
     * @return the attribute from {@link AttributeDefinition}
     * @throws EIDASSAMLEngineException in case of errors
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
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                        "Eidas Attribute: " + requestedAttribute + " does not comply with the eIDAS specification(" + specAttribute + ").");
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
                    throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR, "ERROR :  transformer exception: ", e);
                }
            }
            return xsAny.getTextContent();
        }
        return null;
    }

    public void configure(final ProtocolSignerI protocolSigner) {
        OpenSamlHelper.initialize();
        EidasExtensionConfiguration.configureExtension(this, protocolSigner);
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

            Set<eu.eidas.auth.commons.attribute.AttributeValue<?>> valueSet = new LinkedHashSet<>();

            List<XMLObject> values = attribute.getOrderedChildren();

            QName xmlType = attributeDefinition.getXmlType();

            QName latinScript = new QName(xmlType.getNamespaceURI(), "LatinScript", xmlType.getPrefix());

            // Process the values.
            for (XMLObject xmlObject : values) {
                try {
                    if (xmlObject instanceof XSStringImpl) {
                        valueSet.add(
                                attributeValueMarshaller.unmarshal(((XSStringImpl) xmlObject).getValue(), false));
                    } else if (xmlObject instanceof XSAnyImpl) {
                        XSAnyImpl xsAny = (XSAnyImpl) xmlObject;

                        boolean isNonLatinScriptAlternateVersion = false;
                        String latinScriptAttrValue = xsAny.getUnknownAttributes().get(latinScript);
                        if (StringUtils.isNotBlank(latinScriptAttrValue) && "false".equals(latinScriptAttrValue)) {
                            isNonLatinScriptAlternateVersion = true;
                        }

                        valueSet.add(attributeValueMarshaller.unmarshal(xsAny.getTextContent(),
                                                                              isNonLatinScriptAlternateVersion));

                    } else {
                        LOG.info("BUSINESS EXCEPTION : attribute value is unknown in generatePersonalAttributeList.");
                        throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR,
                                "Attribute value is unknown for \""
                                        + attributeDefinition.getNameUri().toASCIIString()
                                        + "\" - value: \"" + xmlObject + "\"");
                    }
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("BUSINESS EXCEPTION : Illegal Attribute Value: " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR,
                            "BUSINESS EXCEPTION : Illegal Attribute Value: ", e);
                }
            }

            mapBuilder.put(attributeDefinition, (Set) valueSet);
        }

        return mapBuilder.build();
    }

    private XMLObject createAttributeValue(AttributeDefinition<?> attributeDefinition,
                                           QName attributeValueType,
                                           String value) {
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        QName xmlType = attributeDefinition.getXmlType();

        if (XSString.TYPE_NAME.equals(xmlType)) {
            XMLObjectBuilder xmlObjectBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
            XSString xsString = (XSString) xmlObjectBuilder.buildObject(attributeValueType, xmlType);
            xsString.setValue(value);
            return xsString;
        }

        XMLObjectBuilder xmlObjectBuilder = builderFactory.getBuilder(XSAny.TYPE_NAME);
        XSAny anyValue = (XSAny) xmlObjectBuilder.buildObject(attributeValueType, xmlType);

        anyValue.getNamespaceManager().registerNamespaceDeclaration(new Namespace(xmlType.getNamespaceURI(), xmlType.getPrefix()));

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

    private AuthenticationResponse.Builder createResponseBuilder(Response samlResponse) throws EIDASSAMLEngineException {
        LOG.trace("Create EidasAuthResponse.");
        AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();

        responseBuilder.country(getCountryCode(samlResponse));;

        LOG.trace("Set ID.");
        responseBuilder.id(samlResponse.getID());
        LOG.trace("Set InResponseTo.");
        responseBuilder.inResponseTo(samlResponse.getInResponseTo());

        responseBuilder.issuer(samlResponse.getIssuer().getValue());
        responseBuilder.consent(samlResponse.getConsent());

        responseBuilder.encrypted(
                samlResponse.getEncryptedAssertions() != null && !samlResponse.getEncryptedAssertions().isEmpty());
        return responseBuilder;
    }

    /**
     * Retrieve origin country code from the metadata of the issuer {@link EIDASValues#EIDAS_NODE_COUNTRY}
     * or (as backward compatibility behavior) if not present in metadata from the signature certificate
     * @param signableSAMLObject the signed saml object.
     * @return the country code.
     * @throws EIDASSAMLEngineException when an error occurred try to get the country code from the signature
     */
    public String getCountryCode(SignableSAMLObject signableSAMLObject) throws EIDASSAMLEngineException {
        return getMetadataCountryCode(signableSAMLObject)
                .orElseThrow(() -> new EIDASSAMLEngineException(
                        EidasErrorKey.SAML_ENGINE_INVALID_METADATA,
                        "Country code was not present in colleague metadata"));
    }

    private Optional<String> getMetadataCountryCode(SignableSAMLObject signableSAMLObject) {
        return getIssuer(signableSAMLObject)
                .map(XSString::getValue)
                .flatMap(this::getMetadataParametersOrEmpty)
                .map(EidasMetadataParametersI::getNodeCountry);
    }

    private Optional <HighLevelMetadataParamsI> getMetadataParametersOrEmpty(String metadataUrl) {
        try {
            return Optional.of(getMetadataParameters(metadataUrl));
        } catch (EIDASSAMLEngineException e) {
            return Optional.empty();
        }
    }

    private Optional<Issuer> getIssuer(SignableSAMLObject signableSAMLObject) {
        Issuer issuer = null;
        if (signableSAMLObject instanceof RequestAbstractType){
            issuer = ((RequestAbstractType)signableSAMLObject).getIssuer();
        }else if (signableSAMLObject instanceof Response){
            issuer = ((Response)signableSAMLObject).getIssuer();
        }
        return Optional.ofNullable(issuer);
    }

    @Nonnull
    @Override
    public HighLevelMetadataParamsI getMetadataParameters(String metadataUrl) throws EIDASSAMLEngineException {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(metadataUrl);
        return this.getMetadataParameters(issuer);
    }

    @Nonnull
    @Override
    public HighLevelMetadataParamsI getMetadataParameters(SignableSAMLObject samlMessage) throws EIDASSAMLEngineException {
        final Optional<KeyInfo> keyInfo = Optional.ofNullable(samlMessage.getSignature()).map(Signature::getKeyInfo);

        if (keyInfo.isPresent()) {
            if (samlMessage instanceof Response) {
                return this.getMetadataParameters(((Response)samlMessage).getIssuer(), keyInfo.get());
            }

            if (samlMessage instanceof AuthnRequest) {
                return this.getMetadataParameters(((AuthnRequest)samlMessage).getIssuer(), keyInfo.get());
            }

            throw new EIDASSAMLEngineException(
                    EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Signable SAMLObject Type not supported.");
        }
        throw new EIDASSAMLEngineException(
                EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                "No signature present to verify integrity.");
    }

    private HighLevelMetadataParamsI getMetadataParameters(Issuer metadataIssuer) throws EIDASSAMLEngineException {
        return getMetadataParameters(metadataIssuer, null);
    }

    private HighLevelMetadataParamsI getMetadataParameters(Issuer metadataIssuer, KeyInfo keyInfo) throws EIDASSAMLEngineException {
        try {
            assert metadataFetcher != null;
            final EidasMetadataParametersI eidasMetadata = metadataFetcher.getEidasMetadata(metadataIssuer, keyInfo, metadataSigner, metadataClock);
            assert eidasMetadata != null;
            return new HighLevelMetadataParams(eidasMetadata);
        } catch (EIDASMetadataException e) {
            String metadataUrl = metadataIssuer.getValue();
            LOG.warn("Metadata at " + metadataUrl + " couldn't be fetched.");
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_NO_METADATA,
                    "Unable to retrieve metadata for " + metadataUrl, e);
        }
    }

    /**
     * Extract requesterID from the SAMLObject according to SAML core specifications
     *
     * @param authnRequest the eidas request as SAMLObject
     * @return the first requesterID value if present, otherwise null.
     * @throws EIDASSAMLEngineException in case of invalid requesterID value
     */
    protected String extractRequesterId(AuthnRequest authnRequest) throws EIDASSAMLEngineException {
        if (authnRequest.getScoping() != null && authnRequest.getScoping().getRequesterIDs() != null
                && !authnRequest.getScoping().getRequesterIDs().isEmpty()) {
            // eIDAS-Node only handles one RequesterID
            String requesterId = authnRequest.getScoping().getRequesterIDs().get(0).getURI();
            validateRequesterId(requesterId);
            return requesterId;
        }
        return null;
    }

    /**
     * Extracts the levels of assurance in the RequestedAuthContext of the authRequest
     *
     * @param authnRequest the request data
     * @return List of LevelOfAssurance
     * @throws EIDASSAMLEngineException in case of invalid level of assurance value
     */
    protected List<LevelOfAssurance> extractLevelsOfAssurance(AuthnRequest authnRequest) throws EIDASSAMLEngineException {
        RequestedAuthnContext authnContext = authnRequest.getRequestedAuthnContext();
        List<LevelOfAssurance> loaList = new ArrayList<>();
        if (authnContext != null && authnContext.getAuthnContextClassRefs() != null) {
            if (null == authnContext.getComparison()) {
                authnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
            }
            List<AuthnContextClassRef> authnContextClassRefs = authnContext.getAuthnContextClassRefs();
            try {
                for (AuthnContextClassRef contextRef : authnContextClassRefs) {
                    LevelOfAssurance level = LevelOfAssurance.build(contextRef.getURI());
                    loaList.add(level);
                }
            } catch (IllegalArgumentException e) {
                LOG.error("Invalid Level of Assurance: " + e, e);
                throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA,
                        "Invalid Level of Assurance: ", e);
            }
        }
        return loaList;
    }

    private void fillRequestedAttributes(IAuthenticationRequest request, RequestedAttributes reqAttributes)
            throws EIDASSAMLEngineException {
        LOG.trace("SAML Engine configuration properties load.");
        for (final Map.Entry<AttributeDefinition<?>, Set<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : request
                .getRequestedAttributes()
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();

            attributeDefinition = checkRequestedAttribute(attributeDefinition);

            LOG.trace("Generate requested attribute: " + attributeDefinition);
            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
            Set<String> values = new LinkedHashSet<>();
            for (final eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue : entry.getValue()) {
                try {
                    String marshalledValue = attributeValueMarshaller.marshal(
                            (eu.eidas.auth.commons.attribute.AttributeValue) attributeValue);
                    values.add(marshalledValue);
                } catch (AttributeValueMarshallingException e) {
                    LOG.error("Illegal attribute value: " + e, e);
                    throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "Illegal attribute value: ", e);
                }
            }

            RequestedAttribute requestedAttr = generateReqAuthnAttributeSimple(attributeDefinition, values);

            // Add requested attribute.
            reqAttributes.getAttributes().add(requestedAttr);
        }
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
        for (Map.Entry<AttributeDefinition<?>, Set<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : requestedAttributes
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> attributeDefinition = entry.getKey();
            String fullName = attributeDefinition.getNameUri().toASCIIString();
            if (supportedAttributeNames.contains(fullName)) {
                AttributeDefinition<?> validatedDefinition = validateRequestedDefinition(attributeDefinition);
                if (!validatedDefinition.equals(attributeDefinition)) {
                    modified = true;
                }
                builder.put(validatedDefinition, (Set) entry.getValue());
            } else if (attributeDefinition.isRequired()) {
                // TODO use a new error code: the Metadata of the partner does not understand a requested mandatory attribute:
                // Failfast, refuse this request as it cannot be met
                String additionalInformation =
                        "The Metadata of the Service does not contain the requested mandatory attribute \"" + fullName
                                + "\" (request issuer: " + requestIssuer + " - Service metadata URL: "
                                + serviceMetadataURL + ")";
                LOG.error(additionalInformation);
                throw new EIDASSAMLEngineException(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES, additionalInformation);
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
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_ATTRIBUTE_VALUE, "Unable to create SAML object DEFAULT_ELEMENT_NAME");
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

        for (final Map.Entry<AttributeDefinition<?>, Set<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : attributeMap
                .getAttributeMap()
                .entrySet()) {
            // Verification that only one value is permitted, simple or
            // complex, not both.

            Set<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>> value = entry.getValue();
            addToAttributeList(list, entry.getKey(), value);
        }
        return attrStatement;
    }

    @Override
    public AttributeRegistry getAdditionalAttributes() {
        return additionalAttributeRegistry;
    }

    @Override
    public SortedSet<AttributeDefinition<?>> getAllSupportedAttributes() {
        AttributeRegistry minimumDataSetAttributes = getMinimumDataSetAttributes();
        AttributeRegistry additionalAttributes = getAdditionalAttributes();

        SortedSet<AttributeDefinition<?>> allAttributes = new TreeSet<>();
        allAttributes.addAll(minimumDataSetAttributes.getAttributes());
        allAttributes.addAll(additionalAttributes.getAttributes());

        return allAttributes;
    }

    @Nonnull
    private AttributeDefinition<?> getAttributeDefinitionNotNull(@Nonnull String name) throws EIDASSAMLEngineException {
        AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNullable(name);
        if (null == attributeDefinition) {
            LOG.error("BUSINESS EXCEPTION : Attribute name: {} is not known.", name);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR, "Attribute name: " + name + " is not known.");
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
                throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR,
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

    public Set<AttributeDefinition<?>> getFilteredAttributes(
            @Nonnull AttributeRegistry.AttributeDefinitionFilter filter) {
        AttributeRegistry minimumDataSetAttributes = getMinimumDataSetAttributes();
        AttributeRegistry additionalAttributes = getAdditionalAttributes();
        SortedSet<AttributeDefinition<?>> attributeDefinitions = new TreeSet<>();

        for (AttributeDefinition<?> attributeDefinition : minimumDataSetAttributes.getAttributes()) {
            if (filter.accept(attributeDefinition)) {
                attributeDefinitions.add(attributeDefinition);
            }
        }
        for (AttributeDefinition<?> attributeDefinition : additionalAttributes.getAttributes()) {
            if (filter.accept(attributeDefinition)) {
                attributeDefinitions.add(attributeDefinition);
            }
        }
        return attributeDefinitions;
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

    @Override
    @SuppressWarnings("squid:S2583")
    public boolean isAcceptableHttpRequest(@Nonnull IAuthenticationRequest authnRequest, @Nullable String httpMethod) throws EIDASSAMLEngineException {
        if (metadataFetcher == null || metadataSigner == null) {
            return true;
        }
        final String issuer = authnRequest.getIssuer();
        final HighLevelMetadataParamsI metadataParameters = getMetadataParameters(issuer);
        final EidasMetadataRoleParametersI spRoleDescriptor = metadataParameters.getSPRoleDescriptor();
        final String metadataAssertionUrl = spRoleDescriptor.getDefaultAssertionConsumerUrl();

        if (StringUtils.isEmpty(metadataAssertionUrl) || (authnRequest.getAssertionConsumerServiceURL() != null
                && !authnRequest.getAssertionConsumerServiceURL().equals(metadataAssertionUrl))) {
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }

        if (httpMethod != null && StringUtils.isNotBlank(httpMethod)) {
            boolean isBindingValid = false;
            for (String metadataBindingMethod : spRoleDescriptor.getProtocolBindings()) {
                if (httpMethod.equalsIgnoreCase(metadataBindingMethod)) {
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
        String metadataSpType = metadataParameters.getSpType();
        if (authnRequest.getSpType() != null) {
            SpType requestSpType = SpType.fromString(authnRequest.getSpType());
            // both Metadata and Request supplies SP type - not allowed
            if ((requestSpType != null) && (metadataSpType != null) && StringUtils.isNotBlank(metadataSpType)) {
                LOG.error("SPType both in Connector Metadata and Request");
                throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INCONSISTENT_SPTYPE,
                        "SPType both in Connector Metadata and Request");
            }
        } else {
            // neither of them has SPType available
            if (!StringUtils.isNotBlank(metadataSpType)) {
                LOG.error("SPType not provided");
                throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_MISSING_SPTYPE, "SPType not provided");
            }
        }

        return true;
    }

    @Nonnull
    @Override
    @SuppressWarnings("squid:S2583")
    public Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                          @Nonnull IAuthenticationResponse response,
                                          @Nonnull String ipAddress,
                                          @Nonnull SamlEngineCoreProperties coreProperties,
                                          @Nonnull final ZonedDateTime currentTime)
            throws EIDASSAMLEngineException {
        Response responseFail = getResponseWithoutAssertion(request, response, coreProperties, currentTime);

        String responseIssuer = response.getIssuer();
        if (responseIssuer != null && !responseIssuer.isEmpty()) {
            responseFail.getIssuer().setValue(responseIssuer);
        }

        return responseFail;
    }

    private Response getResponseWithoutAssertion(@Nonnull IAuthenticationRequest request,
                                                 @Nonnull IAuthenticationResponse response,
                                                 @Nonnull SamlEngineCoreProperties coreProperties,
                                                 @Nonnull ZonedDateTime currentTime) throws EIDASSAMLEngineException {
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
        return newResponse(status, request.getAssertionConsumerServiceURL(), request.getId(), coreProperties, currentTime);
    }

    private void addAssertionToResponseIfRequesterNeedsOne(@Nonnull IAuthenticationRequest request,
                                                           @Nonnull IAuthenticationResponse response,
                                                           @Nonnull String ipAddress,
                                                           @Nonnull SamlEngineCoreProperties coreProperties,
                                                           @Nonnull ZonedDateTime currentTime,
                                                           List<String> includeAssertionApplicationIdentifiers,
                                                           Response responseFail) throws EIDASSAMLEngineException {
        final String requestApplicationIdentifier = getMetadataParameters(request.getIssuer()).getEidasApplicationIdentifier();
        if (includeAssertionApplicationIdentifiers != null) {
            final boolean isAddAssertionToResponse = includeAssertionApplicationIdentifiers.contains(requestApplicationIdentifier);
            if (isAddAssertionToResponse) {
                addAssertionToResponse(request, response, ipAddress, coreProperties, currentTime, responseFail);
            }
        }
    }

    private void addAssertionToResponse(@Nonnull IAuthenticationRequest request,
                                        @Nonnull IAuthenticationResponse response,
                                        @Nonnull String ipAddress,
                                        @Nonnull SamlEngineCoreProperties coreProperties,
                                        @Nonnull ZonedDateTime currentTime,
                                        Response responseFail) throws EIDASSAMLEngineException {

        ZonedDateTime notOnOrAfter = currentTime;

        notOnOrAfter = notOnOrAfter.plusSeconds(coreProperties.getTimeNotOnOrAfter());

        Assertion assertion =
                AssertionUtil.generateResponseAssertion(true, ipAddress, request, response.getSubject(), response.getSubjectNameIdFormat(), responseFail.getIssuer(),
                        ImmutableAttributeMap.of(), notOnOrAfter,
                        coreProperties.getFormatEntity(), coreProperties.getResponder(),
                        formatDescriptor, coreProperties.isOneTimeUse(), currentTime);
        addResponseAuthnContextClassRef(response, assertion);
        responseFail.getAssertions().add(assertion);
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
     * @param eidasRequestToBeSent the eidas request data
     * @param serviceIssuer        the issuer of a request
     * @param coreProperties       the saml engine's core properties
     * @param currentTime          the current time
     * @return org.opensaml.saml.saml2.core.AuthnRequest;
     * @throws EIDASSAMLEngineException if the request could not be marshalled
     */
    @Nonnull
    @Override
    public AuthnRequest marshallRequest(@Nonnull IEidasAuthenticationRequest eidasRequestToBeSent,
                                        @Nonnull String serviceIssuer,
                                        @Nonnull SamlEngineCoreProperties coreProperties,
                                        @Nonnull final ZonedDateTime currentTime)
            throws EIDASSAMLEngineException {

        AuthnRequest samlRequest =
                BuilderFactoryUtil.generateAuthnRequest(eidasRequestToBeSent.getId(), SAMLVersion.VERSION_20,
                        currentTime);

        // Set name spaces.
        registerRequestNamespace(samlRequest);

        // Add parameter Mandatory
        samlRequest.setForceAuthn(Boolean.TRUE);

        // Add parameter Mandatory
        samlRequest.setIsPassive(Boolean.FALSE);

        samlRequest.setAssertionConsumerServiceURL(eidasRequestToBeSent.getAssertionConsumerServiceURL());

        samlRequest.setProviderName(eidasRequestToBeSent.getProviderName());

        // Add protocol binding
        samlRequest.setProtocolBinding(getProtocolBinding(eidasRequestToBeSent, coreProperties));

        // Add parameter optional
        // Destination is mandatory
        // The application must to know the destination
        if (StringUtils.isNotBlank(eidasRequestToBeSent.getDestination())) {
            samlRequest.setDestination(eidasRequestToBeSent.getDestination());
        }

        // Consent is optional. Set from SAMLEngine.xml - consent.
        samlRequest.setConsent(coreProperties.getConsentAuthnRequest());

        Issuer issuer = BuilderFactoryUtil.generateIssuer();
        issuer.setValue(eidasRequestToBeSent.getIssuer());

        // Optional
        String formatEntity = coreProperties.getFormatEntity();
        if (StringUtils.isNotBlank(formatEntity)) {
            issuer.setFormat(formatEntity);
        }

        samlRequest.setIssuer(issuer);

        RequestedAuthnContext requestedAuthnContext = buildRequestedAuthnContext(eidasRequestToBeSent);
        samlRequest.setRequestedAuthnContext(requestedAuthnContext);

        // Generate format extensions.
        Extensions formatExtensions = generateExtensions(eidasRequestToBeSent);
        // add the extensions to the SAMLAuthnRequest
        samlRequest.setExtensions(formatExtensions);

        // optional name id policy
        if (StringUtils.isNotBlank(eidasRequestToBeSent.getNameIdFormat())) {
            addNameIDPolicy(samlRequest, eidasRequestToBeSent.getNameIdFormat());
        }

        String requesterIdValue = eidasRequestToBeSent.getRequesterId();
        if (metadataFetcher != null && getMetadataParameters(serviceIssuer).isSendRequesterId(requesterIdValue)) {
            Scoping scoping = buildScopingWithRequesterID(requesterIdValue);
            samlRequest.setScoping(scoping);
        }

        return samlRequest;
    }

    private Scoping buildScopingWithRequesterID(String requesterIdValue) throws EIDASSAMLEngineException {
        final RequesterID requesterId = buildRequesterID(requesterIdValue);

        final Scoping scoping = buildScoping();
        scoping.getRequesterIDs().add(requesterId);

        return scoping;
    }

    private Scoping buildScoping() throws EIDASSAMLEngineException {
        final Scoping scoping = (Scoping) BuilderFactoryUtil.buildXmlObject(Scoping.DEFAULT_ELEMENT_NAME);
        return scoping;
    }

    private RequesterID buildRequesterID(String requesterIdValue) throws EIDASSAMLEngineException {
        final RequesterID requesterId = buildRequesterID();
        requesterId.setURI(requesterIdValue);
        return requesterId;
    }

    private RequesterID buildRequesterID() throws EIDASSAMLEngineException {
        final RequesterID requesterID = (RequesterID) BuilderFactoryUtil.buildXmlObject(RequesterID.DEFAULT_ELEMENT_NAME);
        return requesterID;
    }

    /**
     * @param request the request
     * @param response the authentication response from the IdP
     * @param ipAddress the IP address
     * @param coreProperties the saml engine core properties
     * @param currentTime the current time
     * @return the authentication response
     * @throws EIDASSAMLEngineException in case of errors
     */
    @Override
    @Nonnull
    public Response marshallResponse(@Nonnull IAuthenticationRequest request,
                                     @Nonnull IAuthenticationResponse response,
                                     @Nullable String ipAddress,
                                     @Nonnull SamlEngineCoreProperties coreProperties,
                                     @Nonnull final ZonedDateTime currentTime) throws EIDASSAMLEngineException {
        LOG.trace("marshallResponse");

        // At this point the assertion consumer service URL is mandatory (and must have been replaced by the value from the metadata if needed)
        if (StringUtils.isBlank(request.getAssertionConsumerServiceURL())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Request AssertionConsumerServiceURL must not be blank.");
        }

        // Mandatory SAML
        LOG.trace("Generate StatusCode");
        StatusCode statusCode = BuilderFactoryUtil.generateStatusCode(StatusCode.SUCCESS);

        LOG.trace("Generate Status");
        Status status = BuilderFactoryUtil.generateStatus(statusCode);

        LOG.trace("Generate StatusMessage");
        StatusMessage statusMessage = BuilderFactoryUtil.generateStatusMessage(StatusCode.SUCCESS);

        status.setStatusMessage(statusMessage);

        LOG.trace("Generate Response");

        Response samlResponse =
                newResponse(status, request.getAssertionConsumerServiceURL(), request.getId(), coreProperties, currentTime);

        String consent = SAMLEngineUtils.getConsentValue(response.getConsent());
        samlResponse.setConsent(consent);

        if (StringUtils.isNotBlank(response.getIssuer()) && null != samlResponse.getIssuer()) {
            samlResponse.getIssuer().setValue(SAMLEngineUtils.getValidIssuerValue(response.getIssuer()));
        }

        ImmutableAttributeMap attributes = response.getAttributes();

        ZonedDateTime notOnOrAfter = currentTime.plusSeconds(coreProperties.getTimeNotOnOrAfter().intValue());

        boolean shouldProvideIpAddress = Boolean.parseBoolean(coreProperties
                .getProperty(SamlEngineCoreProperties.ENABLE_ADDRESS_ATTRIBUTE_SUBJECT_CONFIRMATION_DATA));
        Assertion assertion =
                AssertionUtil.generateResponseAssertion(false, shouldProvideIpAddress ? ipAddress : null,
                        request, response.getSubject(), response.getSubjectNameIdFormat(),
                        samlResponse.getIssuer(),
                        attributes, notOnOrAfter,
                        coreProperties.getFormatEntity(), coreProperties.getResponder(),
                        formatDescriptor, coreProperties.isOneTimeUse(), currentTime);

        AttributeStatement attrStatement = generateResponseAttributeStatement(attributes);

        assertion.getAttributeStatements().add(attrStatement);

        addResponseAuthnContextClassRef(response, assertion);

        samlResponse.getAssertions().add(assertion);

        validateMarshalledResponse((AuthenticationResponse)response);

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
                                 @Nonnull final ZonedDateTime currentTime) throws EIDASSAMLEngineException {
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

        return response;
    }

    protected void registerRequestNamespace(@Nonnull XMLObject xmlObject) {
        xmlObject.getNamespaceManager().registerNamespaceDeclaration(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlObject.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlObject.getNamespaceManager().registerNamespaceDeclaration(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));
        xmlObject.getNamespaceManager().registerNamespaceDeclaration(new Namespace(SAMLCore.EIDAS10_SAML_NS.getValue(), SAMLCore.EIDAS10_SAML_PREFIX.getValue()));
    }

    /**
     * Register the namespace on the response SAML xml token
     *
     * @param xmlObject the response SAML xml token
     */
    public void registerResponseNamespace(@Nonnull XMLObject xmlObject) {
        LOG.trace("Set namespaces.");
        xmlObject.getNamespaceManager()
                .registerNamespaceDeclaration(new Namespace(XMLConstants.XSI_NS, XMLConstants.XSI_PREFIX));
        xmlObject.getNamespaceManager()
                .registerNamespaceDeclaration(new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX));
        xmlObject.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds"));
        xmlObject.getNamespaceManager()
                .registerNamespaceDeclaration(new Namespace(SAMLConstants.SAML20P_NS, SAMLConstants.SAML20P_PREFIX));

        xmlObject.getNamespaceManager()
                .registerNamespaceDeclaration(new Namespace(NaturalPersonSpec.Namespace.URI,
                        NaturalPersonSpec.Namespace.PREFIX));

        xmlObject.getNamespaceManager()
                .registerNamespaceDeclaration(new Namespace(LegalPersonSpec.Namespace.URI,
                        LegalPersonSpec.Namespace.PREFIX));
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
        List<LevelOfAssurance> loaList = extractLevelsOfAssurance(samlRequest);

        ImmutableAttributeMap.Builder attributeMapBuilder = new ImmutableAttributeMap.Builder();
        for (RequestedAttribute requestedAttribute : reqAttrs) {
            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionNullable(requestedAttribute.getName());
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
                Set<eu.eidas.auth.commons.attribute.AttributeValue<?>> set = new LinkedHashSet<>();
                for (final String value : stringValues) {
                    eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue;
                    try {
                        attributeValue = attributeValueMarshaller.unmarshal(value, false);
                        set.add(attributeValue);
                    } catch (AttributeValueMarshallingException e) {
                        LOG.error("Illegal attribute value: " + e, e);
                        throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                                "Illegal attribute value: ", e);
                    }
                }
                attributeMapBuilder.put(attributeDefinition, (Set) set);
                checkRepresentativeAttributesPresent(attributeDefinition);
                checkRequiredAttributeCompiles(attributeDefinition, requestedAttribute);
            } else {
                LOG.info(AbstractProtocolEngine.SAML_EXCHANGE,
                        "BUSINESS EXCEPTION : Attribute name: {} was not found. It will be removed from the request object",
                        requestedAttribute.getName());
            }
        }

        EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder();
        builder.serviceProviderCountryCode(originCountryCode);
        builder.assertionConsumerServiceURL(samlRequest.getAssertionConsumerServiceURL());
        builder.binding(SAMLEngineUtils.getBindingMethod(samlRequest.getProtocolBinding()));
        builder.citizenCountryCode(citizenCountryCode);
        builder.destination(samlRequest.getDestination());
        builder.id(samlRequest.getID());
        builder.issuer(samlRequest.getIssuer().getValue());
        builder.levelsOfAssurance(loaList);
        builder.nameIdFormat(null == samlRequest.getNameIDPolicy() ? null : samlRequest.getNameIDPolicy().getFormat());
        builder.providerName(samlRequest.getProviderName());
        builder.requestedAttributes(attributeMapBuilder.build());
        builder.requesterId(extractRequesterId(samlRequest));
        // eIDAS only:
        builder.levelOfAssuranceComparison(LevelOfAssuranceComparison.MINIMUM.stringValue());
        builder.spType(getNullableSPTypeFromExtension(extensions));

        EidasAuthenticationRequest request;
        try {
            request = builder.build();
        } catch (IllegalArgumentException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.ILLEGAL_ARGUMENTS_IN_BUILDER,
                    "Failed to construct a valid eIDAS Request", e);
        }

        validateReceivedRequest(request);

        return request;
    }

    private void checkRepresentativeAttributesPresent(AttributeDefinition<?> attributeDefinition) throws EIDASSAMLEngineException {
        if (isRepresentativeAttribute(attributeDefinition)) {
            throw new EIDASSAMLEngineException(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES,
                    "Cannot find any Representative attribute");
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("squid:S2583")
    public IAuthenticationResponse unmarshallResponse(@Nonnull Response response,
                                                      boolean verifyBearerIpAddress,
                                                      @Nullable String userIpAddress,
                                                      long beforeSkewTimeInMillis,
                                                      long afterSkewTimeInMillis,
                                                      @Nonnull ZonedDateTime now,
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
            ZonedDateTime notOnOrAfter = ZonedDateTime.ofInstant(assertion.getConditions().getNotOnOrAfter(), ZoneId.systemDefault());
            builder.notOnOrAfter(notOnOrAfter);

            LOG.trace("Set notBefore.");
            ZonedDateTime notBefore = ZonedDateTime.ofInstant(assertion.getConditions().getNotBefore(), ZoneId.systemDefault());
            builder.notBefore(notBefore);

            builder.audienceRestriction((assertion.getConditions().getAudienceRestrictions().get(0)).getAudiences()
                                                .get(0)
                                                .getURI());
            if (!assertion.getAuthnStatements().isEmpty()
                    && assertion.getAuthnStatements().get(0).getAuthnContext() != null &&
                    assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef() != null) {
                builder.levelOfAssurance(assertion.getAuthnStatements()
                                                 .get(0)
                                                 .getAuthnContext()
                                                 .getAuthnContextClassRef()
                                                 .getURI());
            }
            LOG.trace("Set ipAddress.");
            String ipAddress = ResponseUtil.extractSubjectConfirmationIPAddress(assertion);
            builder.ipAddress(ipAddress);

            LOG.trace("Set Subject.");
            String subject = ResponseUtil.extractSubject(assertion);
            builder.subject(subject);

            LOG.trace("Set SubjectNameIdFormat.");
            String subjectFormat = ResponseUtil.extractSubjectNameIdFormat(assertion);
            builder.subjectNameIdFormat(subjectFormat);
        }

        // Case no error.
        if (null != assertion && !ResponseUtil.isFailure(responseStatus)) {
            LOG.trace("Status Success. Set PersonalAttributeList.");
            builder.attributes(convertToAttributeMap(assertion));
        } else {
            LOG.trace("Status Fail.");
        }
        LOG.trace("Return result.");
        AuthenticationResponse authenticationResponse = builder.build();
        validateReceivedResponse(authenticationResponse);

        return authenticationResponse;
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

        LevelOfAssuranceRequestValidator.validate(request);
    }

    private void validateReceivedResponse(AuthenticationResponse authenticationResponse) throws EIDASSAMLEngineException {
        LevelOfAssuranceResponseValidator.validate(authenticationResponse);
    }

    private void validateMarshalledResponse(AuthenticationResponse authenticationResponse)throws EIDASSAMLEngineException {
        LevelOfAssuranceResponseValidator.validate(authenticationResponse);
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

        new LevelOfAssuranceRequestValidator().validate(request);

        String issuer = getValidIssuerValue(request, coreProperties);

        String bindingMethod = SAMLEngineUtils.getBindingMethod(getProtocolBinding(request, coreProperties));

        // Always generate a new ID whatever the incoming input
        String id = SAMLEngineUtils.generateNCName();

        EidasAuthenticationRequest.Builder updatedRequestBuilder =
                EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) request)
                        .id(id)
                        .issuer(issuer)
                        .binding(bindingMethod)
                        .destination(request.getDestination());

        if (serviceIssuerMetadataUrl != null) { // TODO pletipi this condition is stupid, however too many test abuse it
            final EidasMetadataRoleParametersI proxyRoleDescriptor = getMetadataParameters(serviceIssuerMetadataUrl).getIDPRoleDescriptor();
            final Set<String> supportedAttributes = proxyRoleDescriptor.getSupportedAttributes();
            if (CollectionUtils.isNotEmpty(supportedAttributes)) {
                ImmutableAttributeMap filteredAttributes = filterSupportedAttributeNames(
                        request.getRequestedAttributes(),
                        supportedAttributes,
                        request.getIssuer(),
                        serviceIssuerMetadataUrl
                );

                updatedRequestBuilder.requestedAttributes(filteredAttributes);
            }
        }

        IAuthenticationRequest updatedRequest;

        try {
            updatedRequest = updatedRequestBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.ILLEGAL_ARGUMENTS_IN_BUILDER, "Failed to build SAML Request", e);
        }

        if (updatedRequest.getRequestedAttributes().isEmpty()) {
            String additionalInformation = "No requested attribute (request issuer: " + request.getIssuer() + " - serviceIssuer: "
                    + serviceIssuerMetadataUrl + ")";
            LOG.error(SAML_EXCHANGE, additionalInformation);
            throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL,
                    "No requested attribute (request issuer: " + request.getIssuer() + " - serviceIssuer: ");
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
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "Error Status Code is null or empty.");
        }

        if (StringUtils.isBlank(request.getAssertionConsumerServiceURL())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "assertionConsumerServiceURL is null or empty.");
        }

        if (StringUtils.isBlank(request.getId())) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "request ID is null or empty.");
        }
    }
}
