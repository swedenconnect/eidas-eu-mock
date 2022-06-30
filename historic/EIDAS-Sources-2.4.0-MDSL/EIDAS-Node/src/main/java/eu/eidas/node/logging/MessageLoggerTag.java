/*
 *
 *  Copyright (c) 2019 by European Commission
 *
 *  Licensed under the EUPL, Version 1.2 or - as soon they will be
 *  approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at:
 *  https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.
 *  See the Licence for the specific language governing permissions and
 *  limitations under the Licence
 *
 */
package eu.eidas.node.logging;

/**
 * Enum with the exhaustive list of the information logged in the various logging points of EIDAS.
 * @since 2.4
 */
public enum MessageLoggerTag {


    TIMESTAMP("Timestamp"),
    OP_TYPE("OpType"),
    NODE_ID("NodeId"),
    ORIGIN("Origin"),
    DESTINATION("Destination"),
    FLOW_ID("flowId"),
    MSG_ID("msgId"),
    BLT_HASH("bltHash"),
    MSG_HASH("msgHash"),
    IN_RESPONSE_TO("inResponseTo"),
    STATUS_CODE("statusCode")
    ;

    private final String value;

    MessageLoggerTag(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
