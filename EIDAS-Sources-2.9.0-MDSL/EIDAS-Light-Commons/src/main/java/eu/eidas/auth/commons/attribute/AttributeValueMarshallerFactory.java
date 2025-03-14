/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.attribute.impl.reflect.GenericTypeReflector;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A static factory responsible for instantiating {@link AttributeValueMarshaller} objects.
 *
 * @since 1.1
 */
final class AttributeValueMarshallerFactory {

    @Nullable
    static Class<?> findParameterizedType(@Nonnull Class<?> implementingClass,
                                          @Nonnull Class<?> genericInterfaceClass) {
        return findParameterizedTypeWithGentyref(implementingClass, genericInterfaceClass);
    }

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
