/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.security;

import org.apache.commons.lang.reflect.FieldUtils;

public class ReflectionUtils {

    /**
     * Initializes static field on a class using reflection
     *
     * @param clazz:     The class under reflection
     * @param fieldName: The field, public or not, to be set
     * @param value:     the value being set
     */
    public static void setStaticField (Class clazz, String fieldName, Object value) {
        try {
            FieldUtils.writeStaticField(clazz, fieldName, value, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
