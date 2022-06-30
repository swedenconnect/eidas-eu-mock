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

package eu.eidas.auth.engine.core.stork.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.IndexedXMLObjectChildrenList;

import eu.eidas.auth.engine.core.stork.RequestedAttribute;
import eu.eidas.auth.engine.core.stork.RequestedAttributes;

/**
 * The Class RequestedAttributesImpl.
 *
 * @author fjquevedo
 */
public class RequestedAttributesImpl extends AbstractSAMLObject implements
	RequestedAttributes {

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
     * @see eu.eidas.auth.engine.core.RequestedAttributes#getAttributes()
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
