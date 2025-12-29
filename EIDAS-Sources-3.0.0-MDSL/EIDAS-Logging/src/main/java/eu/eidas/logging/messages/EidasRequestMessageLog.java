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

import java.util.Collections;
import java.util.List;

/**
 * Model class for logging incoming and outgoing eIDAS requests from/to
 * of the Eidas Proxy Service and Eidas Connector.
 *
 * @since 2.5
 */
public class EidasRequestMessageLog extends MessageLog {

    /**
     * MessageLog should only be build through the builder
     */
    private EidasRequestMessageLog() {}

    @Override
    protected List<Tag> getSpecificTags() {
        return Collections.emptyList();
    }

    public static class Builder extends MessageLog.Builder<EidasRequestMessageLog> {

        public Builder() {
            super(EidasRequestMessageLog::new);
        }

        @Override
        protected void buildSpecificTags(EidasRequestMessageLog messageLog) {}
    }
}
