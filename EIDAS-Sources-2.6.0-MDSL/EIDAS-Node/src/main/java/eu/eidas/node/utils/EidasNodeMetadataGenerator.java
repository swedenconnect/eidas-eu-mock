/*
 * Copyright (c) 2021 by European Commission
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
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataConfiguration;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.OrganizationData;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.Constants;
import eu.eidas.node.auth.AUNODEUtil;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * generator for Eidas metadata
 */
public class EidasNodeMetadataGenerator {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EidasNodeMetadataGenerator.class.getName());

    //saml engine names
    //Connector as Idp
    private String connectorEngine;
    //Connector as SP
    private String proxyServiceEngine;

    private String connectorMetadataUrl;
    private String proxyServiceMetadataUrl;

    private String assertionUrl;
    private Properties nodeProps;
    private AUNODEUtil nodeUtil;
    private long validityDuration;

    private static final String INVALID_METADATA = "invalid metadata";
    private String singleSignOnServiceRedirectLocation;
    private String singleSignOnServicePostLocation;

    private ProtocolEngineFactory nodeProtocolEngineFactory;

    public void setConnectorEngine(String connectorEngine) {
        this.connectorEngine = connectorEngine;
    }

    public void setProxyServiceEngine(String proxyServiceEngine) {
        this.proxyServiceEngine = proxyServiceEngine;
    }

    public void setConnectorMetadataUrl(String connectorMetadataUrl) {
        this.connectorMetadataUrl = connectorMetadataUrl;
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

    public String generateConnectorMetadata(ProtocolEngineI protocolEngine) {
        ContactData technicalContact = NodeMetadataUtil.createConnectorTechnicalContact(getNodeProps());
        ContactData supportContact = NodeMetadataUtil.createConnectorSupportContact(getNodeProps());
        OrganizationData organization = NodeMetadataUtil.createConnectorOrganizationData(getNodeProps());
        return generateMetadata(false, connectorMetadataUrl, technicalContact, supportContact, organization, null, (MetadataSignerI) protocolEngine.getSigner());
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
        return generateMetadata(true, proxyServiceMetadataUrl, technicalContact, supportContact, organization,
                loAs, (MetadataSignerI) protocolEngine.getSigner());
    }

    private String generateMetadata(boolean idpRole, String url, ContactData technicalContact,
                                    ContactData supportContact, OrganizationData organization, List<String> loAs, MetadataSignerI signer) {
        String metadata = INVALID_METADATA;

        if (url != null && !url.isEmpty()) {
            try {
                EidasMetadataParametersI eidasMetadataParametersI = MetadataConfiguration.newParametersInstance();
                EidasMetadataRoleParametersI eidasMetadataRoleParameters = MetadataConfiguration.newRoleParametersInstance();

                final ProtocolEngineI protocolEngine;
                if (idpRole) {
                    protocolEngine = setIDPMetadataRoleParams(eidasMetadataRoleParameters, url);
                    eidasMetadataParametersI.setRequesterIdFlag(isRequesterIdFlag());
                } else {
                    protocolEngine = setSPMetadataRoleParams(eidasMetadataParametersI, eidasMetadataRoleParameters);
                }

                ProtocolCipherI protocolCipher = protocolEngine.getCipher();
                String encryptionAlgorithmWhitelist = String.join(EIDASValues.SEMICOLON.toString(),
                        protocolCipher.getEncryptionConfiguration().getEncryptionAlgorithmWhitelist());
                eidasMetadataRoleParameters.setEncryptionAlgorithms(encryptionAlgorithmWhitelist);

                eidasMetadataParametersI.setEntityID(url);
                eidasMetadataParametersI.setAssuranceLevels(loAs);
                eidasMetadataParametersI.addRoleDescriptor(eidasMetadataRoleParameters);
                eidasMetadataParametersI.setNodeCountry(getNodeProps().getProperty(EIDASValues.EIDAS_NODE_COUNTRY.toString()));

                ProtocolSignerI protocolSigner = protocolEngine.getSigner();
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

                //TODO decouple here : EidasMetadata.Generator will be used in EIDAS-Metadata
                EidasMetadata.Generator generator = EidasMetadata.generator(eidasMetadataParametersI);
                EidasMetadata eidasMetadata = generator.generate(signer);
                metadata = eidasMetadata.getMetadata();

            } catch (EIDASMetadataException eidasSamlexc) {
                LOGGER.info("ERROR : Error creating Node metadata " + eidasSamlexc.getMessage());
                LOGGER.debug("ERROR : Error creating Node metadata ", eidasSamlexc);
                if (EidasErrorKey.isErrorCode(eidasSamlexc.getErrorCode())) {
                    EidasNodeErrorUtil.processSAMLEngineException(eidasSamlexc, LOGGER, EidasErrorKey.SAML_ENGINE_NO_METADATA);
                }
            } catch (EIDASSAMLEngineException eidasSamlexc) {
                LOGGER.info("ERROR : Error creating Node metadata " + eidasSamlexc.getMessage());
                LOGGER.debug("ERROR : Error creating Node metadata ", eidasSamlexc);
                if (EidasErrorKey.isErrorCode(eidasSamlexc.getErrorCode())) {
                    EidasNodeErrorUtil.processSAMLEngineException(eidasSamlexc, LOGGER, EidasErrorKey.SAML_ENGINE_NO_METADATA);
                }
            }
        }
        return metadata;
    }

    /**
     * Gets the SPType property from configuration
     *
     * @return String representation of spType
     */
    private String getSpTypeFromProperties() {
        return getNodeProps().getProperty(EIDASValues.EIDAS_SPTYPE.toString());
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

    private ProtocolEngineI setSPMetadataRoleParams(final EidasMetadataParametersI emp, final EidasMetadataRoleParametersI emrp) throws EIDASSAMLEngineException {
        emrp.setRole(MetadataRole.SP);

        //TODO these bindings should come from configuration
        emrp.setDefaultBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        emrp.addProtocolBindingLocation(SAMLConstants.SAML2_POST_BINDING_URI, assertionUrl);
        emrp.addProtocolBindingLocation(SAMLConstants.SAML2_REDIRECT_BINDING_URI, assertionUrl);

        emp.setSpType(getSpTypeFromProperties());
        final ProtocolEngineI protocolEngine = getNodeProtocolEngineFactory().getProtocolEngine(connectorEngine);
        final X509Certificate spEngineDecryptionCertificate = protocolEngine.getDecryptionCertificate();
        if (spEngineDecryptionCertificate != null) {
            emrp.setEncryptionCertificate(spEngineDecryptionCertificate);
        }
        emrp.setSigningCertificate(protocolEngine.getSigningCertificate());

        final Set<String> nameIdFormats = getNameIDFormatsSet(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS);
        emrp.setNameIDFormats(nameIdFormats);
        return protocolEngine;
    }

    private ProtocolEngineI setIDPMetadataRoleParams(final EidasMetadataRoleParametersI emrp, final String url) throws EIDASSAMLEngineException {
        emrp.setRole(MetadataRole.IDP);

        final ProtocolEngineI protocolEngine = getNodeProtocolEngineFactory().getProtocolEngine(proxyServiceEngine);
        final X509Certificate idpEngineDecryptionCertificate = protocolEngine.getDecryptionCertificate();
        if (idpEngineDecryptionCertificate != null) {
            emrp.setEncryptionCertificate(idpEngineDecryptionCertificate);
        }
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
        for (String nameIDFormat: optionalNameIdFormats) {
            nameIdFormatSet.add(nameIDFormat.trim());
        }

        return nameIdFormatSet;
    }

    private ProtocolEngineI getProxyServiceProtocolEngine() {
        return getNodeProtocolEngineFactory().getProtocolEngine(proxyServiceEngine);
    }

    private String validateBindingLocation(String binding, String location, String metadataUrl) throws EIDASSAMLEngineException {
        if (location == null) {
            String msg = String.format("BUSINESS EXCEPTION : Location is null for binding %1$s at %2$s", binding, metadataUrl);
            LOGGER.error(msg);
            throwSAMLEngineNoMetadataException();
        } else if (location.startsWith(Constants.EXPRESSION_LANGUAGE_PREFIX)) {
            String msg = String.format("BUSINESS EXCEPTION : Missing property %3$s for binding %1$s at %2$s", binding, metadataUrl, location);
            LOGGER.error(msg);
            throwSAMLEngineNoMetadataException();
        }

        return location;
    }

    private void throwSAMLEngineNoMetadataException() {
        final String exErrorCode = EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode());
        final String exErrorMessage = EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage());
        throw new EIDASServiceException(exErrorCode, exErrorMessage);
    }

    public Properties getNodeProps() {
        if (nodeProps != null) {
            return nodeProps;
        }
        return nodeUtil == null ? null : nodeUtil.getConfigs();
    }

    /**
     * Set properties to be use by this bean
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

    public void setAssertionUrl(String assertionUrl) {
        this.assertionUrl = assertionUrl;
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
}
