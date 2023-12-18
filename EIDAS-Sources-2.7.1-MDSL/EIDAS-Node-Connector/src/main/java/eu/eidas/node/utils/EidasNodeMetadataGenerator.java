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

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.io.ReloadableProperties;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
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
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.connector.exceptions.ConnectorError;
import eu.eidas.node.connector.utils.ConnectorErrorUtil;
import eu.eidas.util.WhitelistUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
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

    //saml engine names
    private String connectorEngine;

    private String connectorMetadataUrl;
    private String assertionUrl;
    private Properties nodeProps;
    private AUCONNECTORUtil nodeUtil;
    private long validityDuration;

    private static final String INVALID_METADATA = "invalid metadata";

    private ProtocolEngineFactory nodeProtocolEngineFactory;

    private ReloadableProperties whitelistConfigProperties;

    public void setConnectorEngine(String connectorEngine) {
        this.connectorEngine = connectorEngine;
    }

    public void setConnectorMetadataUrl(String connectorMetadataUrl) {
        this.connectorMetadataUrl = connectorMetadataUrl;
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
        try {
            return generateMetadata(connectorMetadataUrl, technicalContact, supportContact, organization, null, (MetadataSignerI) protocolEngine.getSigner());
        } catch (EidasNodeException e) {
            throw new ConnectorError(e.getErrorCode(), e.getErrorMessage(), e);
        }
    }

    private String generateMetadata(String url, ContactData technicalContact,
                                    ContactData supportContact, OrganizationData organization, List<String> loAs, MetadataSignerI signer) {
        String metadata = INVALID_METADATA;

        if (url != null && !url.isEmpty()) {
            try {
                EidasMetadataParametersI eidasMetadataParametersI = MetadataConfiguration.newParametersInstance();
                EidasMetadataRoleParametersI eidasMetadataRoleParameters = MetadataConfiguration.newRoleParametersInstance();

                final ProtocolEngineI protocolEngine = setSPMetadataRoleParams(eidasMetadataParametersI, eidasMetadataRoleParameters);
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
                    ConnectorErrorUtil.processSAMLEngineException(eidasSamlexc, EidasErrorKey.SAML_ENGINE_NO_METADATA);
                }
            } catch (EIDASSAMLEngineException eidasSamlexc) {
                LOG.info("ERROR : Error creating Node metadata " + eidasSamlexc.getMessage());
                LOG.debug("ERROR : Error creating Node metadata ", eidasSamlexc);
                if (EidasErrorKey.isErrorCode(eidasSamlexc.getErrorCode())) {
                    ConnectorErrorUtil.processSAMLEngineException(eidasSamlexc, EidasErrorKey.SAML_ENGINE_NO_METADATA);
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
        final Collection<String> trustedCertificateIdentifiers = protocolSigner.getTrustedCredentialGraphIdentifiers();
        final Set<String> whitelistUrlHashes = new HashSet<>(WhitelistUtil.metadataWhitelistHashes(this.whitelistConfigProperties));

        final boolean whitelistEnabled = WhitelistUtil.isUseWhitelist(this.whitelistConfigProperties);

        Base64.Encoder encoder = Base64.getEncoder();

        final Collection<String> metadataUrlHashes = this.nodeUtil.loadConfigServiceMetadataURLs()
                .stream()
                .map(DigestUtils::sha256)
                .map(encoder::encodeToString)
                .filter(hash -> !whitelistEnabled || whitelistUrlHashes.contains(hash))
                .collect(Collectors.toSet());

        final InterconnectionGraphData data = InterconnectionGraphData.builder()
                .trustedCertificateIdentifiers(trustedCertificateIdentifiers)
                .metadataUrlHashes(metadataUrlHashes)
                .whitelistEnabled(whitelistEnabled)
                .build();

        return data;
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
     * @deprecated use {@link #setNodeUtil(AUCONNECTORUtil)} instead
     */
    @Deprecated
    public void setNodeProps(Properties properties) {
        this.nodeProps = properties;
    }

    public void setNodeUtil(AUCONNECTORUtil nodeUtil) {
        this.nodeUtil = nodeUtil;
    }

    public void setAssertionUrl(String assertionUrl) {
        this.assertionUrl = assertionUrl;
    }

    public void setValidityDuration(long validityDuration) {
        this.validityDuration = validityDuration;
    }

    public void setWhitelistConfigProperties(final ReloadableProperties whitelistConfigProperties) {
        this.whitelistConfigProperties = whitelistConfigProperties;
    }
}
