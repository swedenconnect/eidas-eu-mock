/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.auth.commons.protocol.eidas;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum to describe the different possibles technical specifications implementations of the eIDAS Protocol.
 */
public enum EidasProtocolVersion {

    PROTOCOL_VERSION_1_2 (1,2),
    PROTOCOL_VERSION_1_1 (1,1)
    ;

    /**
     * Search the Enum value of a technical specifications implementation
     * @param version the value of the searched protocol version.
     * @return the eIDAS Protocol version matching the given version or null if not found.
     */
    public static EidasProtocolVersion fromString(String version) {
        for (EidasProtocolVersion protocolVersion : EidasProtocolVersion.values()) {
            if (protocolVersion.toString().equals(version)) {
                return protocolVersion;
            }
        }
        return null;
    }

    /**
     * Search the Enum values of a technical specifications implementation
     * @param versions the list of values of the searched protocol versions.
     * @return the list of eIDAS Protocol version matching with the given versions or empty list if no match.
     */
    public static List<EidasProtocolVersion> fromString(List<String> versions) {
        List<EidasProtocolVersion> eidasProtocolVersions = new ArrayList<>();
        if (versions != null) {
            for (String protocolVersionAsString : versions) {
                EidasProtocolVersion eidasProtocolVersion = fromString(protocolVersionAsString);
                if (eidasProtocolVersion != null) {
                    eidasProtocolVersions.add(eidasProtocolVersion);
                }
            }
        }
        return eidasProtocolVersions;
    }

    /**
     * Get the highest protocol version from a list of protocol versions.
     * Highest protocol version is the protocol version having first a bigger or equal major version and then a bigger
     * minor version.
     * @param protocolVersionList the list of EidasProtocolVersions froms which to find the highest one.
     * @return the highest protocol version or null if list is null or empty.
     */
    public static EidasProtocolVersion getHighestProtocolVersion(List<EidasProtocolVersion> protocolVersionList) {
        if (protocolVersionList == null) {
            return null;
        }
        EidasProtocolVersion highestProtocolVersion = null;
        for (EidasProtocolVersion protocolVersion : protocolVersionList) {
            if (protocolVersion.isHigherProtocolVersion(highestProtocolVersion)) {
                highestProtocolVersion = protocolVersion;
            }
        }
        return highestProtocolVersion;
    }

    /**
     * Check if the current protocol version is bigger than the one in parameter
     * @param eidasProtocolVersion the other protocol version
     * @return true if the current protocol version is bigger or equally high and false otherwise
     */
    public boolean isHigherProtocolVersion(EidasProtocolVersion eidasProtocolVersion) {
        if (eidasProtocolVersion == null)  {
            return true;
        } else if ((this.majorVersion > eidasProtocolVersion.majorVersion)
                || (this.majorVersion == eidasProtocolVersion.majorVersion
                    && this.minorVersion >= eidasProtocolVersion.minorVersion)) {
            return true;
        } else {
            return false;
        }
    }

    private int majorVersion;
    private int minorVersion;

    EidasProtocolVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    @Override
    public String toString() {
        return majorVersion + "." + minorVersion;
    }

}
