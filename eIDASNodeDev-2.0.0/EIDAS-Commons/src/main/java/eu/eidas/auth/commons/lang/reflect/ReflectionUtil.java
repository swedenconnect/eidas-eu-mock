/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.commons.lang.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.util.Preconditions;

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

    /**
     * Removes the {@code final} modifier on an {@code AccessibleObject} ie Field, Constructor or Method.
     *
     * @param accessibleObject the final accessibleObject which requires its final modifier to be removed.
     */
    public static void removeFinalModifier(@Nonnull AccessibleObject accessibleObject) {
        try {
            // reset the final fields:
            Field modifiersField = accessibleObject.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            int finalFieldModifiers = modifiersField.getInt(accessibleObject);
            // remove the final modifier:
            finalFieldModifiers &= ~Modifier.FINAL;
            modifiersField.setInt(accessibleObject, finalFieldModifiers);
        } catch (Exception ex) {
            final AssertionError assertionError =
                    new AssertionError("Accessing java.lang.reflect.Field#modifiers failed: " + ex);
            assertionError.initCause(ex);
            throw assertionError;
        }
    }

    /**
     * Modifies the value of a final field on an object.
     * <p/>
     * The final field can have any modifier including private or package-private.
     *
     * @param declaringClass the Class which declares the given final field.
     * @param instance the object containing the given final field, maybe null for a static field.
     * @param fieldName the name of the given final field
     * @param value the value to set to the given final field
     */
    public static <T> void setFinalFieldValue(@Nonnull Class<? super T> declaringClass,
                                              @Nullable T instance,
                                              @Nonnull String fieldName,
                                              @Nullable Object value) {
        try {
            Field finalField = declaringClass.getDeclaredField(fieldName);
            finalField.setAccessible(true);

            removeFinalModifier(finalField);

            finalField.set(instance, value);
        } catch (IllegalAccessException iae) {
            final AssertionError assertionError = new AssertionError("setAccessible(boolean) failed: " + iae);
            assertionError.initCause(iae);
            throw assertionError;
        } catch (NoSuchFieldException nsfe) {
            AssertionError assertionError = new AssertionError(
                    "The " + fieldName + " field does not exist in class " + declaringClass.getName() + ": " + nsfe);
            assertionError.initCause(nsfe);
            throw assertionError;
        }
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
