package eu.eidas.auth.engine.configuration.dom;

import java.util.Map;

/**
 * Functional interface to convert any Map to a type T.
 *
 * @param <T> type to convert from the Map.
 * @since 1.1
 */
interface MapConverter<T> {

    T convert(Map<String, String> map);
}
