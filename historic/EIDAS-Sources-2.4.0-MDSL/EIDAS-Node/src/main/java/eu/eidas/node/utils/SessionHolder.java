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

package eu.eidas.node.utils;

import javax.servlet.http.HttpSession;

/**
 * thread local session holder
 */
public class SessionHolder {

        private static final ThreadLocal<HttpSession> sessionHolderMap = new ThreadLocal<HttpSession>();

        private SessionHolder() {
        }

        public static void setId(HttpSession identifier) {
            if (null == identifier) {
                //throw some exception
            }
            sessionHolderMap.set(identifier);
        }

        public static HttpSession getId() {
            return sessionHolderMap.get();
        }

        public static void clear() {
            sessionHolderMap.remove();
        }

}
