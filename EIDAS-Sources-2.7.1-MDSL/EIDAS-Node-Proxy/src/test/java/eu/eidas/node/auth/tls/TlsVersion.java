/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.node.auth.tls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum TlsVersion {
    TLS1_1("TLSv1.1"),TLS1_2("TLSv1.2");

    public static List<String> toStringList(Collection<TlsVersion> tlsVersions) {
        List<String> result = new ArrayList<>();
        for (TlsVersion tlsVersion : tlsVersions) {
            result.add(tlsVersion.getVersion());
        }
        return result;
    }

    public static String[] toStringArray(Collection<TlsVersion> tlsVersions) {
        String[] result = new String[tlsVersions.size()];
        int ndx = 0;
        for (TlsVersion tlsVersion : tlsVersions) {
            result[ndx++] = tlsVersion.getVersion();
        }
        return result;
    }

    private final String version;

    TlsVersion (String version) {
        this.version = version;
    }

    private String getVersion () {
        return version;
    }
}
