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

package eu.eidas.node.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Utility class used to define the Mapped Diagnostic Contexts and the markers used for logging.
 * Reference : A lighter technique consists of uniquely stamping each log request servicing a given client.
 * Neil Harrison described this method in the book Patterns for Logging Diagnostic Messages in Pattern Languages of Program Design 3,
 * edited by R. Martin, D. Riehle, and F. Buschmann (Addison-Wesley, 1997). Logback leverages a variant of this technique included in the SLF4J API:
 * Mapped Diagnostic Contexts (MDC).
 * @author vanegdi
 * @since 1.2.2
 */
public final class LoggingMarkerMDC {
    public static final String MDC_SESSIONID = "sessionId";
    public static final String MDC_REMOTE_HOST = "remoteHost";

    /*Contains all the markers related to the security log*/
    public static final Marker SECURITY_SUCCESS = MarkerFactory.getMarker("SECURITY_SUCCESS");
    public static final Marker SECURITY_WARNING = MarkerFactory.getMarker("SECURITY_WARNING");
    public static final Marker SECURITY_FAILURE = MarkerFactory.getMarker("SECURITY_FAILURE");

    /*Marker dedicated to system event log*/
    public static final Marker SYSTEM_EVENT = MarkerFactory.getMarker("SYSTEM");

    /*Dedicated marker for the web events*/
    public static final Marker WEB_EVENT = MarkerFactory.getMarker("WEB_EVENT");

    /*Dedicated marker for the web events*/
    public static final Marker SESSION_CONTENT = MarkerFactory.getMarker("SESSION_CONTENT");

    /*Dedicated marker for the SAML exchanges*/
    public static final Marker SAML_EXCHANGE = MarkerFactory.getMarker("SAML_EXCHANGE");
    public static final Marker CRYPTO = MarkerFactory.getMarker("CRYPTO");
    /**
     * private constructor
     */
    private LoggingMarkerMDC(){
    }
}
