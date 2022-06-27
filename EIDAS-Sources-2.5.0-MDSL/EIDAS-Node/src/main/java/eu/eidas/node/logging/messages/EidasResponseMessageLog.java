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
 * Model class for logging incoming and outgoing eIDAS responses from/to
 * of the Eidas Proxy Service and Eidas Connector.
 *
 * @since 2.5
 */
public class EidasResponseMessageLog extends MessageLog {

    private final Tag inResponseTo = new Tag("inResponseTo");
    private final Tag statusCode = new Tag("statusCode");

    /**
     * MessageLog should only be build through the builder
     */
    private EidasResponseMessageLog() {}

    @Override
    protected List<Tag> getSpecificTags() {
        return Arrays.asList(inResponseTo, statusCode);
    }

    public static class Builder extends MessageLog.Builder<EidasResponseMessageLog> {

        private String inResponseTo;
        private String statusCode;

        public Builder() {
            super(EidasResponseMessageLog::new);
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
        protected void buildSpecificTags(EidasResponseMessageLog messageLog) {
            messageLog.inResponseTo.setValue(this.inResponseTo);
            messageLog.statusCode.setValue(this.statusCode);
        }
    }
}
