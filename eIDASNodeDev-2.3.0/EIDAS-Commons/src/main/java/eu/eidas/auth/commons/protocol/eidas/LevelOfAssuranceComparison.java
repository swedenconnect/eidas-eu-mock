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

package eu.eidas.auth.commons.protocol.eidas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * As per the eIDAS spec, 2.4.1 SAML AuthnRequest:
 * <p>
 * <em>"eIDAS-Connectors requesting a LoA MUST limit the value of the Comparison attribute of
 * &lt;saml2p:RequestedAuthnContext&gt; to &quot;minimum&quot;."</em>
 */
public enum LevelOfAssuranceComparison {

    MINIMUM("minimum");

    @Nullable
    public static LevelOfAssuranceComparison fromString(@Nullable String val) {
        // No default value when not found
        return MINIMUM.stringValue().equals(val) ? MINIMUM : null;
    }

    @Nonnull
    private final transient String value;

    LevelOfAssuranceComparison(@Nonnull String value) {
        this.value = value;
    }

    @Nonnull
    public String stringValue() {
        return value;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Nonnull
    @Override
    public String toString() {
        return value;
    }
}
