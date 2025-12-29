/*
 * Copyright (c) 2025 by European Commission
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
package eu.eidas.auth.commons.attribute.impl.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapping between type variables and actual parameters.
 */
final class VarMap {

    private final Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();

    /**
     * Creates an empty VarMap
     */
    VarMap() {
    }

    void addAll(TypeVariable<?>[] variables, Type[] values) {
        assert variables.length == values.length;
        for (int i = 0; i < variables.length; i++) {
            map.put(variables[i], values[i]);
        }
    }

    @SuppressWarnings("squid:S00112")
    Type map(Type type) {
        if (type instanceof Class) {
            return type;
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            if (!map.containsKey(type)) {
                throw new UnresolvedTypeVariableException(tv);
            }
            return map.get(type);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            return new ParameterizedTypeImpl((Class<?>) pType.getRawType(), map(pType.getActualTypeArguments()),
                    pType.getOwnerType() == null ? pType.getOwnerType()
                            : map(pType.getOwnerType()));
        } else if (type instanceof WildcardType) {
            WildcardType wType = (WildcardType) type;
            return new WildcardTypeImpl(map(wType.getUpperBounds()), map(wType.getLowerBounds()));
        } else if (type instanceof GenericArrayType) {
            return GenericArrayTypeImpl.createArrayType(map(((GenericArrayType) type).getGenericComponentType()));
        } else {
            throw new UnsupportedTypeException(type);
        }
    }

    Type[] map(Type[] types) {
        Type[] result = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = map(types[i]);
        }
        return result;
    }
}