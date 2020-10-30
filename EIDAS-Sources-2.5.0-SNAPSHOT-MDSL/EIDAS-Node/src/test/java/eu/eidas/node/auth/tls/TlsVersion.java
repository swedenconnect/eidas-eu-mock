package eu.eidas.node.auth.tls;

import java.util.*;

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
