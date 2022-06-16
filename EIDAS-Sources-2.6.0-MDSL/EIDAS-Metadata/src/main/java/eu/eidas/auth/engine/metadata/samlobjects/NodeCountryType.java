/*
 * Copyright (c) 2020 by European Commission
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

import eu.eidas.auth.engine.metadata.SAMLMetadataCore;
import org.opensaml.saml.common.SAMLObject;

import javax.xml.namespace.QName;

/**
 * The Interface SPType. - @see "eidas:NodeCountry md:Extensions" element of SAML metadata.
 *
 * The value of the @see "eidas:NodeCountry" element MUST be the Nationality Code of the SP country or international
 * organization in ISO 3166-1 alpha-2 format
 */
public interface NodeCountryType extends SAMLObject {

    /** The Constant DEFAULT_ELEMENT_LOCAL_NAME. */
    String DEF_LOCAL_NAME = "NodeCountry";

    /** The Constant DEFAULT_ELEMENT_NAME. */
    QName DEF_ELEMENT_NAME = new QName(SAMLMetadataCore.EIDAS10_SAML_NS.getValue(), DEF_LOCAL_NAME,
            SAMLMetadataCore.EIDAS10_SAML_PREFIX.getValue());

    /** The Constant TYPE_LOCAL_NAME. */
    String TYPE_LOCAL_NAME = "NodeCountryType";

    /** The Constant TYPE_NAME. */
    QName TYPE_NAME = new QName(SAMLMetadataCore.EIDAS10_SAML_NS.getValue(), TYPE_LOCAL_NAME,
            SAMLMetadataCore.EIDAS10_SAML_NS.getValue());

    /**
     * Gets the node country value.
     *
     * @return the node country
     */
    String getNodeCountry();

    /**
     * Sets the node country type.
     *
     * @param nodeCountry the new nodeCountry value
     */
    void setNodeCountry(String nodeCountry);
}
