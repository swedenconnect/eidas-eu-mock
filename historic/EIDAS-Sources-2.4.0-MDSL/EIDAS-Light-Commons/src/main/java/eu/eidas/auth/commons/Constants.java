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
package eu.eidas.auth.commons;

import java.nio.charset.Charset;

/**
 * @deprecated since 1.1.1 use {@link eu.eidas.auth.commons.lang.Charsets} instead.
 */
@Deprecated
public final class Constants {

    /**
     * @deprecated since 1.1.1 use {@link eu.eidas.auth.commons.lang.Charsets} instead.
     */
    @Deprecated
    public static final String UTF8_ENCODING = "UTF-8";

    /**
     * @deprecated since 1.1.1 use {@link eu.eidas.auth.commons.lang.Charsets} instead.
     */
    @Deprecated
    public static final Charset UTF8 = Charset.forName(UTF8_ENCODING);

    private Constants() {
    }
}
