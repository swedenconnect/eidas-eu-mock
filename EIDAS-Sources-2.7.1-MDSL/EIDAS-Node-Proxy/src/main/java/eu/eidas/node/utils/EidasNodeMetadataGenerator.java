/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.node.utils;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.io.ReloadableProperties;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.eidas.spec.EidasEncryptionConstants;
import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.InterconnectionGraphData;
import eu.eidas.auth.engine.metadata.MetadataConfiguration;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.OrganizationData;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.Constants;
import eu.eidas.node.auth.AUNODEUtil;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.service.utils.ProxyServiceErrorUtil;
import eu.eidas.util.WhitelistUtil;
import org.apache.xml.security.algorithms.JCEMapper;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * generator for Eidas metadata
 */
public class EidasNodeMetadataGenerator {
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasNodeMetadataGenerator.class.getName());

    //saml engine name
    private String proxyServiceEngine;

    private String proxyServiceMetadataUrl;

    private Properties nodeProps;
    private AUNODEUtil nodeUtil;
    private long validityDuration;

    private static final String INVALID_METADATA = "invalid metadata";
    private String singleSignOnServiceRedirectLocation;
    private String singleSignOnServicePostLocation;

    private ProtocolEngineFactory nodeProtocolEngineFactory;

    private ReloadableProperties whitelistConfigProperties;

    public void setProxyServiceEngine(String proxyServiceEngine) {
        this.proxyServiceEngine = proxyServiceEngine;
    }

    public void setProxyServiceMetadataUrl(String proxyServiceMetadataUrl) {
        this.proxyServiceMetadataUrl = proxyServiceMetadataUrl;
    }

    public ProtocolEngineFactory getNodeProtocolEngineFactory() {
        return nodeProtocolEngineFactory;
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }

    public String generateProxyServiceMetadata(ProtocolEngineI protocolEngine) {
        List<String> loAs = null;
        if (getNodeProps() != null) {
            String serviceLoAProperty = getNodeProps().getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString());
            loAs = EidasStringUtil.getDistinctValues(serviceLoAProperty);
        }
        ContactData technicalContact = NodeMetadataUtil.createServiceTechnicalContact(getNodeProps());
        ContactData supportContact = NodeMetadataUtil.createServiceSupportContact(getNodeProps());
        OrganizationData organization = NodeMetadataUtil.createServiceOrganization(getNodeProps());
        try {
            return generateMetadata(proxyServiceMetadataUrl, technicalContact, supportContact, organization,
                    loAs, (MetadataSignerI) protocolEngine.getSigner());
        } catch (EidasNodeException e) {
            throw new ProxyServiceError(e.getErrorCode(), e.getErrorMessage(), e);
        }
    }

    private String generateMetadata(String url, ContactData technicalContact,
                                    ContactData supportContact, OrganizationData organization, List<String> loAs, MetadataSignerI signer) {
        String metadata = INVALID_METADATA;

        if (url != null && !url.isEmpty()) {
            try {
                EidasMetadataParametersI eidasMetadataParametersI = MetadataConfiguration.newParametersInstance();
                EidasMetadataRoleParametersI eidasMetadataRoleParameters = MetadataConfiguration.newRoleParametersInstance();

                final ProtocolEngineI protocolEngine = setIDPMetadataRoleParams(eidasMetadataRoleParameters, url);
                eidasMetadataParametersI.setRequesterIdFlag(isRequesterIdFlag());

                eidasMetadataParametersI.setEntityID(url);
                eidasMetadataParametersI.setAssuranceLevels(loAs);
                eidasMetadataParametersI.addRoleDescriptor(eidasMetadataRoleParameters);
                eidasMetadataParametersI.setNodeCountry(getNodeProps().getProperty(EIDASValues.EIDAS_NODE_COUNTRY.toString()));

                final ProtocolSignerI protocolSigner = protocolEngine.getSigner();
                String signatureAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(),
                        protocolSigner.getSignatureConfiguration().getSignatureAlgorithmWhitelist());
                eidasMetadataParametersI.setSigningMethods(signatureAlgorithmWhiteList);

                String digestMethodAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(),
                        protocolSigner.getSignatureConfiguration().getDigestMethodAlgorithmWhiteList());
                eidasMetadataParametersI.setDigestMethods(digestMethodAlgorithmWhiteList);
                eidasMetadataParametersI.setHideLoaType(Boolean.TRUE);
                setValidityUntil(eidasMetadataParametersI, protocolEngine);
                setContacts(eidasMetadataParametersI, technicalContact, supportContact, organization);

                eidasMetadataParametersI.setEidasProtocolVersion(getNodeProps().getProperty(EIDASValues.EIDAS_PROTOCOL_VERSION.toString()));
                eidasMetadataParametersI.setEidasApplicationIdentifier(getNodeProps().getProperty(EIDASValues.EIDAS_APPLICATION_IDENTIFIER.toString()));

                final boolean interconnectionGraphMetadataEnabled = Boolean.parseBoolean(getNodeProps().getProperty(EIDASValues.INTERCONNECTION_GRAPH_ENABLED.toString(), "false"));
                if (interconnectionGraphMetadataEnabled) {
                    eidasMetadataParametersI.setInterconnectionGraphData(createInterconnectionGraphMetadata(protocolSigner));
                } else {
                    eidasMetadataParametersI.setInterconnectionGraphData(null);
                }

                //TODO decouple here : EidasMetadata.Generator will be used in EIDAS-Metadata
                EidasMetadata.Generator generator = EidasMetadata.generator(eidasMetadataParametersI);
                EidasMetadata eidasMetadata = generator.generate(signer);
                metadata = eidasMetadata.getMetadata();

            } catch (EIDASMetadataException eidasSamlexc) {
                LOG.info("ERROR : Error creating Node metadata " + eidasSamlexc.getMessage());
                LOG.debug("ERROR : Error creating Node metadata ", eidasSamlexc);
                if (EidasErrorKey.isErrorCode(eidasSamlexc.getErrorCode())) {
                    ProxyServiceErrorUtil.processSAMLEngineException(eidasSamlexc, LOG, EidasErrorKey.SAML_ENGINE_NO_METADATA);
                }
            } catch (EIDASSAMLEngineException eidasSamlexc) {
                LOG.info("ERROR : Error creating Node metadata " + eidasSamlexc.getMessage());
                LOG.debug("ERROR : Error creating Node metadata ", eidasSamlexc);
                if (EidasErrorKey.isErrorCode(eidasSamlexc.getErrorCode())) {
                    ProxyServiceErrorUtil.processSAMLEngineException(eidasSamlexc, LOG, EidasErrorKey.SAML_ENGINE_NO_METADATA);
                }
            }
        }
        return metadata;
    }

    private boolean isRequesterIdFlag() {
        String requesterIdFlagPropertyValue = getNodeProps().getProperty(EIDASValues.REQUESTER_ID_FLAG.toString());
        return Boolean.parseBoolean(requesterIdFlagPropertyValue);
    }

    private void setContacts(EidasMetadataParametersI emp, ContactData technicalContact, ContactData supportContact, OrganizationData organization) {
        emp.setTechnicalContact(technicalContact);
        emp.setSupportContact(supportContact);
        emp.setOrganization(organization);
    }

    private void setValidityUntil(EidasMetadataParametersI emp, ProtocolEngineI protocolEngine) {
        DateTime expiryDate = protocolEngine.getClock().getCurrentTime();
        expiryDate = expiryDate.withFieldAdded(DurationFieldType.seconds(), (int) (validityDuration));
        emp.setValidUntil(expiryDate);
    }

    private ProtocolEngineI setIDPMetadataRoleParams(final EidasMetadataRoleParametersI emrp, final String url) throws EIDASSAMLEngineException {
        emrp.setRole(MetadataRole.IDP);

        final ProtocolEngineI protocolEngine = getNodeProtocolEngineFactory().getProtocolEngine(proxyServiceEngine);
        emrp.setSigningCertificate(protocolEngine.getSigningCertificate());

        final String locationRedirect = validateBindingLocation(SAMLConstants.SAML2_REDIRECT_BINDING_URI, singleSignOnServiceRedirectLocation, url);
        emrp.addProtocolBindingLocation(SAMLConstants.SAML2_REDIRECT_BINDING_URI, locationRedirect);

        final String locationPost = validateBindingLocation(SAMLConstants.SAML2_POST_BINDING_URI, singleSignOnServicePostLocation, url);
        emrp.addProtocolBindingLocation(SAMLConstants.SAML2_POST_BINDING_URI, locationPost);

        final ImmutableSortedSet<String> supportedAttributes = getSupportedAttributes();
        emrp.setSupportedAttributes(supportedAttributes);

        final Set<String> nameIdFormats = getNameIDFormatsSet(EidasParameterKeys.EIDAS_SERVICE_NAMEID_FORMATS);
        emrp.setNameIDFormats(nameIdFormats);
        return protocolEngine;
    }

    private ImmutableSortedSet<String> getSupportedAttributes() {
        final ImmutableSortedSet.Builder<String> supportedAttributes = new ImmutableSortedSet.Builder<>(Ordering.natural());

        ImmutableSortedSet<AttributeDefinition<?>> allSupportedAttributes = getProxyServiceProtocolEngine()
                .getProtocolProcessor().getAllSupportedAttributes();

        final Set<String> unsupportedAttributes = ((AUSERVICEUtil) nodeUtil).getUnsupportedAttributes();
        allSupportedAttributes.stream()
                .map(AttributeDefinition::getNameUri)
                .map(URI::toASCIIString)
                .filter((attributeUri) -> !unsupportedAttributes.contains(attributeUri))
                .forEachOrdered(supportedAttributes::add);

        return supportedAttributes.build();
    }

    private Set<String> getNameIDFormatsSet(EidasParameterKeys optionalNameIDFormatsKey) {
        Set<String> nameIdFormatSet = new HashSet<>();
        nameIdFormatSet.add(NameIDType.PERSISTENT);
        nameIdFormatSet.add(NameIDType.TRANSIENT);
        nameIdFormatSet.add(NameIDType.UNSPECIFIED);

        String optionalNameIdFormatsStringList = nodeUtil.getConfigs()
                .getProperty(optionalNameIDFormatsKey.getValue());
        List<String> optionalNameIdFormats = EidasStringUtil.getDistinctValues(optionalNameIdFormatsStringList);
        for (String nameIDFormat : optionalNameIdFormats) {
            nameIdFormatSet.add(nameIDFormat.trim());
        }

        return nameIdFormatSet;
    }

    @Nonnull
    private InterconnectionGraphData createInterconnectionGraphMetadata(@Nonnull ProtocolSignerI protocolSigner) {
        final boolean whitelistEnabled = WhitelistUtil.isUseWhitelist(this.whitelistConfigProperties);
        final Collection<String> whitelistUrlHashes = !whitelistEnabled ? Collections.emptyList() : WhitelistUtil.metadataWhitelistHashes(this.whitelistConfigProperties);
        final Collection<String> trustedCertificateIdentifiers = protocolSigner.getTrustedCredentialGraphIdentifiers();
        final Collection<String> cryptoSupportedEncryptionAlgorithms = EidasEncryptionConstants.DEFAULT_ENCRYPTION_ALGORITHM_WHITE_LIST.parallelStream()
                .filter(this::encryptionAlgorithmIsSupportedBySecurityProviders)
                .collect(Collectors.toSet());

        return InterconnectionGraphData.builder()
                .trustedCertificateIdentifiers(trustedCertificateIdentifiers)
                .metadataUrlHashes(whitelistUrlHashes)
                .supportedEncryptionAlgorithms(cryptoSupportedEncryptionAlgorithms)
                .whitelistEnabled(whitelistEnabled)
                .build();
    }

    private boolean encryptionAlgorithmIsSupportedBySecurityProviders(String uri) {
        final String jcaName = JCEMapper.translateURItoJCEID(uri);
        final int keyLengthFromURI = JCEMapper.getKeyLengthFromURI(uri);
        try {
            return Cipher.getMaxAllowedKeyLength(jcaName) >= keyLengthFromURI;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    private ProtocolEngineI getProxyServiceProtocolEngine() {
        return getNodeProtocolEngineFactory().getProtocolEngine(proxyServiceEngine);
    }

    private String validateBindingLocation(String binding, String location, String metadataUrl) throws EIDASSAMLEngineException {
        if (location == null) {
            String msg = String.format("BUSINESS EXCEPTION : Location is null for binding %1$s at %2$s", binding, metadataUrl);
            LOG.error(msg);
            throwSAMLEngineNoMetadataException();
        } else if (location.startsWith(Constants.EXPRESSION_LANGUAGE_PREFIX)) {
            String msg = String.format("BUSINESS EXCEPTION : Missing property %3$s for binding %1$s at %2$s", binding, metadataUrl, location);
            LOG.error(msg);
            throwSAMLEngineNoMetadataException();
        }

        return location;
    }

    private void throwSAMLEngineNoMetadataException() {
        throw new EIDASServiceException(
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()));
    }

    public Properties getNodeProps() {
        if (nodeProps != null) {
            return nodeProps;
        }
        return nodeUtil == null ? null : nodeUtil.getConfigs();
    }

    /**
     * Set properties to be use by this bean
     *
     * @param properties the properties
     * @deprecated use {@link #setNodeUtil(AUNODEUtil)} instead
     */
    @Deprecated
    public void setNodeProps(Properties properties) {
        this.nodeProps = properties;
    }

    public void setNodeUtil(AUNODEUtil nodeUtil) {
        this.nodeUtil = nodeUtil;
    }

    public void setValidityDuration(long validityDuration) {
        this.validityDuration = validityDuration;
    }

    public void setSingleSignOnServiceRedirectLocation(String singleSignOnServiceRedirectLocation) {
        this.singleSignOnServiceRedirectLocation = singleSignOnServiceRedirectLocation;
    }

    public void setSingleSignOnServicePostLocation(String singleSignOnServicePostLocation) {
        this.singleSignOnServicePostLocation = singleSignOnServicePostLocation;
    }

    public void setWhitelistConfigProperties(ReloadableProperties whitelistConfigProperties) {
        this.whitelistConfigProperties = whitelistConfigProperties;
    }
}
