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

package eu.eidas.auth.engine.metadata.samlobjects;

import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The implementation for the NodeCountry type element.
 */
public class NodeCountryTypeImpl extends AbstractXMLObject implements NodeCountryType {

    /**
     * the ISO 3166-1 alpha-2 format for Nationality Code of the SP country or international organization
     */
    private static final Pattern PATTERN = Pattern.compile("[A-Z][A-Z]");

    /** The node country. */
    private String nodeCountry;

    /**
     * Instantiates a new node country type with the default:
     * namespace prefix,
     * namespace uri,
     * local element name
     * and with the given nodeCountry value
     *
     * @param nodeCountry the node country value.
     */
    protected NodeCountryTypeImpl(final String nodeCountry) {
        super(NodeCountryType.DEF_ELEMENT_NAME.getNamespaceURI(), NodeCountryType.DEF_ELEMENT_NAME.getLocalPart(),
                NodeCountryType.DEF_ELEMENT_NAME.getPrefix());
        this.nodeCountry = nodeCountry;
    }

    /**
     * Instantiates a new node country type.
     *
     * @param namespaceURI the namespace URI
     * @param elementLocalName the element local name
     * @param namespacePrefix the namespace prefix
     */
    protected NodeCountryTypeImpl(final String namespaceURI, final String elementLocalName,
            final String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /**
     * Gets the ordered children.
     *
     * @return the ordered children
     */
    public final List<XMLObject> getOrderedChildren() {
        return new ArrayList<>();
    }

    @Override
    public String getNodeCountry() {
        return nodeCountry;
    }

    @Override
    public void setNodeCountry(final String nodeCountry) {
        if (!isValid(nodeCountry)) {
            throw new IllegalArgumentException("Invalid node country [" + nodeCountry + "] value");
        }
        this.nodeCountry = prepareForAssignment(this.nodeCountry, nodeCountry);
    }

    private boolean isValid(final String nodeCountry) {
        if (nodeCountry == null) {
            return false;
        }
        return NodeCountryTypeImpl.PATTERN.matcher(nodeCountry).matches();
    }

}
