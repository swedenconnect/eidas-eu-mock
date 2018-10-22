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
public final class TestMetadataFetcher implements MetadataFetcherI {
    @Override
    @Nonnull
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner) {
        EntityDescriptor entityDescriptor = new EntityDescriptorBuilder().buildObject();
        Extensions extensions = new ExtensionsBuilder().buildObject();
        EntityAttributesBuilder entityAttributesBuilder = new EntityAttributesBuilder();
        EntityAttributes entityAttributes = entityAttributesBuilder.buildObject();

        Attribute attributeProtocol = new AttributeBuilder().buildObject();
        XSAny protocol = buildAttributeValue("1.1");
        attributeProtocol.setName(EidasMetadata.PROTOCOL_VERSION_URI);
        attributeProtocol.getAttributeValues().add(protocol);
        entityAttributes.getAttributes().add(attributeProtocol);

        Attribute attributeApplicationIdentifier = new AttributeBuilder().buildObject();
        XSAny applicationIdentifier = buildAttributeValue("CEF:eIDAS-ref:2.2");
        attributeApplicationIdentifier.setName(EidasMetadata.APPLICATION_IDENTIFIER);
        attributeApplicationIdentifier.getAttributeValues().add(applicationIdentifier);
        entityAttributes.getAttributes().add(attributeApplicationIdentifier);

        extensions.getUnknownXMLObjects().add(entityAttributes);
        entityDescriptor.setExtensions(extensions);

//        final String requestApplicationIdentifier = ((XSAnyImpl) metadataFetcher.getEntityDescriptor(request.getIssuer(), metadataSigner).getExtensions().getUnknownXMLObjects().get(0).getOrderedChildren().get(1).getOrderedChildren().get(0)).getTextContent();
        return entityDescriptor;
    }

    private XSAny buildAttributeValue(String valueText) {
        XSAnyBuilder builder = new XSAnyBuilder();
        XSAny ep = builder.buildObject(SAMLConstants.SAML20_NS, AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        ep.setTextContent(valueText);

        return ep;
    }

}
