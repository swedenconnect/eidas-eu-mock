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
 * limitations under the Licence
 */
package eu.eidas.node.logging;

import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IMessageLogger;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.node.utils.LoggingSanitizer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Specific Bean class that implements the logging.
 */
public final class MessageLoggerBean implements IMessageLogger {

    private final static String TIMESTAMP_PREFIX = "Timestamp     ";

    private final static String OP_TYPE_PREFIX = "OpType        ";

    private final static String NODE_ID_PREFIX = "NodeId        ";

    private final static String ORIGIN_PREFIX = "Origin        ";

    private final static String DESTINATION_PREFIX = "Destination   ";

    private final static String FLOW_ID_PREFIX = "flowId        ";

    private final static String MSG_ID_PREFIX = "msgId         ";

    private final static String BLT_HASH_PREFIX = "bltHash       ";

    private final static String MSH_HASH_PREFIX = "msgHash       ";

    private final static String IN_RESPONSE_TO_PREFIX = "inResponseTo  ";

    private final static String STATUS_CODE_PREFIX = "statusCode    ";

    private final static int BASE64_LENGTH = 88;

    /**
     * The origin.
     */
    private String origin;

    /**
     * Status Code of the Response.
     */
    private String statusCode;

    /**
     * In Response to.
     */
    private String inResponseTo;

    /**
     * Operation type.
     */
    private String opType;

    /**
     * The destination.
     */
    private String destination;

    /**
     * Encrypted SAML/LightRequest/LightResponse token.
     */
    private String msgHash;

    /**
     * Encrypted binary light token.
     */
    private String bltHash;

    /**
     * Id of the originator message.
     */
    private String msgId;

    /**
     * ID for related messages (requests or responses) in Eidas Connector, respectively in Eidas Proxy Service, must be Unique.
     */
    private String flowId;

    /**
     * the value of "lightToken.connector.request.node.id" from specificCommunicationDefinitionConnector.xml that needs to be created
     */
    private static String nodeId;

    public MessageLoggerBean(String opType, byte[] msgObj, String msgId, String origin, String destinationUrl,
                             String flowId, String nodeId, String tokenBase64, String inResponseTo, String statusCode) {

        this(opType, msgObj, msgId, origin, destinationUrl, flowId, nodeId, inResponseTo, statusCode);

        this.bltHash = createBltHash(tokenBase64);
    }

    public MessageLoggerBean(String opType, byte[] msgObj, String msgId, String origin, String destinationUrl,
                             String flowId, String nodeId, String inResponseTo, String statusCode) {

        this(opType, msgObj, msgId, origin, destinationUrl, flowId, nodeId);

        this.inResponseTo = LoggingSanitizer.removeCRLFInjection(inResponseTo);
        this.statusCode = LoggingSanitizer.removeCRLFInjection(statusCode);
    }

    public MessageLoggerBean(String opType, byte[] msgObj, String msgId, String origin, String destinationUrl,
                             String flowId, String nodeId, String tokenBase64) {

        this(opType, msgObj, msgId, origin, destinationUrl, flowId, nodeId);

        this.bltHash = createBltHash(tokenBase64);
    }

    public MessageLoggerBean(String opType, byte[] msgObj, String msgId, String origin, String destinationUrl,
                             String flowId, String nodeId) {

        this.opType = opType;
        this.origin = LoggingSanitizer.removeCRLFInjection(origin);
        this.destination = LoggingSanitizer.removeCRLFInjection(destinationUrl);

        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(msgObj);
        this.msgHash = Base64.encode(msgHashBytes, BASE64_LENGTH);

        this.msgId = LoggingSanitizer.removeCRLFInjection(msgId);
        this.flowId = flowId;
        this.nodeId = nodeId;
    }

    @Override
    public String createLogMessage() {
        final StringBuilder stringBuilder = new StringBuilder();

        origin = removeHttpParametersInCaseOfRedirectBinding(getOrigin());

        String timestamp = DateTime.now(DateTimeZone.UTC).toString();
        stringBuilder
                .append("\n")
                .append(TIMESTAMP_PREFIX).append(timestamp).append(",\n")
                .append(OP_TYPE_PREFIX).append(getOpType()).append(",\n")
                .append(NODE_ID_PREFIX).append(getNodeId()).append(",\n")
                .append(ORIGIN_PREFIX).append(getOrigin()).append(",\n")
                .append(DESTINATION_PREFIX).append(getDestination()).append(",\n")
                .append(FLOW_ID_PREFIX).append(getFlowId()).append(",\n")
                .append(MSG_ID_PREFIX).append(getMsgId()).append(",\n");

        if (StringUtils.isNotEmpty(getMsgHash())) {
            stringBuilder
                    .append(MSH_HASH_PREFIX).append(getMsgHash());
        }

        if (StringUtils.isNotEmpty(getBltHash())) {
            stringBuilder
                    .append(",\n")
                    .append(BLT_HASH_PREFIX).append(getBltHash());
        }

        if (StringUtils.isNotEmpty(getInResponseTo()) && StringUtils.isNotEmpty(getStatusCode())) {
            stringBuilder
                    .append(",\n")
                    .append(IN_RESPONSE_TO_PREFIX).append(getInResponseTo());

            stringBuilder
                    .append(",\n")
                    .append(STATUS_CODE_PREFIX).append(getStatusCode());
        }

        return stringBuilder.toString();
    }

    /**
     * Creates the BltHash from the token received in Base64.
     *
     */
    private String createBltHash(String tokenBase64) {
        final byte[] tokenBytes = EidasStringUtil.decodeBytesFromBase64(tokenBase64);
        if (ArrayUtils.isEmpty(tokenBytes)) {
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }
        byte[] bltHashBytes = EidasDigestUtil.hashPersonalToken(tokenBytes);

        return Base64.encode(bltHashBytes, BASE64_LENGTH);
    }

    private String removeHttpParametersInCaseOfRedirectBinding(String url) {
        if (StringUtils.isNotEmpty(url) && url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        } else {
            return url;
        }
    }

    /**
     * Getter for origin
     *
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Getter for statusCode
     *
     * @return the statusCode
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Getter for inResponseTo
     *
     * @return the inResponseTo
     */
    public String getInResponseTo() {
        return inResponseTo;
    }

    /**
     * Getter for opType
     *
     * @return the opType
     */
    public String getOpType() {
        return opType;
    }

    /**
     * Getter for destination
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Getter for msgHash
     *
     * @return the msgHash
     */
    public String getMsgHash() {
        return msgHash;
    }

    /**
     * Getter for bltHash
     *
     * @return the bltHash
     */
    public String getBltHash() {
        return bltHash;
    }

    /**
     * Getter for msgId
     *
     * @return the msgId
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * Getter for flowId
     *
     * @return the flowId
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Getter for nodeId
     *
     * @return the nodeId
     */
    public static String getNodeId() {
        return nodeId;
    }

}
