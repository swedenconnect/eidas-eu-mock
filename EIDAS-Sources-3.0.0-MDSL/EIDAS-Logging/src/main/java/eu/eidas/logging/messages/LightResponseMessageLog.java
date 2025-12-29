/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.logging.messages;

import java.util.Arrays;
import java.util.List;

/**
 * Model class for logging incoming and outgoing Light responses from/to
 * of the Eidas Proxy Service and Eidas Connector.
 *
 * @since 2.5
 */
public class LightResponseMessageLog extends MessageLog {

    private final Tag bltHash = new Tag("bltHash");
    private final Tag inResponseTo = new Tag("inResponseTo");
    private final Tag statusCode = new Tag("statusCode");

    /**
     * MessageLog should only be build through the builder
     */
    private LightResponseMessageLog() {}

    @Override
    protected List<Tag> getSpecificTags() {
        return Arrays.asList(bltHash, inResponseTo, statusCode);
    }

    public static class Builder extends MessageLog.Builder<LightResponseMessageLog> {

        private String bltHash;
        private String inResponseTo;
        private String statusCode;

        public Builder() {
            super(LightResponseMessageLog::new);
        }

        public Builder setBltHash(String bltHash) {
            this.bltHash = bltHash;
            return this;
        }

        public Builder setInResponseTo(String inResponseTo) {
            this.inResponseTo = inResponseTo;
            return this;
        }

        public Builder setStatusCode(String statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        protected void buildSpecificTags(LightResponseMessageLog messageLog) {
            messageLog.bltHash.setValue(this.bltHash);
            messageLog.inResponseTo.setValue(this.inResponseTo);
            messageLog.statusCode.setValue(this.statusCode);
        }
    }
}
