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

import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.impl.ExtensionsBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;

import javax.xml.namespace.QName;

/**
 * Open SAML {@link XMLObjectBuilderFactory} utility class.
 *
 * @since 2.0.0
 */
public final class MetadataBuilderFactoryUtil {


    private MetadataBuilderFactoryUtil() {
    }

    /**
     * Creates the SAML object.
     *
     * @param qname the QName
     * @return the XML object
     * @throws EIDASMetadataException if the xml object can not be built
     */
    public static XMLObject buildXmlObject(QName qname) throws EIDASMetadataException {
        XMLObjectBuilder builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname);
        if (builder == null) {
            throw new EIDASMetadataException("Unable to instantiate BuilderFactory from qname " + qname);
        }
        return builder.buildObject(qname);
    }

    public static <T> T buildXmlObject(Class<T> clazz) throws NoSuchFieldException, IllegalAccessException {
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
        XMLObjectBuilder builder = builderFactory.getBuilder(defaultElementName);

        return (T) builder.buildObject(defaultElementName);
    }

    /**
     * Creates the SAML object.
     *
     * @param qname  the quality name
     * @param qname1 the qname1
     * @return the xML object
     */
    public static XMLObject buildXmlObject(QName qname, QName qname1) {
        return XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qname1).buildObject(qname, qname1);
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
     * @throws EIDASMetadataException if the issuer can not be generated
     */
    public static Issuer generateIssuer() throws EIDASMetadataException {
        return (Issuer) buildXmlObject(Issuer.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate key info.
     *
     * @return the key info
     * @throws EIDASMetadataException if the key info can not be generated
     */
    public static KeyInfo generateKeyInfo() throws EIDASMetadataException {
        return (KeyInfo) buildXmlObject(KeyInfo.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate name id.
     *
     * @return the name id
     * @throws EIDASMetadataException if the nameId can not be generated
     */
    public static NameID generateNameID() throws EIDASMetadataException {
        return (NameID) buildXmlObject(NameID.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Generate name id.
     *
     * @param nameQualifier   the name qualifier
     * @param format          the format
     * @param spNameQualifier the sP name qualifier
     * @return the name id
     */
    public static NameID generateNameID(String nameQualifier, String format, String spNameQualifier) {
        // <saml:NameID>
        NameID nameId = (NameID) XMLObjectProviderRegistrySupport.getBuilderFactory()
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
     * Generate status.
     *
     * @param statusCode the status code
     * @return the status
     * @throws EIDASMetadataException if the status can not be generated
     */
    public static Status generateStatus(StatusCode statusCode) throws EIDASMetadataException {
        Status status = (Status) buildXmlObject(Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode(statusCode);
        return status;
    }

    /**
     * Generate status code.
     *
     * @param value the value
     * @return the status code
     * @throws EIDASMetadataException if the status can not be generated
     */
    public static StatusCode generateStatusCode(String value) throws EIDASMetadataException {
        StatusCode statusCode = (StatusCode) buildXmlObject(StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(value);
        return statusCode;
    }

    /**
     * Generate status message.
     *
     * @param message the message
     * @return the status message
     * @throws EIDASMetadataException if the status can not be generated
     */
    public static StatusMessage generateStatusMessage(String message) throws EIDASMetadataException {
        StatusMessage statusMessage = (StatusMessage) buildXmlObject(StatusMessage.DEFAULT_ELEMENT_NAME);
        statusMessage.setMessage(message);
        return statusMessage;
    }
}
