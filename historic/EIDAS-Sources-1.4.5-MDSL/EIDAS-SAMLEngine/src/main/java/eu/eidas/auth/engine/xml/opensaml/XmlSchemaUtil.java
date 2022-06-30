package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.opensaml.common.xml.SAMLSchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * XML Schema Utility class.
 *
 * @since 1.1
 */
public final class XmlSchemaUtil {

    private static final Logger LOG = LoggerFactory.getLogger(XmlSchemaUtil.class);

    public static Document validateSamlSchema(@Nonnull String samlString) throws EIDASSAMLEngineException {
        try {
            return validateSchema(SAMLSchemaBuilder.getSAML11Schema(), samlString);
        } catch (SAXException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
    }

    public static Document validateSamlSchema(@Nonnull byte[] samlBytes) throws EIDASSAMLEngineException {
        try {
            return validateSchema(SAMLSchemaBuilder.getSAML11Schema(), samlBytes);
        } catch (SAXException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
    }

    public static void validateSchema(@Nonnull Schema schema, @Nonnull Document document)
            throws EIDASSAMLEngineException {
        try {
            Element element = document.getDocumentElement();
            Validator validator = schema.newValidator();
            DOMSource domSrc = new DOMSource(element);
            validator.validate(domSrc);
        } catch (IOException | SAXException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
    }

    public static Document validateSchema(@Nonnull Schema schema, @Nonnull String xmlString)
            throws EIDASSAMLEngineException {
        Document document;
        try {
            document = DocumentBuilderFactoryUtil.parse(xmlString);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
        validateSchema(schema, document);
        return document;
    }

    public static Document validateSchema(@Nonnull Schema schema, @Nonnull byte[] xmlBytes)
            throws EIDASSAMLEngineException {
        Document document;
        try {
            document = DocumentBuilderFactoryUtil.parse(xmlBytes);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
        validateSchema(schema, document);
        return document;
    }

    /**
     * Create a new {@link Validator} for this schema, already set up with security features turned on.
     *
     * @param schema
     * @return a new {@link Validator} for this schema, already set up with security features turned on.
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    @Nonnull
    public static Validator newSecureValidator(@Nonnull Schema schema) throws SAXNotRecognizedException, SAXNotSupportedException {
        Validator validator = schema.newValidator();
        configureSecureValidator(validator);
        return validator;
    }

    /**
     * Configures a given {@link Validator} with security features turned on.
     *
     * @param validator the instance to configure
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @since 1.1.1
     */
    public static void configureSecureValidator(@Nonnull Validator validator) throws SAXNotRecognizedException, SAXNotSupportedException {
        Preconditions.checkNotNull(validator, "validator");

        for (final Map.Entry<String, String> entry : getSecureSchemaFeatures().entrySet()) {
            validator.setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Build the default set of parser features to use to protect a {@link javax.xml.validation.SchemaFactory} or a {@link javax.xml.validation.Validator} from XXE.
     * See https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet for more details.
     * The default features set are: <ul> <li>{@link
     * javax.xml.XMLConstants#ACCESS_EXTERNAL_DTD} = ""</li> <li>{@link javax.xml.XMLConstants#ACCESS_EXTERNAL_SCHEMA} = ""
     *</li></ul>
     */
    @Nonnull
    public static Map<String, String> getSecureSchemaFeatures() {
        Map<String, String> features = new HashMap<>();

        features.put(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        features.put(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);

        return features;
    }

    private XmlSchemaUtil() {
    }
}
