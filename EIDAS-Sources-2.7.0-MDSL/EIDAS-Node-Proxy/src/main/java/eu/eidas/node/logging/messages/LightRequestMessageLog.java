/*
 * Copyright (c) 2019 by European Commission
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

package eu.eidas.node.logging.messages;

import java.util.Arrays;
import java.util.List;

/**
 * Model class for logging incoming and outgoing Light requests from/to
 * of the Eidas Proxy Service and Eidas Connector.
 *
 * @since 2.5
 */
public class LightRequestMessageLog extends MessageLog {

    private final Tag bltHash = new Tag("bltHash");

    /**
     * MessageLog should only be build through the builder
     */
    private LightRequestMessageLog() {}

    @Override
    protected List<Tag> getSpecificTags() {
        return Arrays.asList(bltHash);
    }

    public static class Builder extends MessageLog.Builder<LightRequestMessageLog> {

        private String bltHash;

        public Builder() {
            super(LightRequestMessageLog::new);
        }

        public Builder setBltHash(String bltHash) {
            this.bltHash = bltHash;
            return this;
        }

        @Override
        protected void buildSpecificTags(LightRequestMessageLog messageLog) {
            messageLog.bltHash.setValue(bltHash);
        }
    }
}
