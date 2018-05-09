/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core.eidas;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.eidas.impl.*;
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

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

/**
 * register the configuration for eIDAS format TODO: add unregister method
 */
public final class EidasExtensionConfiguration {

    /**
     * @since 1.1
     */
    public static void configureExtension(@Nonnull ProtocolProcessorI protocolProcessor) {

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

//        GenericEidasAttributeTypeBuilder genericBuilder = new GenericEidasAttributeTypeBuilder();
//        for(String attrName: EIDASAttributes.ATTRIBUTES_TO_TYPESNAMES.values()){
//            Configuration.registerObjectProvider(genericBuilder.buildObject().getDefElementName(attrName),
//                    genericBuilder, new GenericEidasAttributeTypeMarshaller(),
//                    new GenericEidasAttributeTypeUnmarshaller());
//
//        }
    }

    private EidasExtensionConfiguration() {
    }
}
