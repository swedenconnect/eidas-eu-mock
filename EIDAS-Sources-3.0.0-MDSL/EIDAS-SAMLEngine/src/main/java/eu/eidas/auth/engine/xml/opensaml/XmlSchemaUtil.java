/*
 * Copyright (c) 2025 by European Commission
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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;

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
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "BUSINESS EXCEPTION : Validate schema exception: ", e);
        }
    }

    public static Document validateSamlSchema(@Nonnull byte[] samlBytes) throws EIDASSAMLEngineException {
        try {
            return validateSchema(OpenSamlHelper.getSchema(), samlBytes);
        } catch (EIDASSAMLEngineException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "BUSINESS EXCEPTION : Validate schema exception: ", e);
        }
    }

    public static void validateSchema(@Nonnull Schema schema, @Nonnull Document document)
            throws EIDASSAMLEngineException {
        try {
            Element element = document.getDocumentElement();
            DOMSource domSrc = new DOMSource(element);
            Validator validator = newValidator(schema);
            validator.validate(domSrc);
        } catch (IOException | SAXException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "BUSINESS EXCEPTION : Validate schema exception: ", e);
        }
    }

    public static Document validateSchema(@Nonnull Schema schema, @Nonnull String xmlString)
            throws EIDASSAMLEngineException {
        Document document;
        try {
            document = DocumentBuilderFactoryUtil.parse(xmlString);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOG.error(AbstractProtocolEngine.SAML_EXCHANGE, "BUSINESS EXCEPTION : Validate schema exception: " + e, e);
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR,
                    "BUSINESS EXCEPTION : Validate schema exception: ", e);
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
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR, "BUSINESS EXCEPTION : Validate schema exception: ", e);
        }
        validateSchema(schema, document);
        return document;
    }

    /**
     * Creates a new {@link Validator} instance using the default configuration of the provided {@link Schema}.
     * <p>
     * This method does not apply any explicit security features. The validator inherits its configuration from
     * the underlying parser implementation provided by the JDK, which may vary depending on the runtime environment.
     * Protections against external entity access (e.g., XXE) or schema injection are not enforced unless configured
     * externally.
     * </p>
     *
     * @param schema the {@link Schema} instance used to create the {@link Validator}
     * @return a new {@link Validator} using default parser configuration
     */
    @Nonnull
    public static Validator newValidator(@Nonnull Schema schema) {
        Validator validator = schema.newValidator();
        return validator;
    }

    private XmlSchemaUtil() {
    }
}
