package eu.eidas.auth.engine.xml.opensaml;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.common.impl.ExtensionsBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.SubjectLocality;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Open SAML {@link org.opensaml.xml.XMLObjectBuilderFactory} utility class.
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
     */
    public static XMLObject buildXmlObject(QName qname) throws EIDASSAMLEngineException {
        XMLObjectBuilder builder = Configuration.getBuilderFactory().getBuilder(qname);
        if (builder == null) {
            throw new EIDASSAMLEngineException("Unable to instantiate BuilderFactory from qname " + qname);
        }
        return builder.buildObject(qname);
    }

    public static <T> T buildXmlObject(Class<T> clazz) throws NoSuchFieldException, IllegalAccessException {
        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

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
        return Configuration.getBuilderFactory().getBuilder(qname1).buildObject(qname, qname1);
    }

    /**
     * Generate assertion.
     *
     * @param version the version
     * @param identifier the identifier
     * @param issueInstant the issue instant
     * @param issuer the issuer
     * @return the assertion
     */
    public static Assertion generateAssertion(SAMLVersion version,
                                              String identifier,
                                              DateTime issueInstant,
                                              Issuer issuer) {
        AssertionBuilder assertionBuilder = new AssertionBuilder();
        Assertion assertion = assertionBuilder.buildObject();
        assertion.setVersion(version);
        assertion.setID(identifier);
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
     * @param issueInstant the issue instant
     * @return the authentication request
     */
    public static AuthnRequest generateAuthnRequest(String identifier, SAMLVersion version, DateTime issueInstant)
            throws EIDASSAMLEngineException {
        LOG.debug("Generate basic authentication request.");
        AuthnRequest authnRequest = (AuthnRequest) buildXmlObject(AuthnRequest.DEFAULT_ELEMENT_NAME);

        authnRequest.setID(identifier);
        authnRequest.setVersion(version);
        authnRequest.setIssueInstant(issueInstant);
        return authnRequest;
    }

    /**
     * Generate authentication statement.
     *
     * @param authnInstant the authentication instant
     * @param authnContext the authentication context
     * @return the authentication statement
     */
    public static AuthnStatement generateAuthnStatement(DateTime authnInstant, AuthnContext authnContext)
            throws EIDASSAMLEngineException {
        // <saml:AuthnStatement>
        AuthnStatement authnStatement = (AuthnStatement) buildXmlObject(AuthnStatement.DEFAULT_ELEMENT_NAME);

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
     * Generate metadata extension.
     *
     * @return the extensions
     */
    public static Extensions generateMetadataExtension() {
        ExtensionsBuilder extensionsBuilder = new ExtensionsBuilder();
        return extensionsBuilder.buildObject(SAMLConstants.SAML20MD_NS, "Extensions", "md");
    }

    /**
     * Generate issuer.
     *
     * @return the issuer
     */
    public static Issuer generateIssuer() throws EIDASSAMLEngineException {
        return (Issuer) buildXmlObject(Issuer.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate key info.
     *
     * @return the key info
     */
    public static KeyInfo generateKeyInfo() throws EIDASSAMLEngineException {
        return (KeyInfo) buildXmlObject(KeyInfo.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate name id.
     *
     * @return the name id
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
        NameID nameId = (NameID) Configuration.getBuilderFactory()
                .getBuilder(NameID.DEFAULT_ELEMENT_NAME)
                .buildObject(NameID.DEFAULT_ELEMENT_NAME);

        // optional
        nameId.setNameQualifier(nameQualifier);

        // optional
        nameId.setFormat(format);

        // optional
        nameId.setSPNameQualifier(spNameQualifier);

        return nameId;
    }

    /**
     * Generate response.
     *
     * @param identifier the identifier
     * @param issueInstant the issue instant
     * @param status the status
     * @return the response
     */
    public static Response generateResponse(String identifier, DateTime issueInstant, Status status)
            throws EIDASSAMLEngineException {
        Response response = (Response) buildXmlObject(Response.DEFAULT_ELEMENT_NAME);
        response.setID(identifier);
        response.setIssueInstant(issueInstant);
        response.setStatus(status);
        return response;
    }

    /**
     * Generate status.
     *
     * @param statusCode the status code
     * @return the status
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
        final SubjectConfirmation subjectConf = (SubjectConfirmation) Configuration.getBuilderFactory()
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
     */
    public static SubjectConfirmationData generateSubjectConfirmationData(DateTime notOnOrAfter,
                                                                          String recipient,
                                                                          String inResponseTo)
            throws EIDASSAMLEngineException {
        final SubjectConfirmationData subjectConfData =
                (SubjectConfirmationData) buildXmlObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        subjectConfData.setNotOnOrAfter(notOnOrAfter);
        subjectConfData.setRecipient(recipient);
        subjectConfData.setInResponseTo(inResponseTo);
        return subjectConfData;
    }

    /**
     * Generate subject locality.
     *
     * @param address the address
     * @return the subject locality
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
