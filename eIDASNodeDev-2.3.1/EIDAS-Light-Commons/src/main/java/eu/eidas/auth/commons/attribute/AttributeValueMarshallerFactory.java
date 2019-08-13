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
package eu.eidas.auth.commons.attribute;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.TypeToken;

import eu.eidas.auth.commons.attribute.impl.reflect.GenericTypeReflector;
import eu.eidas.util.Preconditions;

/**
 * A static factory responsible for instantiating {@link AttributeValueMarshaller} objects.
 *
 * @since 1.1
 */
final class AttributeValueMarshallerFactory {

    @VisibleForTesting
    @Nullable
    static Class<?> findParameterizedType(@Nonnull Class<?> implementingClass,
                                          @Nonnull Class<?> genericInterfaceClass) {
        return findParameterizedTypeWithGentyref(implementingClass, genericInterfaceClass);
    }

    @VisibleForTesting
    @Nullable
    static Class<?> findParameterizedTypeWithGentyref(@Nonnull Class<?> implementingClass,
                                                      @Nonnull Class<?> genericInterfaceClass) {
        Type exactSuperType = GenericTypeReflector.getExactSuperType(implementingClass, genericInterfaceClass);

        if (exactSuperType instanceof Class<?>) {
            return (Class<?>) exactSuperType;
        }

        if (exactSuperType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) exactSuperType).getActualTypeArguments()[0];
        }
        return null;
    }

    @VisibleForTesting
    @Nullable
    static Class<?> findParameterizedTypeWithGuava(@Nonnull Class<?> implementingClass,
                                                   @Nonnull Class<?> genericInterfaceClass) {
        // See https://github.com/google/guava/wiki/ReflectionExplained
        Type exactSuperType =
                TypeToken.of(implementingClass).resolveType(genericInterfaceClass.getTypeParameters()[0]).getType();

        if (exactSuperType instanceof Class<?>) {
            return (Class<?>) exactSuperType;
        }

        if (exactSuperType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) exactSuperType).getActualTypeArguments()[0];
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T> getParameterizedType(@Nonnull AttributeValueMarshaller<T> marshaller) {
        Preconditions.checkNotNull(marshaller, "marshaller");
        Class<? extends AttributeValueMarshaller<T>> marshallerClass =
                (Class<? extends AttributeValueMarshaller<T>>) marshaller.getClass();

        Class<T> parameterizedType = (Class<T>) findParameterizedType(marshallerClass, AttributeValueMarshaller.class);

        if (null != parameterizedType) {
            return parameterizedType;
        }

        throw new IllegalStateException(
                "Unable to find the ParameterizedType of marshallerClass: \"" + marshallerClass.getName() + "\"");
    }

    static AttributeValueMarshaller<?> newAttributeValueMarshallerInstance(
            @Nonnull String attributeValueMarshallerClassName)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Preconditions.checkNotBlank(attributeValueMarshallerClassName, "attributeValueMarshallerClassName");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        @SuppressWarnings("unchecked") Class<AttributeValueMarshaller<?>> attributeValueMarshallerClass =
                (Class<AttributeValueMarshaller<?>>) Class.forName(attributeValueMarshallerClassName, true,
                                                                   contextClassLoader);
        return attributeValueMarshallerClass.newInstance();
    }

    private AttributeValueMarshallerFactory() {
    }
}
