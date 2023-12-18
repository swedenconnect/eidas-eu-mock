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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

final class GenericArrayTypeImpl implements GenericArrayType {

    private Type componentType;

    static Class<?> createArrayType(Class<?> componentType) {
        // there's no (clean) other way to create a array class, than creating an instance of it
        return Array.newInstance(componentType, 0).getClass();
    }

    static Type createArrayType(Type componentType) {
        if (componentType instanceof Class) {
            return createArrayType((Class<?>) componentType);
        } else {
            return new GenericArrayTypeImpl(componentType);
        }
    }

    private GenericArrayTypeImpl(Type componentType) {
        super();
        this.componentType = componentType;
    }

    public Type getGenericComponentType() {
        return componentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GenericArrayType)) {
            return false;
        }
        return componentType.equals(((GenericArrayType) obj).getGenericComponentType());
    }

    @Override
    public int hashCode() {
        return componentType.hashCode() * 7;
    }

    @Override
    public String toString() {
        return componentType + "[]";
    }
}
