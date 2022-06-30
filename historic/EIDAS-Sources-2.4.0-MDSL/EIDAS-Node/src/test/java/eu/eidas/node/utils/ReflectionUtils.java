package eu.eidas.node.utils;

import org.apache.commons.lang.reflect.FieldUtils;

public class ReflectionUtils {

    /**
     * Initializes static field on a class using reflection
     * @param clazz: The class under reflection
     * @param fieldName: The field, public or not, to be set
     * @param value: the value being set
     */
    public static void setStaticField (Class clazz, String fieldName, Object value) {
        try {
            FieldUtils.writeStaticField(clazz, fieldName, value, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
