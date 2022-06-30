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

package eu.eidas.auth.engine.core.eidas.impl;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;

import eu.eidas.auth.engine.core.eidas.SigningMethod;


/**
 * The Class SigningMethodImpl.
 */
public class SigningMethodImpl extends AbstractSAMLObject implements SigningMethod {

    /** The algorithm. */
    private String algorithm;

    /**
     * Instantiates a new SigningMethod implementation.
     *
     * @param namespaceURI the namespace URI
     * @param elementLocalName the element local name
     * @param namespacePrefix the namespace prefix
     */
    protected SigningMethodImpl(final String namespaceURI,
                                final String elementLocalName, final String namespacePrefix) {
	super(namespaceURI, elementLocalName, namespacePrefix);
    }


    /**
     * Gets the algorithm
     *
     * @return the algorithm
     *
     * @see SigningMethod#getAlgorithm()
     */
    public final String getAlgorithm() {
	return algorithm;
    }


    /**
     * Sets the  algorithm.
     *
     * @param newAlgo the new signing algorithm
     */
    public final void setAlgorithm(final String newAlgo) {
	    this.algorithm = prepareForAssignment(this.algorithm, newAlgo);
    }


    /**
     * Gets the ordered children.
     *
     * @return the ordered children
     */
    public final List<XMLObject> getOrderedChildren() {
	    return new ArrayList<XMLObject>();
    }

    @Override
    public int hashCode() {// NOSONAR
        throw new UnsupportedOperationException("hashCode method not implemented");
    }

}
