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

package eu.eidas.node.utils.logger;

import eu.eidas.node.logging.connector.messages.ConnectorIncomingLightRequestLogger;
import org.apache.commons.lang.reflect.FieldUtils;

/**
 * Builder for ConnectorIncomingLightRequestLoggerBuilder
 *
 * @since 2.3
 */
public class ConnectorIncomingLightRequestLoggerBuilder
        extends LightMessageLoggerBuilder <ConnectorIncomingLightRequestLoggerBuilder, ConnectorIncomingLightRequestLogger> {

    private String lightTokenConnectorRequestNodeId;

    public ConnectorIncomingLightRequestLoggerBuilder withLightTokenConnectorRequestNodeId(String lightTokenConnectorRequestNodeId) {
        this.lightTokenConnectorRequestNodeId = lightTokenConnectorRequestNodeId;
        return getThis();
    }

    public ConnectorIncomingLightRequestLogger build() throws IllegalAccessException {
        ConnectorIncomingLightRequestLogger logger = new ConnectorIncomingLightRequestLogger();
        logger.setFlowIdCache(getFlowIdCache());
        logger.setLightTokenConnectorRequestNodeId(getLightTokenConnectorRequestNodeId());
        logger.setMessageLoggerUtils(getMessageLoggerUtils());
        logger.setSpringManagedSpecificCommunicationLoggingService(getSpecificCommunicationLoggingService());
        FieldUtils.writeField(logger, "fullLogger", getFullLogger(), true);
        return logger;
    }

    @Override
    protected ConnectorIncomingLightRequestLoggerBuilder getThis() {
        return this;
    }

    public String getLightTokenConnectorRequestNodeId() {
        return lightTokenConnectorRequestNodeId;
    }
}