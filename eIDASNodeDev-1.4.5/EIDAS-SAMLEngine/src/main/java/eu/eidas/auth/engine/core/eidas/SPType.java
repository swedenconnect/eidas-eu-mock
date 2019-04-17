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

package eu.eidas.auth.engine.core.eidas;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;

import eu.eidas.auth.engine.core.SAMLCore;

/**
 * The Interface SPType. - <eidas:SPType> MUST be present either in the <md:Extensions> element of SAML metadata or in the <saml2p:Extensions> element of a <saml2p:AuthnRequest>. The <eidas:SPType> element can contain the values "public" or "private" only
 */
public interface SPType extends SAMLObject {

    /** The Constant DEFAULT_ELEMENT_LOCAL_NAME. */
    String DEF_LOCAL_NAME = "SPType";

    /** The Constant DEFAULT_ELEMENT_NAME. */
    QName DEF_ELEMENT_NAME = new QName(SAMLCore.EIDAS10_SAML_NS.getValue(), DEF_LOCAL_NAME,
	    SAMLCore.EIDAS10_SAML_PREFIX.getValue());

    /** The Constant TYPE_LOCAL_NAME. */
    String TYPE_LOCAL_NAME = "SPType";

    /** The default value of SPType. */
    String DEFAULT_VALUE = "public";

    /** The Constant TYPE_NAME. */
    QName TYPE_NAME = new QName(SAMLCore.EIDAS10_SAML_NS.getValue(), TYPE_LOCAL_NAME,
	    SAMLCore.EIDAS10_SAML_NS.getValue());

    /**
     * Gets the sector type.
     *
     * @return the sector provider
     */
    String getSPType();

    /**
     * Sets the request's sector type.
     *
     * @param spType the new service type
     */
    void setSPType(String spType);
}
