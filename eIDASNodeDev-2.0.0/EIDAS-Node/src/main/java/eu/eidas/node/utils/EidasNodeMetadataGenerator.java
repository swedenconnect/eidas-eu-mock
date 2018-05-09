/*
 * Copyright (c) 2017 by European Commission
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
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.dom.EncryptionKey;
import eu.eidas.auth.engine.configuration.dom.SignatureKey;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.Constants;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.Properties;

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
    private long validityDuration;

    private String  singleSignOnServiceRedirectLocation;
    private String  singleSignOnServicePostLocation;

    private static final String INVALID_METADATA="invalid metadata";

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

    public String generateConnectorMetadata(ProtocolEngineI protocolEngine){
        ContactData technicalContact = NodeMetadataUtil.createConnectorTechnicalContact(nodeProps);
        ContactData supportContact = NodeMetadataUtil.createConnectorSupportContact(nodeProps);
        OrganizationData organization = NodeMetadataUtil.createConnectorOrganizationData(nodeProps);
        return generateMetadata(false, connectorMetadataUrl, technicalContact, supportContact, organization, null, (MetadataSignerI) protocolEngine.getSigner());
    }

    public String generateProxyServiceMetadata(ProtocolEngineI protocolEngine){
        String loA=null;
        if(getNodeProps()!=null){
            loA=getNodeProps().getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString());
        }
        ContactData technicalContact = NodeMetadataUtil.createServiceTechnicalContact(nodeProps);
        ContactData supportContact = NodeMetadataUtil.createServiceSupportContact(nodeProps);
        OrganizationData organization = NodeMetadataUtil.createServiceOrganization(nodeProps);
        return generateMetadata(true, proxyServiceMetadataUrl, technicalContact, supportContact, organization, loA, (MetadataSignerI) protocolEngine.getSigner());
    }

    private String generateMetadata(boolean idpRole, String url, ContactData technicalContact,
                                    ContactData supportContact, OrganizationData organization, String loA, MetadataSignerI signer){
        String metadata=INVALID_METADATA;

        if(url!=null && !url.isEmpty()) {
            try {
                EidasMetadataParametersI emp = MetadataConfiguration.newParametersInstance();
                EidasMetadataRoleParametersI emrp = MetadataConfiguration.newRoleParametersInstance();

                final ProtocolEngineI protocolEngine;
                if (idpRole) {
                    protocolEngine = setIDPMetadataRoleParams(emrp, url);
                } else {
                    protocolEngine = setSPMetadataRoleParams(emp, emrp);
                }

                emrp.setEncryptionAlgorithms(nodeProps == null ? null : nodeProps.getProperty(EncryptionKey.ENCRYPTION_ALGORITHM_WHITE_LIST.getKey()));

                emp.setEntityID(url);
                emp.setAssuranceLevel(loA);
                emp.addRoleDescriptor(emrp);

                emp.setSigningMethods(nodeProps == null ? null : nodeProps.getProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getKey()));
                emp.setDigestMethods(nodeProps == null ? null : nodeProps.getProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getKey()));

                setValidityUntil(emp, protocolEngine);
                setContacts(emp, technicalContact, supportContact, organization);

                //TODO decouple here : EidasMetadata.Generator will be used in EIDAS-Metadata
                EidasMetadata.Generator generator = EidasMetadata.generator(emp);
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

        emp.setSpType(nodeProps == null ? null : nodeProps.getProperty(EIDASValues.EIDAS_SPTYPE.toString()));
        final ProtocolEngineI protocolEngine = getNodeProtocolEngineFactory().getProtocolEngine(connectorEngine);
        final X509Certificate spEngineDecryptionCertificate = protocolEngine.getDecryptionCertificate();
        if (spEngineDecryptionCertificate != null) {
            emrp.setEncryptionCertificate(spEngineDecryptionCertificate);
        }
        emrp.setSigningCertificate(protocolEngine.getSigningCertificate());
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

        final ImmutableSortedSet.Builder<String> supportedAttributes = new ImmutableSortedSet.Builder<>(Ordering.natural());
        for (AttributeDefinition<?> attributeDefinition : protocolEngine.getProtocolProcessor().getAllSupportedAttributes()) {
            supportedAttributes.add(attributeDefinition.getNameUri().toASCIIString());
        }
        emrp.setSupportedAttributes(supportedAttributes.build());
        return protocolEngine;
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
        return nodeProps;
    }

    public void setNodeProps(Properties nodeProps) {
        this.nodeProps = nodeProps;
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
