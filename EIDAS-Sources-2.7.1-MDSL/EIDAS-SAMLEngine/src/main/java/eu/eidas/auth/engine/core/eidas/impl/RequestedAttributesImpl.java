/*
 * Copyright (c) 2021 by European Commission
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
import eu.eidas.auth.engine.core.eidas.RequestedAttributes;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.IndexedXMLObjectChildrenList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class RequestedAttributesImpl.
 *
 */
public class RequestedAttributesImpl extends AbstractXMLObject implements RequestedAttributes {

    /**
     * Instantiates a new requested attributes implement.
     *
     * @param namespaceURI the namespace URI
     * @param elementLocalName the element local name
     * @param namespacePrefix the namespace prefix
     */
    protected RequestedAttributesImpl(final String namespaceURI,
	    final String elementLocalName, final String namespacePrefix) {
	super(namespaceURI, elementLocalName, namespacePrefix);
	indexedChildren = new IndexedXMLObjectChildrenList<XMLObject>(this);
    }

    /** The indexed children. */
    private final IndexedXMLObjectChildrenList<XMLObject> indexedChildren;

    /**
     * Gets the indexed children.
     *
     * @return the indexed children
     */
    public final IndexedXMLObjectChildrenList<XMLObject> getIndexedChildren() {
	return indexedChildren;
    }


    /**
     * Gets the ordered children.
     *
     * @return the ordered children
     */
    public final List<XMLObject> getOrderedChildren() {

	final List<XMLObject> children = new ArrayList<XMLObject>();

	children.addAll(indexedChildren);

	return Collections.unmodifiableList(children);

    }

    /**
     * Gets the attributes.
     *
     * @return the attributes
     *
     * @see RequestedAttributes#getAttributes()
     */
    @SuppressWarnings("unchecked")
    public final List<RequestedAttribute> getAttributes() {
	return (List<RequestedAttribute>) indexedChildren
		.subList(RequestedAttribute.DEF_ELEMENT_NAME);
    }

    @Override
    public int hashCode() {// NOSONAR
        throw new UnsupportedOperationException("hashCode method not implemented");
    }
}
