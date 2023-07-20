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
package eu.eidas.auth.engine.core.eidas;

import javax.xml.namespace.QName;

import eu.eidas.auth.engine.core.SAMLCore;

/**
 * The Interface RequestedAttribute.
 *
 */
public interface RequestedAttribute extends eu.eidas.auth.engine.core.RequestedAttribute {


    /** Default element name. */
    QName DEF_ELEMENT_NAME = new QName(SAMLCore.EIDAS10_NS.getValue(), DEF_LOCAL_NAME,
	    SAMLCore.EIDAS10_PREFIX.getValue());

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(SAMLCore.EIDAS10_NS.getValue(), TYPE_LOCAL_NAME,
	    SAMLCore.EIDAS10_PREFIX.getValue());


}
