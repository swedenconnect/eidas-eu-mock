/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
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
            return validateSchema(OpenSamlHelper.getSchema(), samlString);
        } catch (EIDASSAMLEngineException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
    }

    public static Document validateSamlSchema(@Nonnull byte[] samlBytes) throws EIDASSAMLEngineException {
        try {
            return validateSchema(OpenSamlHelper.getSchema(), samlBytes);
        } catch (EIDASSAMLEngineException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                                               EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage(), e);
        }
    }

    public static void validateSchema(@Nonnull Schema schema, @Nonnull Document document)
            throws EIDASSAMLEngineException {
        try {
            Element element = document.getDocumentElement();
            DOMSource domSrc = new DOMSource(element);
            Validator validator = newSecureValidator(schema);
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
     * @param schema the {@link Schema} instance to be used in creating the secured {@link Validator} which is should have
     *               security features turned on see {@link OpenSamlHelper#getSchema()}
     * @return a new {@link Validator} for this schema, already set up with security features turned on.
     */
    @Nonnull
    public static Validator newSecureValidator(@Nonnull Schema schema) {
        Validator validator = schema.newValidator();
        return validator;
    }

    /**
     * Configures a given {@link Validator} with security features turned on.
     *
     * @param validator the instance to configure
     * @throws SAXNotRecognizedException exception for an unrecognized identifier
     * @throws SAXNotSupportedException  exception for an unsupported operation
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
     *
     * @return the map of features
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
