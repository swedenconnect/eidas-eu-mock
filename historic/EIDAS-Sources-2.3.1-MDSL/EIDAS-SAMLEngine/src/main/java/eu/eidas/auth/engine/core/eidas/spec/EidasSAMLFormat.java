/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.engine.core.eidas.spec;

import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SAMLExtensionFormat;

import javax.annotation.Nonnull;

/** since 2.0.0 */
public class EidasSAMLFormat implements SAMLExtensionFormat {

    @Nonnull
    private final transient String name = "eidas";

    @Nonnull
    private final transient String assertionNS =  SAMLCore.EIDAS10_NS.getValue();

    @Nonnull
    private final transient String assertionPrefix = SAMLCore.EIDAS10_PREFIX.getValue();

    @Nonnull
    private final transient String protocolNS = SAMLCore.EIDAS10_NS.getValue();

    @Nonnull
    private final transient String protocolPrefix = SAMLCore.EIDAS10_PREFIX.getValue();

    @Nonnull
    public String getAssertionNS() {
        return assertionNS;
    }

    @Nonnull
    public String getAssertionPrefix() {
        return assertionPrefix;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getProtocolNS() {
        return protocolNS;
    }

    @Nonnull
    public String getProtocolPrefix() {
        return protocolPrefix;
    }

    @Override
    public String toString() {
        return "SAMLExtensionFormat{" +
                "name='" + name + '\'' +
                ", assertionNS='" + assertionNS + '\'' +
                ", assertionPrefix='" + assertionPrefix + '\'' +
                ", protocolNS='" + protocolNS + '\'' +
                ", protocolPrefix='" + protocolPrefix + '\'' +
                '}';
    }

}
