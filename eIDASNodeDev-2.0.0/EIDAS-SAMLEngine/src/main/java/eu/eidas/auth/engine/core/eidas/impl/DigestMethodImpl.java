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

import eu.eidas.auth.engine.core.eidas.DigestMethod;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObject;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class DigestMethodImpl.
 */
public class DigestMethodImpl extends AbstractSAMLObject implements DigestMethod {

    /** The algorithm. */
    private String algorithm;

    /**
     * Instantiates a new DigestMethod implementation.
     *
     * @param namespaceURI the namespace URI
     * @param elementLocalName the element local name
     * @param namespacePrefix the namespace prefix
     */
    protected DigestMethodImpl(final String namespaceURI,
                               final String elementLocalName, final String namespacePrefix) {
	super(namespaceURI, elementLocalName, namespacePrefix);
    }


    /**
     * Gets the algorithm
     *
     * @return the algorithm
     *
     * @see DigestMethod#getAlgorithm()
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
	    return new ArrayList<>();
    }

    @Override
    public int hashCode() {// NOSONAR
        throw new UnsupportedOperationException("hashCode method not implemented");
    }

}
