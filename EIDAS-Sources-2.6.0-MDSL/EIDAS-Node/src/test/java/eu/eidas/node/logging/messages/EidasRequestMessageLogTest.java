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

import eu.eidas.node.logging.LoggingConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link EidasRequestMessageLog}
 */
public class EidasRequestMessageLogTest {

    /**
     * Setup tests values for EidasRequestMessageLog tags
     */
    private String opType = "opType";
    private String nodeId = "nodeId";
    private String origin = "origin";
    private String destination = "destination";
    private String flowId = "flowId";
    private String msgId = "msgId";
    private String msgHash = "msgHash";

    /**
     * Amount of lines for a EidasRequestMessageLog
     */
    private int amountOfLines = 9;

    /**
     * Test method for {@link EidasRequestMessageLog#toString()}
     * Test the output of the MessageLog for a fully completed EidasRequestMessageLog
     * build with the intern builder.
     * <br>
     * Must succeed.
     */
    @Test
    public void testFullLog() {
        EidasRequestMessageLog.Builder builder = new EidasRequestMessageLog.Builder();
        builder.setOpType(opType);
        builder.setNodeId(nodeId);
        builder.setOrigin(origin);
        builder.setDestination(destination);
        builder.setFlowId(flowId);
        builder.setMsgId(msgId);
        builder.setMsgHash(msgHash);

        String loggedMessage = builder.build();
        String[] loggedLines = loggedMessage.split("\n");
        Assert.assertEquals(amountOfLines, loggedLines.length);

        MessageLogTestUtils.verifyTimestampTag(loggedLines[1]);
        MessageLogTestUtils.verifyTag("OpType", opType, loggedLines[2]);
        MessageLogTestUtils.verifyTag("NodeId", nodeId, loggedLines[3]);
        MessageLogTestUtils.verifyTag("Origin", origin, loggedLines[4]);
        MessageLogTestUtils.verifyTag("Destination", destination, loggedLines[5]);
        MessageLogTestUtils.verifyTag("flowId", flowId, loggedLines[6]);
        MessageLogTestUtils.verifyTag("msgId", msgId, loggedLines[7]);
        MessageLogTestUtils.verifyTag("msgHash", msgHash, loggedLines[8]);
    }

    /**
     * Test method for {@link EidasRequestMessageLog#toString()}
     * Test the output of the MessageLog for a fully completed EidasRequestMessageLog
     * build with the intern builder in a different order than the output to verify it
     * doesn't impact the order of the actual output message.
     * <br>
     * Must succeed.
     */
    @Test
    public void testFullLogBuildWithMixedOrder() {
        EidasRequestMessageLog.Builder builder = new EidasRequestMessageLog.Builder();
        builder.setOpType(opType);
        builder.setOrigin(origin);
        builder.setNodeId(nodeId);
        builder.setDestination(destination);
        builder.setMsgHash(msgHash);
        builder.setFlowId(flowId);
        builder.setMsgId(msgId);

        String loggedMessage = builder.build();
        String[] loggedLines = loggedMessage.split("\n");
        Assert.assertEquals(amountOfLines, loggedLines.length);

        MessageLogTestUtils.verifyTimestampTag(loggedLines[1]);
        MessageLogTestUtils.verifyTag("OpType", opType, loggedLines[2]);
        MessageLogTestUtils.verifyTag("NodeId", nodeId, loggedLines[3]);
        MessageLogTestUtils.verifyTag("Origin", origin, loggedLines[4]);
        MessageLogTestUtils.verifyTag("Destination", destination, loggedLines[5]);
        MessageLogTestUtils.verifyTag("flowId", flowId, loggedLines[6]);
        MessageLogTestUtils.verifyTag("msgId", msgId, loggedLines[7]);
        MessageLogTestUtils.verifyTag("msgHash", msgHash, loggedLines[8]);
    }

    /**
     * Test method for {@link EidasRequestMessageLog#toString()}
     * Test the output of the MessageLog for a partially completed EidasRequestMessageLog
     * build with the intern builder.
     * <br>
     * Must succeed.
     */
    @Test
    public void testPartialLog() {
        EidasRequestMessageLog.Builder builder = new EidasRequestMessageLog.Builder();
        builder.setOpType(opType);
        builder.setNodeId(nodeId);
        builder.setOrigin(origin);
        builder.setMsgHash(msgHash);

        String loggedMessage = builder.build();
        String[] loggedLines = loggedMessage.split("\n");
        Assert.assertEquals(amountOfLines, loggedLines.length);

        MessageLogTestUtils.verifyTimestampTag(loggedLines[1]);
        MessageLogTestUtils.verifyTag("OpType", opType, loggedLines[2]);
        MessageLogTestUtils.verifyTag("NodeId", nodeId, loggedLines[3]);
        MessageLogTestUtils.verifyTag("Origin", origin, loggedLines[4]);
        MessageLogTestUtils.verifyTag("Destination", LoggingConstants.UNDEFINED, loggedLines[5]);
        MessageLogTestUtils.verifyTag("flowId", LoggingConstants.UNDEFINED, loggedLines[6]);
        MessageLogTestUtils.verifyTag("msgId", LoggingConstants.UNDEFINED, loggedLines[7]);
        MessageLogTestUtils.verifyTag("msgHash", msgHash, loggedLines[8]);
    }
}
