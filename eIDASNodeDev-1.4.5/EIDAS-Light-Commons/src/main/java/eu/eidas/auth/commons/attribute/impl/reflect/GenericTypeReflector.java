package eu.eidas.auth.commons.attribute.impl.reflect;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Utility class for doing reflection on types.
 *
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
public final class GenericTypeReflector {

    private GenericTypeReflector() {
    }

    /**
     * Returns the erasure of the given type.
     */
    @SuppressWarnings("squid:S00112")
    private static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            if (tv.getBounds().length == 0) {
                return Object.class;
            } else {
                return erase(tv.getBounds()[0]);
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType aType = (GenericArrayType) type;
            return GenericArrayTypeImpl.createArrayType(erase(aType.getGenericComponentType()));
        } else {
            // TODO at least support CaptureType here
            throw new RuntimeException("not supported: " + type.getClass());
        }
    }

    /**
     * Maps type parameters in a type to their values.
     *
     * @param toMapType Type possibly containing type arguments
     * @param typeAndParams must be either ParameterizedType, or (in case there are no type arguments, or it's a raw
     * type) Class
     * @return toMapType, but with type parameters from typeAndParams replaced.
     */
    private static Type mapTypeParameters(Type toMapType, Type typeAndParams) {
        if (isMissingTypeParameters(typeAndParams)) {
            return erase(toMapType);
        } else {
            VarMap varMap = new VarMap();
            Type handlingTypeAndParams = typeAndParams;
            while (handlingTypeAndParams instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) handlingTypeAndParams;
                Class<?> clazz = (Class<?>) pType.getRawType(); // getRawType should always be Class
                varMap.addAll(clazz.getTypeParameters(), pType.getActualTypeArguments());
                handlingTypeAndParams = pType.getOwnerType();
            }
            return varMap.map(toMapType);
        }
    }

    /**
     * Checks if the given type is a class that is supposed to have type parameters, but doesn't. In other words, if
     * it's a really raw type.
     */
    private static boolean isMissingTypeParameters(Type type) {
        if (type instanceof Class) {
            for (Class<?> clazz = (Class<?>) type; clazz != null; clazz = clazz.getEnclosingClass()) {
                if (clazz.getTypeParameters().length != 0) {
                    return true;
                }
            }
            return false;
        } else if (type instanceof ParameterizedType) {
            return false;
        } else {
            throw new AssertionError("Unexpected type " + type.getClass());
        }
    }

    /**
     * Finds the most specific supertype of <tt>type</tt> whose erasure is <tt>searchClass</tt>. In other words, returns
     * a type representing the class <tt>searchClass</tt> plus its exact type parameters in <tt>type</tt>. <p> <ul>
     * <li>Returns an instance of {@link ParameterizedType} if <tt>searchClass</tt> is a real class or interface and
     * <tt>type</tt> has parameters for it</li> <li>Returns an instance of {@link GenericArrayType} if
     * <tt>searchClass</tt> is an array type, and <tt>type</tt> has type parameters for it</li> <li>Returns an instance
     * of {@link Class} if <tt>type</tt> is a raw type, or has no type parameters for <tt>searchClass</tt></li>
     * <li>Returns null if <tt>searchClass</tt> is not a superclass of type.</li> </ul> <p> <p>For example, with
     * <tt>class StringList implements List&lt;String&gt;</tt>, <tt>getExactSuperType(StringList.class,
     * Collection.class)</tt> returns a {@link ParameterizedType} representing <tt>Collection&lt;String&gt;</tt>. </p>
     */
    public static Type getExactSuperType(Type type, Class<?> searchClass) {
        if (type instanceof ParameterizedType || type instanceof Class || type instanceof GenericArrayType) {
            Class<?> clazz = erase(type);

            if (searchClass == clazz) {
                return type;
            }

            if (!searchClass.isAssignableFrom(clazz)) {
                return null;
            }
        }

        for (Type superType : getExactDirectSuperTypes(type)) {
            Type result = getExactSuperType(superType, searchClass);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Returns the direct supertypes of the given type. Resolves type parameters.
     */
    @SuppressWarnings("squid:S00112")
    private static Type[] getExactDirectSuperTypes(Type type) {
        if (type instanceof ParameterizedType || type instanceof Class) {
            Class<?> clazz;
            if (type instanceof ParameterizedType) {
                clazz = (Class<?>) ((ParameterizedType) type).getRawType();
            } else {
                // TODO primitive types?
                clazz = (Class<?>) type;
                if (clazz.isArray()) {
                    return getArrayExactDirectSuperTypes(clazz);
                }
            }

            Type[] superInterfaces = clazz.getGenericInterfaces();
            Type superClass = clazz.getGenericSuperclass();

            // the only supertype of an interface without superinterfaces is Object
            if (superClass == null && superInterfaces.length == 0 && clazz.isInterface()) {
                return new Type[] {Object.class};
            }

            Type[] result;
            int resultIndex;
            if (superClass == null) {
                result = new Type[superInterfaces.length];
                resultIndex = 0;
            } else {
                result = new Type[superInterfaces.length + 1];
                resultIndex = 1;
                result[0] = mapTypeParameters(superClass, type);
            }
            for (Type superInterface : superInterfaces) {
                result[resultIndex++] = mapTypeParameters(superInterface, type);
            }

            return result;
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            return tv.getBounds();
        } else if (type instanceof WildcardType) {
            // This should be a rare case: normally this wildcard is already captured.
            // But it does happen if the upper bound of a type variable contains a wildcard
            // TODO shouldn't upper bound of type variable have been captured too? (making this case impossible?)
            return ((WildcardType) type).getUpperBounds();
        } else if (type instanceof GenericArrayType) {
            return getArrayExactDirectSuperTypes(type);
        } else if (type == null) {
            throw new NullPointerException();
        } else {
            //TODO throw other exception
            throw new RuntimeException("not implemented type: " + type);
        }
    }

    /**
     * If type is an array type, returns the type of the component of the array. Otherwise, returns null.
     */
    private static Type getArrayComponentType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return clazz.getComponentType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType aType = (GenericArrayType) type;
            return aType.getGenericComponentType();
        } else {
            return null;
        }
    }

    private static Type[] getArrayExactDirectSuperTypes(Type arrayType) {
        // see http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.10.3
        Type typeComponent = getArrayComponentType(arrayType);

        Type[] result;
        int resultIndex;
        if (typeComponent instanceof Class && ((Class<?>) typeComponent).isPrimitive()) {
            resultIndex = 0;
            result = new Type[3];
        } else {
            Type[] componentSupertypes = getExactDirectSuperTypes(typeComponent);
            result = new Type[componentSupertypes.length + 3];
            for (resultIndex = 0; resultIndex < componentSupertypes.length; resultIndex++) {
                result[resultIndex] = GenericArrayTypeImpl.createArrayType(componentSupertypes[resultIndex]);
            }
        }
        result[resultIndex++] = Object.class;
        result[resultIndex++] = Cloneable.class;
        result[resultIndex] = Serializable.class;
        return result;
    }

    /**
     * Returns the display name of a Type.
     */
    static String getTypeName(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return clazz.isArray() ? (getTypeName(clazz.getComponentType()) + "[]") : clazz.getName();
        } else {
            return type.toString();
        }
    }

}
