package eu.eidas.node.utils.logger;

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
