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

package eu.eidas.auth.engine.core.eidas;


import org.opensaml.saml.common.SAMLObject;

import javax.xml.namespace.QName;

//CAVEAT: this interface and the implmenting classes should be dropped when upgrading to opensaml 3

public interface DigestMethod extends SAMLObject {
    String ALG_SUPPORT_NS="urn:oasis:names:tc:SAML:metadata:algsupport";
    String ALG_SUPPORT_PREFIX="alg";
    /** The Constant DEFAULT_ELEMENT_LOCAL_NAME. */
    String DEF_LOCAL_NAME = "DigestMethod";

    /** The Constant DEFAULT_ELEMENT_NAME. */
    QName DEF_ELEMENT_NAME = new QName(ALG_SUPPORT_NS, DEF_LOCAL_NAME,ALG_SUPPORT_PREFIX);

    /** The Constant TYPE_LOCAL_NAME. */
    String TYPE_LOCAL_NAME = "DigestMethod";

    /** The Constant TYPE_NAME. */
    QName TYPE_NAME = new QName(ALG_SUPPORT_NS, TYPE_LOCAL_NAME,ALG_SUPPORT_PREFIX);

    /**
     * Gets the algorithm
     *
     * @return the algorithm
     */
    String getAlgorithm();

    /**
     * Sets the algorithm.
     *
     * @param algorithm the new algorithm
     */
    void setAlgorithm(String algorithm);
}
