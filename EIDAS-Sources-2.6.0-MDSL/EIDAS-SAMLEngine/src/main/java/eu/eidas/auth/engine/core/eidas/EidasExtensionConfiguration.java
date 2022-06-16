/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.auth.engine.core.eidas;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.eidas.impl.DigestMethodBuilder;
import eu.eidas.auth.engine.core.eidas.impl.DigestMethodMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.DigestMethodUnmarshaller;
import eu.eidas.auth.engine.core.eidas.impl.RequestedAttributeBuilder;
import eu.eidas.auth.engine.core.eidas.impl.RequestedAttributeMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.RequestedAttributeUnmarshaller;
import eu.eidas.auth.engine.core.eidas.impl.RequestedAttributesBuilder;
import eu.eidas.auth.engine.core.eidas.impl.RequestedAttributesMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.RequestedAttributesUnmarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SignatureMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SigningMethodBuilder;
import eu.eidas.auth.engine.core.eidas.impl.SigningMethodMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SigningMethodUnmarshaller;
import eu.eidas.auth.engine.metadata.samlobjects.NodeCountryType;
import eu.eidas.auth.engine.metadata.samlobjects.NodeCountryTypeBuilder;
import eu.eidas.auth.engine.metadata.samlobjects.NodeCountryTypeMarshaller;
import eu.eidas.auth.engine.metadata.samlobjects.NodeCountryTypeUnmarshaller;
import eu.eidas.auth.engine.metadata.samlobjects.SPType;
import eu.eidas.auth.engine.metadata.samlobjects.SPTypeBuilder;
import eu.eidas.auth.engine.metadata.samlobjects.SPTypeMarshaller;
import eu.eidas.auth.engine.metadata.samlobjects.SPTypeUnmarshaller;
import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.core.xml.schema.impl.XSAnyMarshaller;
import org.opensaml.core.xml.schema.impl.XSAnyUnmarshaller;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

/**
 * register the configuration for eIDAS format TODO: add unregister method
 */
public final class EidasExtensionConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureMarshaller.class);

    /**
     * @since 1.1
     *
     * @param protocolProcessor the protocol Processor
     */
    public static void configureExtension(@Nonnull ProtocolProcessorI protocolProcessor) {

        XMLObjectProviderRegistrySupport.registerObjectProvider(Signature.DEFAULT_ELEMENT_NAME, new SignatureBuilder(),
                new SignatureMarshaller(), new SignatureUnmarshaller());

        XMLObjectProviderRegistrySupport.registerObjectProvider(RequestedAttribute.DEF_ELEMENT_NAME, new RequestedAttributeBuilder(),
                new RequestedAttributeMarshaller(), new RequestedAttributeUnmarshaller());

        XMLObjectProviderRegistrySupport.registerObjectProvider(RequestedAttributes.DEF_ELEMENT_NAME, new RequestedAttributesBuilder(),
                new RequestedAttributesMarshaller(),
                new RequestedAttributesUnmarshaller());

        XMLObjectProviderRegistrySupport.registerObjectProvider(SigningMethod.DEF_ELEMENT_NAME, new SigningMethodBuilder(),
                new SigningMethodMarshaller(), new SigningMethodUnmarshaller());

        XMLObjectProviderRegistrySupport.registerObjectProvider(DigestMethod.DEF_ELEMENT_NAME, new DigestMethodBuilder(),
                new DigestMethodMarshaller(), new DigestMethodUnmarshaller());

        XMLObjectProviderRegistrySupport.registerObjectProvider(SPType.DEF_ELEMENT_NAME, new SPTypeBuilder(), new SPTypeMarshaller(),
                new SPTypeUnmarshaller());
        XMLObjectProviderRegistrySupport.registerObjectProvider(NodeCountryType.DEF_ELEMENT_NAME,
                new NodeCountryTypeBuilder(), new NodeCountryTypeMarshaller(), new NodeCountryTypeUnmarshaller());

        XSAnyBuilder xsAnyBuilder = new XSAnyBuilder();
        XSAnyMarshaller xsAnyMarshaller = new XSAnyMarshaller();
        XSAnyUnmarshaller xsAnyUnmarshaller = new XSAnyUnmarshaller();

        XMLObjectProviderRegistrySupport.registerObjectProvider(XSAny.TYPE_NAME, xsAnyBuilder, xsAnyMarshaller, xsAnyUnmarshaller);

        ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions = protocolProcessor.getAllSupportedAttributes();

        for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
            QName xmlType = attributeDefinition.getXmlType();
            // do not overwrite XSD types
            if (!XMLConstants.XSD_NS.equals(xmlType.getNamespaceURI())) {
                XMLObjectProviderRegistrySupport.registerObjectProvider(xmlType, xsAnyBuilder, xsAnyMarshaller, xsAnyUnmarshaller);
            }
        }
    }

    private EidasExtensionConfiguration() {
    }
}
