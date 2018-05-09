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

import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.eidas.RequestedAttribute;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * The Class RequestedAttributeUnmarshaller.
 *
 */
public class RequestedAttributeUnmarshaller extends
		AbstractSAMLObjectUnmarshaller {

    /**
     * Process child element.
     *
     * @param parentSAMLObject parent SAMLObject
     * @param childSAMLObject child SAMLObject
     *
     * @throws UnmarshallingException error at unmarshall XML object
     */
    protected final void processChildElement(final XMLObject parentSAMLObject,
	    final XMLObject childSAMLObject) throws UnmarshallingException {

	final RequestedAttribute requestedAttr = (RequestedAttribute) parentSAMLObject;

	final QName childQName = childSAMLObject.getElementQName();
	if ("AttributeValue".equals(childQName.getLocalPart())
		&& childQName.getNamespaceURI().equals(SAMLCore.EIDAS10_NS.getValue())) {
	    requestedAttr.getAttributeValues().add(childSAMLObject);
	} else {
	    super.processChildElement(parentSAMLObject, childSAMLObject);
	}
    }

	/**
	 * Process attribute.
	 *
	 * @param samlObject the SAML object
	 * @param attribute  the attribute
	 * @throws UnmarshallingException the unmarshalling exception
	 */
	protected final void processAttribute(final XMLObject samlObject,
										  final Attr attribute) throws UnmarshallingException {

		final RequestedAttribute requestedAttr = (RequestedAttribute) samlObject;

		if (attribute.getLocalName()
				.equals(RequestedAttribute.NAME_ATTRIB_NAME)) {
			requestedAttr.setName(attribute.getValue());
		} else if (attribute.getLocalName().equals(
				RequestedAttribute.NAME_FORMAT_ATTR)) {
			requestedAttr.setNameFormat(attribute.getValue());
		} else if (attribute.getLocalName().equals(
				RequestedAttribute.FRIENDLY_NAME_ATT)) {
			requestedAttr.setFriendlyName(attribute.getValue());
		} else if (attribute.getLocalName().equals(
				RequestedAttribute.IS_REQUIRED_ATTR)) {
			requestedAttr.setIsRequired(attribute
					.getValue());

		} else {
			final QName attribQName = getNodeQName(attribute);
			if (attribute.isId()) {
				requestedAttr.getUnknownAttributes().registerID(attribQName);
			}
			requestedAttr.getUnknownAttributes().put(attribQName,
					attribute.getValue());
		}
	}

	/**
	 * Gets the QName for the given DOM node.
	 *
	 * @param domNode the DOM node
	 * @return the QName for the element or null if the element was null
	 */
	private QName getNodeQName(Node domNode) {
		if (domNode != null) {
			return constructQName(domNode.getNamespaceURI(), domNode.getLocalName(), domNode.getPrefix());
		}
		return null;
	}

	/**
	 * Constructs a QName.
	 *
	 * @param namespaceURI the namespace of the QName
	 * @param localName    the local name of the QName
	 * @param prefix       the prefix of the QName, may be null
	 * @return the QName
	 */
	private QName constructQName(String namespaceURI, String localName, String prefix) {
		if (StringUtils.isEmpty(prefix)) {
			return new QName(namespaceURI, localName);
		} else if (StringUtils.isEmpty(namespaceURI)) {
			return new QName(localName);
		}
		return new QName(namespaceURI, localName, prefix);
	}
}
