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

import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.impl.ExtensionsBuilder;

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
     * Generate metadata extension.
     *
     * @return the extensions
     */
    public static Extensions generateMetadataExtension() {
        ExtensionsBuilder extensionsBuilder = new ExtensionsBuilder();
        return extensionsBuilder.buildObject(SAMLConstants.SAML20MD_NS, "Extensions", "md");
    }

}
