package eu.eidas.node.auth.tls;

import java.util.HashMap;
import java.util.Map;

public enum JdkVersion {
    JDK_7("1.7"),JDK_8("1.8");

    private static final Map<String, JdkVersion> lookup = new HashMap<>();
    public static JdkVersion lookup (String javaVersion) {
        return lookup.get(javaVersion);
    }

    static {
        //Create reverse lookup hash map
        for (JdkVersion jdkVersion : JdkVersion.values())
            lookup.put(jdkVersion.getVersion(), jdkVersion);
    }

    private String version;

    JdkVersion (String version) {
        this.version = version;
    }

    private String getVersion () {
        return version;
    }
}
