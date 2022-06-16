/*
 * Copyright (c) 2020 by European Commission
 *
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.specificcommunication.protocol.impl;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link AbstractSpecificCommunicationLoggingService} to be used for logging
 * {@link ILightRequest} and {@link ILightResponse} between the specific
 * ProxyService and node ProxyService
 *
 * @since 2.5
 */
public class SpecificProxyserviceCommunicationLoggerServiceImpl extends AbstractSpecificCommunicationLoggingService {

    SpecificProxyserviceCommunicationLoggerServiceImpl(final String lightTokenRequestSecret, final String lightTokenRequestAlgorithm,
                                                       final String lightTokenResponseSecret, final String lightTokenResponseAlgorithm) {
        super(lightTokenRequestSecret,lightTokenRequestAlgorithm,lightTokenResponseSecret,lightTokenResponseAlgorithm);
    }

    protected String getRequestCacheName(){
        return SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_PROXYSERVICE_CACHE.toString();
    }

    protected String getResponseCacheName(){
        return SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_PROXYSERVICE_CACHE.toString();
    }

}
