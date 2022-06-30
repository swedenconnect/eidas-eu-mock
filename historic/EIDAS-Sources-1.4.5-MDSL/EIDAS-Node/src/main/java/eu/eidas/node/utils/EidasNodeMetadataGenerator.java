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

package eu.eidas.node.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import eu.eidas.auth.engine.metadata.*;
import org.apache.commons.lang.StringUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.dom.EncryptionKey;
import eu.eidas.auth.engine.configuration.dom.SignatureKey;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import static eu.eidas.node.Constants.EXPRESSION_LANGUAGE_PREFIX;

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
    private String samlConnectorIDP;
    //Connector as SP
    private String samlConnectorSP;
    //ProxyServive as Idp
    private String samlServiceIDP;
    //ProxyService as SP
    private String samlServiceSP;

    private String connectorMetadataUrl;
    private String serviceMetadataUrl;

    private String assertionUrl;
    private Properties nodeProps;
    private long validityDuration;

    private String  singleSignOnServiceRedirectLocation;
    private String  singleSignOnServicePostLocation;

    private static final String INVALID_METADATA="invalid metadata";

    private ProtocolEngineFactory nodeProtocolEngineFactory;

    public String getSamlConnectorIDP() {
        return samlConnectorIDP;
    }

    public void setSamlConnectorIDP(String samlConnectorIDP) {
        this.samlConnectorIDP = samlConnectorIDP;
    }

    public String getSamlConnectorSP() {
        return samlConnectorSP;
    }

    public void setSamlConnectorSP(String samlConnectorSP) {
        this.samlConnectorSP = samlConnectorSP;
    }

    public String getSamlServiceIDP() {
        return samlServiceIDP;
    }

    public void setSamlServiceIDP(String samlServiceIDP) {
        this.samlServiceIDP = samlServiceIDP;
    }

    public String getSamlServiceSP() {
        return samlServiceSP;
    }

    public void setSamlServiceSP(String samlServiceSP) {
        this.samlServiceSP = samlServiceSP;
    }

    public String getConnectorMetadataUrl() {
        return connectorMetadataUrl;
    }

    public void setConnectorMetadataUrl(String connectorMetadataUrl) {
        this.connectorMetadataUrl = connectorMetadataUrl;
    }

    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }

    public ProtocolEngineFactory getNodeProtocolEngineFactory() {
        return nodeProtocolEngineFactory;
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }

    public String generateConnectorMetadata(){
        ContactData technicalContact = MetadataUtil.createConnectorTechnicalContact(nodeProps);
        ContactData supportContact = MetadataUtil.createConnectorSupportContact(nodeProps);
        OrganizationData organization = MetadataUtil.createConnectorOrganization(nodeProps);
        return generateMetadata(samlConnectorSP, samlConnectorIDP, connectorMetadataUrl, technicalContact, supportContact, organization, null);
    }

    public String generateServiceMetadata(){
        String loA=null;
        if(getNodeProps()!=null){
            loA=getNodeProps().getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString());
        }
        ContactData technicalContact = MetadataUtil.createServiceTechnicalContact(nodeProps);
        ContactData supportContact = MetadataUtil.createServiceSupportContact(nodeProps);
        OrganizationData organization = MetadataUtil.createServiceOrganization(nodeProps);
        return generateMetadata(samlServiceSP, samlServiceIDP, serviceMetadataUrl, technicalContact, supportContact, organization, loA);
    }

    private String generateMetadata(String spEngineName, String idpEngineName, String url, ContactData technicalContact, ContactData supportContact, OrganizationData organization, String loA){
        String metadata=INVALID_METADATA;
        ProtocolEngineI spEngine=null;
        ProtocolEngineI idpEngine=null;
        if(url!=null && !url.isEmpty()) {
            try {
                EidasMetadata.Generator generator = EidasMetadata.generator();
                MetadataConfigParams.Builder mcp = MetadataConfigParams.builder();
                if (!StringUtils.isEmpty(spEngineName)) {
                    spEngine = getNodeProtocolEngineFactory().getProtocolEngine(spEngineName);
                    mcp.spType(nodeProps == null ? null : nodeProps.getProperty(EIDASValues.EIDAS_SPTYPE.toString()));
                }
                if (!StringUtils.isEmpty(idpEngineName)) {
                    idpEngine = getNodeProtocolEngineFactory().getProtocolEngine(idpEngineName);
                }
                mcp.idpEngine(idpEngine);
                mcp.spEngine(spEngine);
                mcp.entityID(url);
                mcp.assuranceLevel(loA);
                mcp.assertionConsumerUrl(assertionUrl);
                mcp.addProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
                mcp.addProtocolBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                addBindingLocation(idpEngine, spEngine, mcp, url);
                mcp.signingMethods(nodeProps == null ? null : nodeProps.getProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getKey()));
                mcp.digestMethods(nodeProps == null ? null : nodeProps.getProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getKey()));
                mcp.encryptionAlgorithms(nodeProps == null ? null : nodeProps.getProperty(EncryptionKey.ENCRYPTION_ALGORITHM_WHITE_LIST.getKey()));
                mcp.validityDuration(validityDuration);
                mcp.technicalContact(technicalContact);
                mcp.supportContact(supportContact);
                mcp.organization(organization);
                mcp.eidasProtocolVersion(nodeProps == null ? null : nodeProps.getProperty(EIDASValues.EIDAS_PROTOCOL_VERSION.toString()));
                mcp.eidasApplicationIdentifier(nodeProps == null ? null : nodeProps.getProperty(EIDASValues.EIDAS_APPLICATION_IDENTIFIER.toString()));
                generator.configParams(mcp.build());
                return generator.build().getMetadata();
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
     *  Puts binding and location metadata attributes related to single sign on service in the {@code metadataConfigParams} only for the case of Identity Provider metadata.
     *
     * @param idpEngine The idpEngine to be checked for not null
     *
     * @param spEngine The idpEngine to be checked for null
     *
     * @param metadataConfigParams The MetadataConfigParams were the binding and location will be put
     *
     * @param url The url where the metadata will be presented
     *
     * @throws EIDASSAMLEngineException
     */
    private void addBindingLocation(ProtocolEngineI idpEngine, ProtocolEngineI spEngine, MetadataConfigParams.Builder metadataConfigParams, String url) throws EIDASSAMLEngineException {
        //This check is necessary to add and validate of single sign on service metadata only for the case of Identity Provider metadata.
        //It is not necessary to add or validate single sign one service metadata for other metadata, e.g. as for Service Provider metadata
        if (idpEngine!=null && spEngine == null) {
            metadataConfigParams.addProtocolBindingLocation(SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    validateBindingLocation(SAMLConstants.SAML2_REDIRECT_BINDING_URI, singleSignOnServiceRedirectLocation, url));
            metadataConfigParams.addProtocolBindingLocation(SAMLConstants.SAML2_POST_BINDING_URI,
                    validateBindingLocation(SAMLConstants.SAML2_POST_BINDING_URI, singleSignOnServicePostLocation, url));
        }
    }

    private String validateBindingLocation(String binding, String location, String metadataUrl) throws EIDASSAMLEngineException {
        if (location == null) {
            String msg = String.format("BUSINESS EXCEPTION : Location is null for binding %1$s at %2$s", binding, metadataUrl);
            LOGGER.error(msg);
            throwSAMLEngineNoMetadataException();
        } else if (location.startsWith(EXPRESSION_LANGUAGE_PREFIX)) {
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

    public String getAssertionUrl() {
        return assertionUrl;
    }

    public void setAssertionUrl(String assertionUrl) {
        this.assertionUrl = assertionUrl;
    }

    public long getValidityDuration() {
        return validityDuration;
    }

    public void setValidityDuration(long validityDuration) {
        this.validityDuration = validityDuration;
    }

    public String getSingleSignOnServiceRedirectLocation() {
        return singleSignOnServiceRedirectLocation;
    }

    public void setSingleSignOnServiceRedirectLocation(String singleSignOnServiceRedirectLocation) {
        this.singleSignOnServiceRedirectLocation = singleSignOnServiceRedirectLocation;
    }

    public String getSingleSignOnServicePostLocation() {
        return singleSignOnServicePostLocation;
    }

    public void setSingleSignOnServicePostLocation(String singleSignOnServicePostLocation) {
        this.singleSignOnServicePostLocation = singleSignOnServicePostLocation;
    }

}
