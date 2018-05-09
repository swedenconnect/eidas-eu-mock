/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core;

import javax.annotation.Nonnull;

/** since 2.0.0 */
public interface SAMLExtensionFormat {

    @Nonnull public String getAssertionNS();
    @Nonnull public String getAssertionPrefix();
    @Nonnull public String getName();
    @Nonnull public String getProtocolNS();
    @Nonnull public String getProtocolPrefix();
    public String toString();
}
