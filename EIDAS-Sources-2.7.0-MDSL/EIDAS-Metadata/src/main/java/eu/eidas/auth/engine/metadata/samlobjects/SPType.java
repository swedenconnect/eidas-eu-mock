/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.engine.metadata.samlobjects;

import eu.eidas.auth.engine.metadata.SAMLMetadataCore;
import org.opensaml.saml.common.SAMLObject;

import javax.xml.namespace.QName;

/**
 * The Interface SPType. - @see "eidas:SPType md:Extensions" element of SAML metadata. The @see "eidas:SPType" element can contain the values "public" or "private" only
 */
public interface SPType extends SAMLObject {

    /** The Constant DEFAULT_ELEMENT_LOCAL_NAME. */
    String DEF_LOCAL_NAME = "SPType";

    /** The Constant DEFAULT_ELEMENT_NAME. */
    QName DEF_ELEMENT_NAME = new QName(SAMLMetadataCore.EIDAS10_SAML_NS.getValue(), DEF_LOCAL_NAME,
            SAMLMetadataCore.EIDAS10_SAML_PREFIX.getValue());

    /** The Constant TYPE_LOCAL_NAME. */
    String TYPE_LOCAL_NAME = "SPType";

    /** The default value of SPType. */
    String DEFAULT_VALUE = "public";

    /** The Constant TYPE_NAME. */
    QName TYPE_NAME = new QName(SAMLMetadataCore.EIDAS10_SAML_NS.getValue(), TYPE_LOCAL_NAME,
            SAMLMetadataCore.EIDAS10_SAML_NS.getValue());

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
