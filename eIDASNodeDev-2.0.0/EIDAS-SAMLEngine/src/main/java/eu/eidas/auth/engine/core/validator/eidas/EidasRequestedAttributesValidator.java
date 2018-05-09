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

package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.engine.core.eidas.RequestedAttribute;
import eu.eidas.auth.engine.core.eidas.RequestedAttributes;
import eu.eidas.engine.exceptions.ValidationException;

import java.util.List;


public class EidasRequestedAttributesValidator implements
        Validator<RequestedAttributes> {

    public EidasRequestedAttributesValidator() {

    }

    public void validate(RequestedAttributes attrs) throws ValidationException {
        EidasRequestedAttributeValidator valRequestedAttribute = new EidasRequestedAttributeValidator();

        List<RequestedAttribute> requestedAttributeList = attrs.getAttributes();

        for (RequestedAttribute attribute : requestedAttributeList) {
            valRequestedAttribute.validate(attribute);
        }
    }
}
