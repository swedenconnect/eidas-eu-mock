/*
 * Copyright (c) 2017 by European Commission
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

package member_country_specific.specific.proxyservice.utils;


import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceImpl;
import member_country_specific.specific.proxyservice.SpecificProxyServiceParameterNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceViewNames;

import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for preparing token redirect page fields.
 *
 * Puts the {@link ILightResponse} in the communication cache.
 *
 * @since 2.0
 */
public class TokenRedirectHelper {

    private TokenRedirectHelper() {}

    public static void setTokenRedirectAttributes(@Nonnull HttpServletRequest httpServletRequest, ILightResponse lightResponse, ApplicationContext applicationContext, String redirectUrl) throws SpecificCommunicationException {
        final SpecificProxyserviceCommunicationServiceImpl specificProxyserviceCommunicationService = (SpecificProxyserviceCommunicationServiceImpl) applicationContext.getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString());
        final BinaryLightToken binaryLightToken = specificProxyserviceCommunicationService.putResponse(lightResponse);
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        httpServletRequest.setAttribute(EidasParameterKeys.TOKEN.toString(), token);
        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.REDIRECT_URL.toString(), redirectUrl);
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());
    }
}
