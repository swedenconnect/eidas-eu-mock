/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.auth.engine.core.eidas.impl;

import eu.eidas.auth.engine.core.eidas.RequestedAttribute;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map.Entry;

/**
 * The Class RequestedAttributeMarshaller.
 *
 */
public class RequestedAttributeMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Marshall attributes.
     *
     * @param samlElement the SAML element
     * @param domElement the DOM element
     * @throws MarshallingException the marshalling exception
     */
    protected final void marshallAttributes(final XMLObject samlElement,
	    final Element domElement) throws MarshallingException {
	final RequestedAttribute requestedAttr = (RequestedAttribute) samlElement;

	if (requestedAttr.getName() != null) {
	    domElement.setAttributeNS(null,
		    RequestedAttribute.NAME_ATTRIB_NAME, requestedAttr
			    .getName());
	}

	if (requestedAttr.getNameFormat() != null) {
	    domElement.setAttributeNS(null,
		    RequestedAttribute.NAME_FORMAT_ATTR, requestedAttr
			    .getNameFormat());
	}

	if (requestedAttr.getFriendlyName() != null) {
	    domElement.setAttributeNS(null,
		    RequestedAttribute.FRIENDLY_NAME_ATT, requestedAttr
			    .getFriendlyName());
	}

	if (requestedAttr.getIsRequiredXSBoolean() != null) {
	    domElement.setAttributeNS(null,
		    RequestedAttribute.IS_REQUIRED_ATTR, requestedAttr
			    .getIsRequiredXSBoolean());
	}

		Attr attr;
		for (Entry<QName, String> entry : requestedAttr.getUnknownAttributes().entrySet()) {
			attr = constructAttribute(domElement.getOwnerDocument(), entry.getKey());
			attr.setValue(entry.getValue());
			domElement.setAttributeNodeNS(attr);
			if (XMLObjectProviderRegistrySupport.isIDAttribute(entry.getKey())
					|| requestedAttr.getUnknownAttributes().isIDAttribute(
					entry.getKey())) {
				attr.getOwnerElement().setIdAttributeNode(attr, true);
			}
		}
    }

	/**
	 * Constructs an attribute owned by the given document with the given name.
	 *
	 * @param owningDocument the owning document
	 * @param attributeName the name of that attribute
	 *
	 * @return the constructed attribute
	 */
	private Attr constructAttribute(Document owningDocument, QName attributeName) {
		return constructAttribute(owningDocument, attributeName.getNamespaceURI(), attributeName.getLocalPart(),
				attributeName.getPrefix());
	}

	/**
	 * Constructs an attribute owned by the given document with the given name.
	 *
	 * @param document the owning document
	 * @param namespaceURI the URI for the namespace the attribute is in
	 * @param localName the local name
	 * @param prefix the prefix of the namespace that attribute is in
	 *
	 * @return the constructed attribute
	 */
	private Attr constructAttribute(Document document, String namespaceURI, String localName, String prefix) {
		String trimmedLocalName = safeTrimOrNullString(localName);

		if (trimmedLocalName == null) {
			throw new IllegalArgumentException("Local name may not be null or empty");
		}

		String qualifiedName;
		String trimmedPrefix = safeTrimOrNullString(prefix);
		if (trimmedPrefix != null) {
			qualifiedName = trimmedPrefix + ":" + safeTrimOrNullString(trimmedLocalName);
		} else {
			qualifiedName = safeTrimOrNullString(trimmedLocalName);
		}

		if (StringUtils.isEmpty(namespaceURI)) {
			return document.createAttributeNS(null, qualifiedName);
		} else {
			return document.createAttributeNS(namespaceURI, qualifiedName);
		}
	}

	/**
	 * Removes preceeding or proceeding whitespace from a string or return null if the string is null or of zero length
	 * after trimming (i.e. if the string only contained whitespace).
	 *
	 * @param s the string to trim
	 *
	 * @return the trimmed string or null
	 */
	private String safeTrimOrNullString(String s) {
		if (s != null) {
			String sTrimmed = s.trim();
			if (sTrimmed.length() > 0) {
				return sTrimmed;
			}
		}

		return null;
	}

}
