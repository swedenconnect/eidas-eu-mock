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

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableSortedSet;

import org.opensaml.Configuration;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.schema.impl.XSAnyMarshaller;
import org.opensaml.xml.schema.impl.XSAnyUnmarshaller;
import org.opensaml.xml.util.XMLConstants;

import eu.eidas.auth.commons.EIDASUtil;
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
import eu.eidas.auth.engine.core.eidas.impl.SPCountryBuilder;
import eu.eidas.auth.engine.core.eidas.impl.SPCountryMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SPCountryUnmarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SPTypeBuilder;
import eu.eidas.auth.engine.core.eidas.impl.SPTypeMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SPTypeUnmarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SigningMethodBuilder;
import eu.eidas.auth.engine.core.eidas.impl.SigningMethodMarshaller;
import eu.eidas.auth.engine.core.eidas.impl.SigningMethodUnmarshaller;

/**
 * register the configuration for eIDAS format TODO: add unregister method
 */
public final class EidasExtensionConfiguration {

    /**
     * @deprecated since 1.1
     */
    @Deprecated
    public static void configureExtension(@Nonnull EidasExtensionProcessor extensionProcessor) {

        Configuration.registerObjectProvider(RequestedAttribute.DEF_ELEMENT_NAME, new RequestedAttributeBuilder(),
                                             new RequestedAttributeMarshaller(), new RequestedAttributeUnmarshaller());

        Configuration.registerObjectProvider(RequestedAttributes.DEF_ELEMENT_NAME, new RequestedAttributesBuilder(),
                                             new RequestedAttributesMarshaller(),
                                             new RequestedAttributesUnmarshaller());

        Configuration.registerObjectProvider(SigningMethod.DEF_ELEMENT_NAME, new SigningMethodBuilder(),
                                             new SigningMethodMarshaller(), new SigningMethodUnmarshaller());

        Configuration.registerObjectProvider(DigestMethod.DEF_ELEMENT_NAME, new DigestMethodBuilder(),
                                             new DigestMethodMarshaller(), new DigestMethodUnmarshaller());

        Configuration.registerObjectProvider(SPType.DEF_ELEMENT_NAME, new SPTypeBuilder(), new SPTypeMarshaller(),
                                             new SPTypeUnmarshaller());

        Configuration.registerObjectProvider(SPCountry.DEF_ELEMENT_NAME, new SPCountryBuilder(),
                                             new SPCountryMarshaller(), new SPCountryUnmarshaller());

        XSAnyBuilder xsAnyBuilder = new XSAnyBuilder();
        XSAnyMarshaller xsAnyMarshaller = new XSAnyMarshaller();
        XSAnyUnmarshaller xsAnyUnmarshaller = new XSAnyUnmarshaller();

        Configuration.registerObjectProvider(XSAny.TYPE_NAME, xsAnyBuilder, xsAnyMarshaller, xsAnyUnmarshaller);

        ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions = extensionProcessor.getAllSupportedAttributes();

        for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
            QName xmlType = attributeDefinition.getXmlType();
            // do not overwrite XSD types
            if (XMLConstants.XSD_NS.equals(xmlType.getNamespaceURI())) {
                continue;
            }
            Configuration.registerObjectProvider(xmlType, xsAnyBuilder, xsAnyMarshaller, xsAnyUnmarshaller);
        }

//        GenericEidasAttributeTypeBuilder genericBuilder = new GenericEidasAttributeTypeBuilder();
//        for(String attrName: EIDASAttributes.ATTRIBUTES_TO_TYPESNAMES.values()){
//            Configuration.registerObjectProvider(genericBuilder.buildObject().getDefElementName(attrName),
//                    genericBuilder, new GenericEidasAttributeTypeMarshaller(),
//                    new GenericEidasAttributeTypeUnmarshaller());
//
//        }
    }

    /**
     * @since 1.1
     */
    public static void configureExtension(@Nonnull ProtocolProcessorI protocolProcessor) {

        Configuration.registerObjectProvider(RequestedAttribute.DEF_ELEMENT_NAME, new RequestedAttributeBuilder(),
                                             new RequestedAttributeMarshaller(), new RequestedAttributeUnmarshaller());

        Configuration.registerObjectProvider(RequestedAttributes.DEF_ELEMENT_NAME, new RequestedAttributesBuilder(),
                                             new RequestedAttributesMarshaller(),
                                             new RequestedAttributesUnmarshaller());

        Configuration.registerObjectProvider(SigningMethod.DEF_ELEMENT_NAME, new SigningMethodBuilder(),
                                             new SigningMethodMarshaller(), new SigningMethodUnmarshaller());

        Configuration.registerObjectProvider(DigestMethod.DEF_ELEMENT_NAME, new DigestMethodBuilder(),
                                             new DigestMethodMarshaller(), new DigestMethodUnmarshaller());

        Configuration.registerObjectProvider(SPType.DEF_ELEMENT_NAME, new SPTypeBuilder(), new SPTypeMarshaller(),
                                             new SPTypeUnmarshaller());

        Configuration.registerObjectProvider(SPCountry.DEF_ELEMENT_NAME, new SPCountryBuilder(),
                                             new SPCountryMarshaller(), new SPCountryUnmarshaller());

        XSAnyBuilder xsAnyBuilder = new XSAnyBuilder();
        XSAnyMarshaller xsAnyMarshaller = new XSAnyMarshaller();
        XSAnyUnmarshaller xsAnyUnmarshaller = new XSAnyUnmarshaller();

        Configuration.registerObjectProvider(XSAny.TYPE_NAME, xsAnyBuilder, xsAnyMarshaller, xsAnyUnmarshaller);

        ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions = protocolProcessor.getAllSupportedAttributes();

        for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
            QName xmlType = attributeDefinition.getXmlType();
            // do not overwrite XSD types
            if (XMLConstants.XSD_NS.equals(xmlType.getNamespaceURI())) {
                continue;
            }
            Configuration.registerObjectProvider(xmlType, xsAnyBuilder, xsAnyMarshaller, xsAnyUnmarshaller);
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
