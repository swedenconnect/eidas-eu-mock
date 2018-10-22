package eu.eidas.auth.commons.xml.opensaml;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.util.Preconditions;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.*;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

/**
 * OpenSAML Helper.
 *
 * @since 1.1
 */
public final class OpenSamlHelper {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OpenSamlHelper.class);

    private static final ParserPool SECURED_PARSER_POOL;

    static {
        LOG.trace("OpenSamlHelper: Initialize OpenSAML");
        try {
            DefaultBootstrap.bootstrap();
            SECURED_PARSER_POOL = newBasicSecuredParserPool();
            Configuration.setParserPool(SECURED_PARSER_POOL);
        } catch (ConfigurationException ce) {
            LOG.error("Problem initializing the OpenSAML library: " + ce, ce);
            throw new IllegalStateException(ce);
        }
    }

    private OpenSamlHelper() {
    }

    @Nonnull
    public static ParserPool getSecuredParserPool() {
        ParserPool parserPool = Configuration.getParserPool();
        if (parserPool != SECURED_PARSER_POOL) {
            Configuration.setParserPool(SECURED_PARSER_POOL);
        }
        return SECURED_PARSER_POOL;
    }

    /**
     * Method that transform the received SAML object into a byte array representation.
     *
     * @param xmlObject the SAML token.
     * @return the byte[] of the SAML token.
     * @throws MarshallException when the OpenSAML object cannot be marshalled
     */
    @Nonnull
    public static byte[] marshall(@Nonnull XMLObject xmlObject) throws MarshallException {
        return marshallToBytes(xmlObject, true);
    }

    /**
     * Method that transform the received SAML object into a byte array representation.
     * <p>
     * The byte[] returned will include an xml declaration if omitXMLDeclaration is set to true.
     *
     * @param xmlObject the SAML token.
     * @param omitXMLDeclaration the omit xml declaration flag
     * @return the byte[] of the SAML token.
     * @throws MarshallException when the OpenSAML object cannot be marshalled
     */
    @Nonnull
    public static byte[] marshall(@Nonnull XMLObject xmlObject, final boolean omitXMLDeclaration)
            throws MarshallException {
        return marshallToBytes(xmlObject, omitXMLDeclaration);
    }

    /**
     * Implementing method that transforms the received SAML object into a byte array representation.
     * <p>
     * The byte[] returned will include an xml declaration if omitXMLDeclaration is set to true.
     *
     * @param xmlObject the SAML token.
     * @param omitXMLDeclaration the omit xml declaration flag
     * @return the byte[] of the SAML token with or without an XML declaration depending on the value of {code
     * omitXMLDeclaration}.
     * @throws MarshallException when the OpenSAML object cannot be marshalled
     */
    private static byte[] marshallToBytes(@Nonnull XMLObject xmlObject, final boolean omitXMLDeclaration)
            throws MarshallException {
        Element element = marshallToDom(xmlObject);
        try {
            // Obtain a byte array representation of the marshalled SAML object
            return DocumentBuilderFactoryUtil.marshall(element.getOwnerDocument(), omitXMLDeclaration);
        } catch (Exception ex) {
            LOG.error("Marshall exception for " + xmlObject + ": " + ex, ex);
            throw new MarshallException(ex);
        }
    }

    /**
     * Implementing method that transforms the received SAML object into a DOM Element.
     *
     * @param xmlObject the SAML token.
     * @return the byte[] of the SAML token with or without an XML declaration depending on the value of {code
     * omitXMLDeclaration}.
     * @throws MarshallException when the OpenSAML object cannot be marshalled
     */
    public static Element marshallToDom(@Nonnull XMLObject xmlObject) throws MarshallException {
        Preconditions.checkNotNull(xmlObject, "xmlObject");

        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        if (null == marshallerFactory) {
            LOG.error("No MarshallerFactory for " + xmlObject);
            throw new MarshallException("No MarshallerFactory for " + xmlObject);
        }

        Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
        if (null == marshaller) {
            LOG.error("No marshaller for " + xmlObject);
            throw new MarshallException("No Marshaller for " + xmlObject);
        }
        try {
            Document doc = DocumentBuilderFactoryUtil.newDocument();

            return marshaller.marshall(xmlObject, doc);

        } catch (Exception ex) {
            LOG.error("Marshall exception for " + xmlObject + ": " + ex, ex);
            throw new MarshallException(ex);
        }
    }

    @Nonnull
    private static BasicParserPool newBasicSecuredParserPool() {
        // Get parser pool manager
        BasicParserPool ppMgr = new BasicParserPool();
        // Note: this is necessary due to an unresolved Xerces deferred DOM issue/bug
        ppMgr.setBuilderFeatures(DocumentBuilderFactoryUtil.getSecureDocumentBuilderFeatures());
        ppMgr.setNamespaceAware(true);
        ppMgr.setIgnoreComments(true);
        ppMgr.setExpandEntityReferences(false);
        ppMgr.setXincludeAware(false);
        return ppMgr;
    }

    @Nonnull
    public static String toString(@Nonnull XMLObject xmlObject) throws MarshallException {
        return EidasStringUtil.toString(marshall(xmlObject));
    }

    @Nonnull
    public static XMLObject unmarshall(@Nonnull String xmlObjectString) throws UnmarshallException {
        return unmarshall(EidasStringUtil.getBytes(xmlObjectString));
    }

    /**
     * Method that unmarshalls a SAML Object from a byte array representation to an XML Object.
     *
     * @param xmlObjectBytes Byte array representation of a SAML Object
     * @return XML Object (superclass of SAMLObject)
     * @throws UnmarshallException when the bytes cannot be unmarshalled
     */
    @Nonnull
    @SuppressWarnings("squid:S2583")
    public static XMLObject unmarshall(@Nonnull byte[] xmlObjectBytes) throws UnmarshallException {
        Preconditions.checkNotNull(xmlObjectBytes, "xmlObjectBytes");

        Document document = null;
        try {
            document = DocumentBuilderFactoryUtil.parse(xmlObjectBytes);
        } catch (Exception ex) {
            LOG.error("Unmarshall: parsing exception for " + EidasStringUtil.toString(xmlObjectBytes) + ": " + ex, ex);
            throw new UnmarshallException(ex);
        }
        if (null == document) {
            LOG.error("Unmarshall: document is null for " + EidasStringUtil.toString(xmlObjectBytes));
            throw new UnmarshallException("Document is null");
        }
        Element root = document.getDocumentElement();
        if (null == root) {
            LOG.error("Unmarshall: root element is null for " + EidasStringUtil.toString(xmlObjectBytes));
            throw new UnmarshallException("Root element is null");
        }
        // Get appropriate unmarshaller
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        // Unmarshall using the SAML Token root element
        if (null == unmarshallerFactory) {
            LOG.error("No UnmarshallerFactory for " + EidasStringUtil.toString(xmlObjectBytes));
            throw new UnmarshallException("No UnmarshallerFactory");
        }
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(root);
        if (null == unmarshaller) {
            LOG.error("No Unmarshaller for " + EidasStringUtil.toString(xmlObjectBytes));
            throw new UnmarshallException("No Unmarshaller");
        }
        try {
            return unmarshaller.unmarshall(root);
        } catch (UnmarshallingException ue) {
            LOG.error("Unmarshall exception for " + EidasStringUtil.toString(xmlObjectBytes) + ": " + ue, ue);
            throw new UnmarshallException(ue);
        }
    }

    /**
     * Method that unmarshalls a SAML Object from a DOM Document representation to an XML Object.
     *
     * @param document DOM Document representation of a SAML Object
     * @return XML Object (superclass of SAMLObject)
     * @throws UnmarshallException when the bytes cannot be unmarshalled
     */
    @Nonnull
    public static XMLObject unmarshallFromDom(@Nonnull Document document) throws UnmarshallException {
        if (null == document) {
            LOG.error("Unmarshall: document is null");
            throw new UnmarshallException("Document is null");
        }
        Element root = document.getDocumentElement();
        if (null == root) {
            LOG.error("Unmarshall: root element is null");
            throw new UnmarshallException("Root element is null");
        }
        // Get appropriate unmarshaller
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        // Unmarshall using the SAML Token root element
        if (null == unmarshallerFactory) {
            LOG.error("No UnmarshallerFactory for " + document);
            throw new UnmarshallException("No UnmarshallerFactory");
        }
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(root);
        if (null == unmarshaller) {
            LOG.error("No Unmarshaller for " + document);
            throw new UnmarshallException("No Unmarshaller");
        }
        try {
            return unmarshaller.unmarshall(root);
        } catch (UnmarshallingException ue) {
            LOG.error("Unmarshall exception for " + document + ": " + ue, ue);
            throw new UnmarshallException(ue);
        }
    }

}
