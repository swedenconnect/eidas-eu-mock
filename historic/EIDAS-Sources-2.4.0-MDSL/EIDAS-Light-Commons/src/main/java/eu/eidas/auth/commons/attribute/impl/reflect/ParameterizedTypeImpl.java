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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

final class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;

    private final Type[] actualTypeArguments;

    private final Type ownerType;

    ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
    }

    public Type getRawType() {
        return rawType;
    }

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType other = (ParameterizedType) obj;
        return rawType.equals(other.getRawType()) && Arrays.equals(actualTypeArguments, other.getActualTypeArguments())
                && (ownerType == null ? other.getOwnerType() == null : ownerType.equals(other.getOwnerType()));
    }

    @Override
    public int hashCode() {
        int result = rawType.hashCode() ^ Arrays.hashCode(actualTypeArguments);
        if (ownerType != null) {
            result ^= ownerType.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String clazz = rawType.getName();

        if (ownerType != null) {
            sb.append(GenericTypeReflector.getTypeName(ownerType)).append('.');

            String prefix = (ownerType instanceof ParameterizedType) ?
                            ((Class<?>) ((ParameterizedType) ownerType).getRawType()).getName() + '$'
                                                                     : ((Class<?>) ownerType).getName() + '$';
            if (clazz.startsWith(prefix)) {
                clazz = clazz.substring(prefix.length());
            }
        }
        sb.append(clazz);

        if (actualTypeArguments.length != 0) {
            sb.append('<');
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type arg = actualTypeArguments[i];
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(GenericTypeReflector.getTypeName(arg));
            }
            sb.append('>');
        }

        return sb.toString();
    }
}
