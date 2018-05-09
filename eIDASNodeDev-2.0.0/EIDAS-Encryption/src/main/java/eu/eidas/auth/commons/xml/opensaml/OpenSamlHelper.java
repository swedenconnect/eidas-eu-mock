package eu.eidas.auth.commons.xml.opensaml;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.util.Preconditions;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ClasspathResolver;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.SchemaBuilder;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.*;
import org.opensaml.saml.common.xml.SAMLSchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.validation.Schema;
import java.io.IOException;

/**
 * OpenSAML Helper.
 *
 * @since 1.1
 */
public final class OpenSamlHelper {

    //TODO vazrica check if this is still necessary.
    public static final String SYSPROP_HTTPCLIENT_HTTPS_DISABLE_HOSTNAME_VERIFICATION = "org.opensaml.httpclient.https.disableHostnameVerification";

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OpenSamlHelper.class);

    private static final ParserPool SECURED_PARSER_POOL;

    private static final Schema SCHEMA;

    private static final Schema METADATA_SCHEMA;

    static {
        LOG.info("OpenSamlHelper: Initialize OpenSAML");
        try {
            InitializationService.initialize();
            SECURED_PARSER_POOL = newSecuredBasicParserPool();

            XMLObjectProviderRegistrySupport.setParserPool(SECURED_PARSER_POOL);

            SAMLSchemaBuilder schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
            SchemaBuilder scBuilder = new SchemaBuilder();
            scBuilder.setResourceResolver(new ClasspathResolver());
            scBuilder.addSchema(new ClassPathResource("/xmldsig-core-schema.xsd").getInputStream());

            scBuilder.addSchema(new ClassPathResource("/eidas/saml_eidas_extension.xsd").getInputStream());

            scBuilder.addSchema(new ClassPathResource("/eidas/saml_eidas_legal_person.xsd").getInputStream());
            scBuilder.addSchema(new ClassPathResource("/eidas/saml_eidas_natural_person.xsd").getInputStream());
            scBuilder.addSchema(new ClassPathResource("/eidas/saml_eidas_representative_legal_person.xsd").getInputStream());
            scBuilder.addSchema(new ClassPathResource("/eidas/saml_eidas_representative_natural_person.xsd").getInputStream());

            schemaBuilder.setSchemaBuilder(scBuilder);
            SCHEMA = schemaBuilder.getSAMLSchema();

            //the schema is joint
            METADATA_SCHEMA = SCHEMA;

        } catch (InitializationException | ComponentInitializationException | SAXException | IOException e) {
            LOG.error("Problem initializing the OpenSAML library: " + e, e);
            throw new IllegalStateException(e);
        }
    }

    private OpenSamlHelper() {
    }

    public static void initialize() {
        //this class has a static initializer for now, this call is to make sure JVM does not optimize
        getSecuredParserPool();
    }

    @Nonnull
    public static ParserPool getSecuredParserPool() {
        ParserPool parserPool = XMLObjectProviderRegistrySupport.getParserPool();
        if (parserPool != SECURED_PARSER_POOL) {
            XMLObjectProviderRegistrySupport.setParserPool(SECURED_PARSER_POOL);
        }
        return SECURED_PARSER_POOL;
    }

    @Nonnull
    public static Schema getSchema() {
        return SCHEMA;
    }

    @Nonnull
    public static Schema getMetadataSchema() {
        return METADATA_SCHEMA;
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

        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
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
    private static ParserPool newSecuredBasicParserPool() throws ComponentInitializationException {
        // Get parser pool manager
        BasicParserPool ppMgr = new BasicParserPool();
        // Note: this is necessary due to an unresolved Xerces deferred DOM issue/bug
        ppMgr.setBuilderFeatures(DocumentBuilderFactoryUtil.getSecureDocumentBuilderFeatures());
        ppMgr.setNamespaceAware(true);
        ppMgr.setIgnoreComments(true);
        ppMgr.setExpandEntityReferences(false);
        ppMgr.setXincludeAware(false);
        ppMgr.initialize();
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
        UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
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
        } catch (UnmarshallingException e) {
            LOG.error("Unmarshall exception for " + EidasStringUtil.toString(xmlObjectBytes) + ": " + e, e);
            throw new UnmarshallException(e);
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
        UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
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
