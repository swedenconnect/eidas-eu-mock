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
import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;

import eu.eidas.auth.engine.core.stork.EIDCrossBorderShare;

/**
 * The Class EIDCrossBorderShareImpl.
 *
 * @author fjquevedo
 */
public class EIDCrossBorderShareImpl extends AbstractSAMLObject implements
	EIDCrossBorderShare {

	/** The citizen country code. */
    private String eIDCrossBorderShare;

    /**
     * Instantiates a new eID cross border share implementation.
     *
     * @param namespaceURI the namespace URI
     * @param elementLocalName the element local name
     * @param namespacePrefix the namespace prefix
     */
    protected EIDCrossBorderShareImpl(final String namespaceURI,
	    final String elementLocalName, final String namespacePrefix) {
	super(namespaceURI, elementLocalName, namespacePrefix);
    }



    /**
     * Gets the eID cross border share.
     *
     * @return the eID cross border share
     */
    public final String getEIDCrossBorderShare() {
    	return eIDCrossBorderShare;
    }


    /**
     * Sets the eID cross border share.
     *
     * @param newEIDCrossBorderShare the new eID cross border share
     */
    public final void setEIDCrossBorderShare(String newEIDCrossBorderShare) {
    	this.eIDCrossBorderShare = prepareForAssignment(this.eIDCrossBorderShare, newEIDCrossBorderShare);
    }

    /**
     * Gets the ordered children.
     *
     * @return the ordered children
     * {@inheritDoc}
     */
    public final List<XMLObject> getOrderedChildren() {
	return new ArrayList<XMLObject>();
    }

}