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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.specificcommunication.protocol.IRequestCallbackHandler;

/**
 * This interface defines the API of the specific protocol at the between the connector and the service provider.
 * <p/>
 * The specific protocol consists in being able to process a response from eIDAS and to asynchronously call back eIDAS
 * with incoming requests.
 *
 * @since 1.1
 */
public interface ISpecificConnector {

    /**
     * Called by the eIDAS Node (when receiving http request from the Service Provider) to process an http Request and transform it in LightRequest {see @ILightRequest}.
     * <p/>
     * As the parameters names are not known, the transformation of the Http request parameters into a LightRequest is delegated to the specific connector module.
     *
     * @param httpServletRequest  an HttpServletRequest object that contains the request the client has made of the servlet,
     * @param httpServletResponse an HttpServletResponse object that contains the response the servlet sends to the client
     * @return the http payload transformed into the LightRequest
     * @throws SpecificException in case of a business exception occurs
     */
    ILightRequest processRequest(@Nonnull HttpServletRequest httpServletRequest,
                                 @Nonnull HttpServletResponse httpServletResponse) throws SpecificException;

    /**
     * Called by the eIDAS Node (when receiving the SAMLResponse) to transform the LightResponse {see @ILightResponse} and send it back to the Service Provider.
     * <p/>
     * As the parameters names and form are only known by the specific module, the transformation of the LightResponse to MS specific format is delegated to this module
     *
     * @param eidasResponse       the lightResponse to transform
     * @param httpServletRequest  an HttpServletRequest object that contains the request the client has made of the servlet,
     * @param httpServletResponse an HttpServletResponse object that contains the response the servlet sends to the client
     * @throws SpecificException in case of a business exception occurs
     */
    void sendResponse(@Nonnull ILightResponse eidasResponse,
                      @Nonnull HttpServletRequest httpServletRequest,
                      @Nonnull HttpServletResponse httpServletResponse) throws SpecificException;

    /**
     * Register a request callBackHandler.
     *
     * @param requestCallbackHandler the call back handler to register
     * @throws SpecificException in case of a business exception occurs
     */
    void setRequestCallbackHandler(@Nonnull IRequestCallbackHandler requestCallbackHandler) throws SpecificException;
}
