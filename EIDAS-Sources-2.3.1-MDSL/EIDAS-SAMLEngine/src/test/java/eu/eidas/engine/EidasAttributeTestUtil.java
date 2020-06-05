/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.engine;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;

/**
 * EidasAttributeTestUtil
 *
 * @since 1.1
 */
public class EidasAttributeTestUtil {

    public static AttributeDefinition<String> newEidasAttributeDefinition(String canoniclaName,
                                                                  String friendlyName,
                                                                  boolean required) {
        return newEidasAttributeDefinition(canoniclaName, friendlyName, required, false, false);
    }

    public static AttributeDefinition<String> newEidasAttributeDefinition(String canoniclaName,
                                                                  String friendlyName,
                                                                  boolean required,
                                                                  boolean uniqueIdentifier,
                                                                  boolean transliterationMandatory) {
        return new AttributeDefinition.Builder<String>()
                .nameUri(NaturalPersonSpec.Namespace.URI + "/" + canoniclaName)
                .friendlyName(friendlyName)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", canoniclaName + "Type", "eidas-natural")
                .required(required)
                .uniqueIdentifier(uniqueIdentifier)
                .transliterationMandatory(transliterationMandatory)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

    public static AttributeDefinition<String> newAttributeDefinition(String fullName,
                                                                  String friendlyName,
                                                                  boolean required) {
        return new AttributeDefinition.Builder<String>().nameUri(fullName)
                .friendlyName(friendlyName)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", "string", "eidas-natural")
                .required(required)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

}
