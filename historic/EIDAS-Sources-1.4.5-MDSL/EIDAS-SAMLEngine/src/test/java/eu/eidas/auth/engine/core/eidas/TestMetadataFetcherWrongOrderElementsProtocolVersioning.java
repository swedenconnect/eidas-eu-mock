package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.common.impl.ExtensionsBuilder;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.samlext.saml2mdattr.EntityAttributes;
import org.opensaml.samlext.saml2mdattr.impl.EntityAttributesBuilder;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;

import javax.annotation.Nonnull;

/**
 * Test MetadataFetcher.
 *
 */
public final class TestMetadataFetcherWrongOrderElementsProtocolVersioning implements MetadataFetcherI {
    @Override
    @Nonnull
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner) {
        EntityDescriptor entityDescriptor = new EntityDescriptorBuilder().buildObject();
        Extensions extensions = new ExtensionsBuilder().buildObject();
        EntityAttributesBuilder entityAttributesBuilder = new EntityAttributesBuilder();
        EntityAttributes entityAttributes = entityAttributesBuilder.buildObject();

        addAttribute( entityAttributes,"attribute", "attributeValueText");
        addAttribute( entityAttributes,EidasMetadata.APPLICATION_IDENTIFIER, "CEF:eIDAS-ref:2.2");
        addAttribute( entityAttributes,"anotherAttribute", "anotherAttributeValueText");
        addAttribute( entityAttributes,EidasMetadata.PROTOCOL_VERSION_URI, "1.1");
        addAttribute( entityAttributes,"yetAnotherAttribute", "yetAnotherAttributeValueText");

        extensions.getUnknownXMLObjects().add(entityAttributes);
        entityDescriptor.setExtensions(extensions);

        return entityDescriptor;
    }

    private void addAttribute(EntityAttributes entityAttributes, String name, String valueText) {
        Attribute attribute = new AttributeBuilder().buildObject();
        XSAny applicationIdentifier = buildAttributeValue(valueText);
        attribute.setName(name);
        attribute.getAttributeValues().add(applicationIdentifier);
        entityAttributes.getAttributes().add(attribute);
    }

    private XSAny buildAttributeValue(String valueText) {
        XSAnyBuilder builder = new XSAnyBuilder();
        XSAny ep = builder.buildObject(SAMLConstants.SAML20_NS, AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        ep.setTextContent(valueText);

        return ep;
    }

}
