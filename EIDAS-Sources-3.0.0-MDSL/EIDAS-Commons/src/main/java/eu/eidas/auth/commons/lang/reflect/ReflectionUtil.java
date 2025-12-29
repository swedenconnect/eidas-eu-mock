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
package eu.eidas.auth.commons.lang.reflect;

import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for Reflection.
 *
 * @since 1.1
 */
public final class ReflectionUtil {

    private static void addImplementedInterfaces(@Nonnull Set<Class<?>> implementedInterfaces,
                                                 @Nonnull Class<?> someClass) {
        if (someClass.isInterface()) {
            implementedInterfaces.add(someClass);
        }
        Class<?>[] interfaces = someClass.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            addImplementedInterfaces(implementedInterfaces, anInterface);
        }
    }

    /**
     * Returns a Set of all the interfaces implemented by the given class.
     *
     * @param aClass the class to examine
     * @return a Set containing all the interfaces implemented by the given class.
     */
    @Nonnull
    public static Set<Class<?>> getImplementedInterfaces(@Nonnull Class<?> aClass) {
        Set<Class<?>> implementedInterfaces = new HashSet<Class<?>>();
        for (Class<?> superClass = aClass; superClass != null; superClass = superClass.getSuperclass()) {
            addImplementedInterfaces(implementedInterfaces, superClass);
        }
        return implementedInterfaces;
    }

    /**
     * Returns an array of all the interfaces implemented by the given class.
     *
     * @param aClass the class to examine
     * @return an array containing all the interfaces implemented by the given class.
     */
    @Nonnull
    public static Class<?>[] getImplementedInterfacesAsArray(@Nonnull Class<?> aClass) {
        Set<Class<?>> implementedInterfaceSet = getImplementedInterfaces(aClass);
        return implementedInterfaceSet.toArray(new Class<?>[implementedInterfaceSet.size()]);
    }

    @Nonnull
    public static <T> T newProxyInstance(@Nonnull ClassLoader contextClassLoader,
                                         @Nonnull Class<T> interfaceType,
                                         @Nonnull Class<? extends T> concreteImplementation,
                                         @Nonnull InvocationHandler invocationHandler) {
        Preconditions.checkNotNull(contextClassLoader, "contextClassLoader");
        Preconditions.checkNotNull(interfaceType, "interfaceType");
        Preconditions.checkNotNull(concreteImplementation, "concreteImplementation");
        Preconditions.checkNotNull(invocationHandler, "invocationHandler");

        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("Class: " + interfaceType + " is not an interface");
        }

        if (!interfaceType.isAssignableFrom(concreteImplementation)) {
            throw new IllegalArgumentException(
                    "Interface: " + interfaceType + " is not assignable to concrete class: " + concreteImplementation);
        }

        Class<?>[] implementedInterfaces = ReflectionUtil.getImplementedInterfacesAsArray(concreteImplementation);

        return newProxyInstance(contextClassLoader, interfaceType, invocationHandler, implementedInterfaces);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(@Nonnull ClassLoader contextClassLoader,
                                         @Nonnull Class<T> interfaceType,
                                         @Nonnull InvocationHandler invocationHandler,
                                         @Nonnull Class<?>... additionalInterfaces) {
        Preconditions.checkNotNull(contextClassLoader, "contextClassLoader");
        Preconditions.checkNotNull(interfaceType, "interfaceType");
        Preconditions.checkNotNull(invocationHandler, "invocationHandler");
        Preconditions.checkNotNull(additionalInterfaces, "additionalInterfaces");

        return (T) Proxy.newProxyInstance(contextClassLoader, toArray(interfaceType, additionalInterfaces),
                                          invocationHandler);
    }

    @Nonnull
    private static <T> Class<?>[] toArray(@Nonnull Class<T> interfaceType, @Nonnull Class<?>... additionalInterfaces) {
        Preconditions.checkNotNull(interfaceType, "interfaceType");
        Preconditions.checkNotNull(additionalInterfaces, "additionalInterfaces");

        // remove duplicates:
        Set<Class<?>> classes = new HashSet<>();
        classes.add(interfaceType);
        Collections.addAll(classes, additionalInterfaces);
        return classes.toArray(new Class<?>[classes.size()]);
    }

    private ReflectionUtil() {
    }
}
