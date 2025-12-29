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
package eu.eidas.auth.commons.attribute.impl.reflect;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;

final class UnresolvedTypeVariableException extends RuntimeException {

    UnresolvedTypeVariableException(TypeVariable<?> typeVariable) {
        super("An exact type is requested, but the type contains a type variable that cannot be resolved.\n" +
                      "   Variable: " + typeVariable.getName() + " from " + typeVariable.getGenericDeclaration() + "\n" +
                      "   Hint: This is usually caused by trying to get an exact type when a generic method who's type parameters are not given is involved.");
    }

}
