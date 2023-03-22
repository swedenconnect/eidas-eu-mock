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
package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.SubjectLocality;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.ExtensionsBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.time.Instant;

/**
 * Open SAML {@link XMLObjectBuilderFactory} utility class.
 *
 * @since 1.1
 */
public final class BuilderFactoryUtil {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BuilderFactoryUtil.class);

    /**
     * Creates the SAML object.
     *
     * @param qname the QName
     * @return the XML object
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static XMLObject buildXmlObject(QName qname) throws EIDASSAMLEngineException {
        XMLObjectBuilder builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname);
        if (builder == null) {
            throw new EIDASSAMLEngineException("Unable to instantiate BuilderFactory from qname " + qname);
        }
        return builder.buildObject(qname);
    }

    public static <T> T buildXmlObject(Class<T> clazz) throws NoSuchFieldException, IllegalAccessException {
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
        XMLObjectBuilder builder = builderFactory.getBuilder(defaultElementName);
        T object = (T) builder.buildObject(defaultElementName);

        return object;
    }

    /**
     * Creates the SAML object.
     *
     * @param qname the quality name
     * @param qname1 the qname1
     * @return the xML object
     */
    public static XMLObject buildXmlObject(QName qname, QName qname1) {
        return XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname1).buildObject(qname, qname1);
    }

    /**
     * Generate assertion.
     *
     * @param version the version
     * @param identifier the identifier
     * @param issueDateTime the issue instant
     * @param issuer the issuer
     * @return the assertion
     */
    public static Assertion generateAssertion(SAMLVersion version,
                                              String identifier,
                                              DateTime issueDateTime,
                                              Issuer issuer) {
        AssertionBuilder assertionBuilder = new AssertionBuilder();
        Assertion assertion = assertionBuilder.buildObject();
        assertion.setVersion(version);
        assertion.setID(identifier);
        Instant issueInstant = issueDateTime != null ? Instant.ofEpochMilli(issueDateTime.getMillis()) : null;
        assertion.setIssueInstant(issueInstant);

        // <saml:Issuer>
        assertion.setIssuer(issuer);
        return assertion;
    }

    /**
     * Method that generates an Authentication Request basing on the provided information.
     *
     * @param identifier the identifier
     * @param version the version
     * @param issueDateTime the issue instant
     * @return the authentication request
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static AuthnRequest generateAuthnRequest(String identifier, SAMLVersion version, DateTime issueDateTime)
            throws EIDASSAMLEngineException {
        LOG.debug("Generate basic authentication request.");
        AuthnRequest authnRequest = (AuthnRequest) buildXmlObject(AuthnRequest.DEFAULT_ELEMENT_NAME);

        authnRequest.setID(identifier);
        authnRequest.setVersion(version);
        Instant issueInstant = issueDateTime != null ? Instant.ofEpochMilli(issueDateTime.getMillis()) : null;
        authnRequest.setIssueInstant(issueInstant);
        return authnRequest;
    }

    /**
     * Generate authentication statement.
     *
     * @param authnDateTime the authentication instant
     * @param authnContext the authentication context
     * @return the authentication statement
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static AuthnStatement generateAuthnStatement(DateTime authnDateTime, AuthnContext authnContext)
            throws EIDASSAMLEngineException {
        // <saml:AuthnStatement>
        AuthnStatement authnStatement = (AuthnStatement) buildXmlObject(AuthnStatement.DEFAULT_ELEMENT_NAME);

        Instant authnInstant = authnDateTime != null ? Instant.ofEpochMilli(authnDateTime.getMillis()) : null;
        authnStatement.setAuthnInstant(authnInstant);
        authnStatement.setAuthnContext(authnContext);

        return authnStatement;
    }

    /**
     * Generate protocol extension.
     *
     * @return the extensions
     */
    public static Extensions generateExtension() {
        ExtensionsBuilder extensionsBuilder = new ExtensionsBuilder();
        return extensionsBuilder.buildObject(SAMLConstants.SAML20P_NS, "Extensions", "saml2p");
    }

    /**
     * Generate issuer.
     *
     * @return the issuer
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static Issuer generateIssuer() throws EIDASSAMLEngineException {
        return (Issuer) buildXmlObject(Issuer.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate key info.
     *
     * @return the key info
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static KeyInfo generateKeyInfo() throws EIDASSAMLEngineException {
        return (KeyInfo) buildXmlObject(KeyInfo.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate name id.
     *
     * @return the name id
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static NameID generateNameID() throws EIDASSAMLEngineException {
        return (NameID) buildXmlObject(NameID.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate name id.
     *
     * @param nameQualifier the name qualifier
     * @param format the format
     * @param spNameQualifier the sP name qualifier
     * @return the name id
     */
    public static NameID generateNameID(String nameQualifier, String format, String spNameQualifier) {
        // <saml:NameID>
        NameID nameId = (NameID) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(NameID.DEFAULT_ELEMENT_NAME)
                .buildObject(NameID.DEFAULT_ELEMENT_NAME);

        // optional
        if (!NameID.ENTITY.equals(format)) {
            nameId.setNameQualifier(nameQualifier);
            nameId.setSPNameQualifier(spNameQualifier);
        }

        // optional
        nameId.setFormat(format);

        return nameId;
    }

    /**
     * Generate response.
     *
     * @param identifier the identifier
     * @param issueDateTime the issue instant
     * @param status the status
     * @return the response
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static Response generateResponse(String identifier, DateTime issueDateTime, Status status)
            throws EIDASSAMLEngineException {
        Response response = (Response) buildXmlObject(Response.DEFAULT_ELEMENT_NAME);
        response.setID(identifier);
        Instant issueInstant = issueDateTime != null ? Instant.ofEpochMilli(issueDateTime.getMillis()) : null;
        response.setIssueInstant(issueInstant);
        response.setStatus(status);
        return response;
    }

    /**
     * Generate status.
     *
     * @param statusCode the status code
     * @return the status
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static Status generateStatus(StatusCode statusCode) throws EIDASSAMLEngineException {
        Status status = (Status) buildXmlObject(Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode(statusCode);
        return status;
    }

    /**
     * Generate status code.
     *
     * @param value the value
     * @return the status code
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static StatusCode generateStatusCode(String value) throws EIDASSAMLEngineException {
        StatusCode statusCode = (StatusCode) buildXmlObject(StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(value);
        return statusCode;
    }

    /**
     * Generate status message.
     *
     * @param message the message
     * @return the status message
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static StatusMessage generateStatusMessage(String message) throws EIDASSAMLEngineException {
        StatusMessage statusMessage = (StatusMessage) buildXmlObject(StatusMessage.DEFAULT_ELEMENT_NAME);
        statusMessage.setMessage(message);
        return statusMessage;
    }

    /**
     * Generate subject.
     *
     * @return the subject
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static Subject generateSubject() throws EIDASSAMLEngineException {
        return (Subject) buildXmlObject(Subject.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate subject confirmation.
     *
     * @param method the method
     * @param data the data
     * @return the subject confirmation
     */
    public static SubjectConfirmation generateSubjectConfirmation(String method, SubjectConfirmationData data) {
        final SubjectConfirmation subjectConf = (SubjectConfirmation) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME)
                .buildObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);

        subjectConf.setMethod(method);

        subjectConf.setSubjectConfirmationData(data);

        return subjectConf;
    }

    /**
     * Generate subject confirmation data.
     *
     * @param notOnOrAfter the not on or after
     * @param recipient the recipient
     * @param inResponseTo the in response to
     * @return the subject confirmation data
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static SubjectConfirmationData generateSubjectConfirmationData(DateTime notOnOrAfter,
                                                                          String recipient,
                                                                          String inResponseTo)
            throws EIDASSAMLEngineException {
        final SubjectConfirmationData subjectConfData =
                (SubjectConfirmationData) buildXmlObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        Instant notOnOrAfterInstant = notOnOrAfter != null ? Instant.ofEpochMilli(notOnOrAfter.getMillis()) : null;
        subjectConfData.setNotOnOrAfter(notOnOrAfterInstant);
        subjectConfData.setRecipient(recipient);
        subjectConfData.setInResponseTo(inResponseTo);
        return subjectConfData;
    }

    /**
     * Generate subject locality.
     *
     * @param address the address
     * @param dnsName the DNS name
     * @return the subject locality
     * @throws EIDASSAMLEngineException in case of errors
     */
    public static SubjectLocality generateSubjectLocality(String address, String dnsName) throws EIDASSAMLEngineException {
        final SubjectLocality subjectLocality = (SubjectLocality) buildXmlObject(SubjectLocality.DEFAULT_ELEMENT_NAME);
        if (StringUtils.isNotBlank(address)) {
            subjectLocality.setAddress(address);
        }
        if (StringUtils.isNotBlank(dnsName)) {
            subjectLocality.setDNSName(dnsName);
        }
        return subjectLocality;
    }

    private BuilderFactoryUtil() {
    }
}
