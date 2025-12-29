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
package eu.eidas.logging.utils.logger;

import javax.cache.Cache;

/**
 * Data container used while unit testing the various message loggers.
 *
 * @since 2.3
 */
public class LoggerTestData {
    public boolean logMessage;
    public String tokenBase64;
    public String opType;
    public String nodeId;
    public String origin;
    public String destination;
    public String msgId;
    public String inResponseTo;
    public String statusCode;

    public Cache<String, String> flowIdCache;

    public LoggerTestData setOpType(String opType) {
        this.opType = opType;
        return this;
    }

    public LoggerTestData setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public LoggerTestData setLogMessage(boolean logMessage) {
        this.logMessage = logMessage;
        return this;
    }

    public LoggerTestData setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public LoggerTestData setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public LoggerTestData setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public LoggerTestData setTokenBase64(String tokenBase64) {
        this.tokenBase64 = tokenBase64;
        return this;
    }

    public LoggerTestData setInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
        return this;
    }

    public LoggerTestData setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public LoggerTestData setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
        return this;
    }
}
