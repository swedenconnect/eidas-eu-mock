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

package eu.eidas.auth.engine.core.stork;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;

import eu.eidas.auth.engine.core.SAMLCore;

/**
 * The Interface EIDCrossSectorShare.
 * @author fjquevedo
 */
public interface EIDCrossSectorShare extends SAMLObject {

    /** The Constant DEFAULT_ELEMENT_LOCAL_NAME. */
    String ELEM_LOCAL_NAME = "eIDCrossSectorShare";

    /** The Constant DEFAULT_ELEMENT_NAME. */
    QName DEF_ELEMENT_NAME = new QName(SAMLCore.STORK10P_NS.getValue(), ELEM_LOCAL_NAME,
	    SAMLCore.STORK10P_PREFIX.getValue());

    /** The Constant TYPE_LOCAL_NAME. */
    String TYPE_LOCAL_NAME = "eIDCrossSectorShareType";

    /** The Constant TYPE_NAME. */
    QName TYPE_NAME = new QName(SAMLCore.STORK10P_NS.getValue(), TYPE_LOCAL_NAME,
	    SAMLCore.STORK10P_PREFIX.getValue());

    /**
     * Gets the eID cross sector share.
     *
     * @return the eID cross sector share
     */
    String getEIDCrossSectorShare();

    /**
     * Sets the eID cross sector share.
     *
     * @param eIDCrossSectorShare the new eID cross sector share
     */
    void setEIDCrossSectorShare(String eIDCrossSectorShare);

}
