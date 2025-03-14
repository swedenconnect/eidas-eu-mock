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
package eu.eidas.auth.commons;

import javax.servlet.http.HttpServletRequest;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;

/**
 * handler for specific actions for a country
 */
public abstract class CountrySpecificService {
    private boolean active=false;
    public static final String SAML_RESPONSE_ERROR="SAML_RESPONSE_ERROR";
    protected CountrySpecificService() {
    }

    /**
     *
     * @return the ISO code of the country to be handled by this specific service
     */
    public abstract String getIsoCode();

    /**
     *
     * @param req the instance of {@link HttpServletRequest}
     * @param authData the instance of {@link IAuthenticationRequest}
     * enrich the request with parameters needed by the country web component (included in eDAIS Node)
     */
    public abstract void prepareRequest(HttpServletRequest req, IAuthenticationRequest authData);

    /**
     *
     * @param req the instance of {@link HttpServletRequest}
     * @return the ursl of the country's web component
     */
    public abstract String getRedirectUrl(HttpServletRequest req);

    /**
     *
     * @param req the instance of {@link HttpServletRequest}
     * @return true when the current req contains a response belonging to the current country
     */
    public abstract boolean isCountryResponse(HttpServletRequest req);

    /**
     *
     * @param req the instance of {@link HttpServletRequest}
     * @return true if the request should be allowed through the security filters
     */
    public boolean allowRequestThroughFilter(HttpServletRequest req){
        String country=req.getParameter("country");
        return country!=null && country.equalsIgnoreCase(getIsoCode());
    }

    public final boolean isActive(){
        return active;
    }
    public final void setActive(boolean activeArg){
        this.active=activeArg;
    }
}
