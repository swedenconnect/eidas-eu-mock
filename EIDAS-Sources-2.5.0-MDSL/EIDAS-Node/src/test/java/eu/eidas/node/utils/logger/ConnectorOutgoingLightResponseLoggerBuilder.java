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

import eu.eidas.node.logging.connector.messages.ConnectorOutgoingLightResponseLogger;
import org.apache.commons.lang.reflect.FieldUtils;

public class ConnectorOutgoingLightResponseLoggerBuilder extends LightMessageLoggerBuilder <ConnectorOutgoingLightResponseLoggerBuilder, ConnectorOutgoingLightResponseLogger> {

    private String lightTokenConnectorResponseNodeId;

    public ConnectorOutgoingLightResponseLoggerBuilder withLightTokenConnectorResponseNodeId(String lightTokenConnectorResponseNodeId) {
        this.lightTokenConnectorResponseNodeId = lightTokenConnectorResponseNodeId;
        return getThis();
    }

    @Override
    public ConnectorOutgoingLightResponseLogger build() throws IllegalAccessException {
        ConnectorOutgoingLightResponseLogger logger = new ConnectorOutgoingLightResponseLogger();
        logger.setFlowIdCache(getFlowIdCache());
        logger.setLightTokenConnectorResponseNodeId(lightTokenConnectorResponseNodeId);
        logger.setMessageLoggerUtils(getMessageLoggerUtils());
        logger.setSpringManagedSpecificCommunicationLoggingService(getSpecificCommunicationLoggingService());
        FieldUtils.writeField(logger, "fullLogger", getFullLogger(), true);
        return logger;
    }

    @Override
    protected ConnectorOutgoingLightResponseLoggerBuilder getThis() {
        return this;
    }

    public String getLightTokenConnectorResponseNodeId() {
        return lightTokenConnectorResponseNodeId;
    }
}
