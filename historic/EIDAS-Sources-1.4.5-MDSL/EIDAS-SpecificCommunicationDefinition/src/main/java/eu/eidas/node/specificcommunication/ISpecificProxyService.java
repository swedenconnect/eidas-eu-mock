/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.specificcommunication;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.specificcommunication.protocol.IResponseCallbackHandler;

/**
 * This interface defines the API of the specific protocol at the ProxyService side.
 * <p/>
 * The specific protocol consists in being able to send a request to a third-party and asynchronously call back eIDAS
 * with a response.
 *
 * @since 1.1
 */
public interface ISpecificProxyService {

    /**
     * Called by the eIDAS Node (when receiving http request from the identity Provider) to process an http response and transform it in LightResponse {see @ILightResponse}.
     * <p/>
     * As the parameters names are not known, the transformation of the Http request parameters into a LightResponse is delegated to the specific proxy service module.
     *
     * @param httpServletRequest  an HttpServletRequest object that contains the request the client has made of the servlet,
     * @param httpServletResponse an HttpServletResponse object that contains the response the servlet sends to the client
     * @return the LightResponse constructed
     * @throws ServletException if the request for the GET could not be handled
     */
    ILightResponse processResponse(@Nonnull HttpServletRequest httpServletRequest,
                                   @Nonnull HttpServletResponse httpServletResponse) throws SpecificException;

    /**
     * Called by the eIDAS Node (when receiving the SAMLRequest) to transform the LightRequest {see @ILightRequest} and forward it to the Identity Provider.
     * <p/>
     * As the parameters names and form are only known by the specific module, the transformation of the LightRequest to the Ms specific protocol is delegated to this module
     *
     * @param lightRequest        the request to transform
     * @param httpServletRequest  an HttpServletRequest object that contains the request the client has made of the servlet,
     * @param httpServletResponse an HttpServletResponse object that contains the response the servlet sends to the client
     * @throws ServletException if the request for the GET could not be handled
     */
    void sendRequest(@Nonnull ILightRequest lightRequest, @Nonnull HttpServletRequest httpServletRequest,
                     @Nonnull HttpServletResponse httpServletResponse) throws SpecificException;

    /**
     * Register a Response Call back handler.
     *
     * @param responseCallbackHandler the call back handler to register
     * @throws ServletException if the request for the GET could not be handled
     */
    void setResponseCallbackHandler(@Nonnull IResponseCallbackHandler responseCallbackHandler) throws SpecificException;
}
