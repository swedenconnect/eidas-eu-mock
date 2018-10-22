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
package eu.eidas.auth.commons.lang;

import javax.annotation.Nonnull;

/**
 * Functional interface to retrieve a key {@code K} from an enum {@code E}.
 * <p/>
 * The key is typically used to build a mapping between keys and enum constants.
 *
 * @param <K> the type of the key obtained from the enum values
 * @param <E> the type of the enum
 * @since 1.1
 */
public interface KeyAccessor<K, E extends Enum<E>> {

    /**
     * Retrieves a key {@code K} from an enum {@code E}.
     * <p/>
     * The key is typically used to build a mapping between keys and enum constants.
     *
     * @param e the enum constant.
     * @return the key matching the given enum constant.
     */
    @Nonnull
    K getKey(@Nonnull E e);
}
