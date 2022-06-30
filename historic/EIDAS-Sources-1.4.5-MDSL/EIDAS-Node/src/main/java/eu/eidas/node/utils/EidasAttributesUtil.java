package eu.eidas.node.utils;

import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;

public final class EidasAttributesUtil {

    /**
     * TODO: use {@link LevelOfAssurance} instead
     *
     * @deprecated use {@link LevelOfAssurance} instead
     */
    @Deprecated
    public static String getUserFriendlyLoa(final String requestLoa) {
        if (requestLoa != null) {
            int lastIndex = requestLoa.lastIndexOf('/');
            if (lastIndex > 0) {
                return requestLoa.substring(lastIndex + 1);
            } else {
                return requestLoa;
            }
        }
        return null;
    }

    private EidasAttributesUtil() {
    }
}
