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

import eu.eidas.auth.commons.EIDASStatusCode;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link MessageLoggerBean}
 */
public class MessageLoggerBeanTest {

    /**
     * Test method for
     * {@link MessageLoggerBean#createLogMessage()}
     * when {@link MessageLoggerBean#MessageLoggerBean(String, byte[], String, String, String, String, String, String, String, String)}
     * constructor is used with all correctly filled in parameters.
     * <p>
     * Must succeed.
     */
    @Test
    public void createLogMessageWithInResponseToAndStatusCode() {
        final String opType = "opType";
        byte[] msgObj = "lightRequest".getBytes();
        String msgId = "ID";
        String origin = "origin?cenas";
        String destinationUrl = "destinationUrl";
        String flowId = "flowID";
        String nodeId = "nodeID";
        String tokenStringBase64 = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvblByb3h5c2VydmljZVJlcXVlc3R8NmE4NDlkZDAtYjkyZS00MWVkLTk3OGYtYTUyMzY4Y2IzNDVlfDIwMTctMTItMTIgMDg6Mzg6MDIgMTIyfGRKZ1N3eGFLZGNQUXhFaXdhaGQ2WDliV1Y0MDgxeGVjTkxlQWgzcENPbk09\"";
        String inResponseToId = "inResponseToId";
        String statusCode = EIDASStatusCode.SUCCESS_URI.toString();

        MessageLoggerBean loggerBean = new MessageLoggerBean(opType, msgObj, msgId, origin, destinationUrl,
                flowId, nodeId, tokenStringBase64, inResponseToId, statusCode);

        String actualLogMessage = loggerBean.createLogMessage();

        String regexp = "\\s" +
                "Timestamp     \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z,\\s" +
                "OpType        opType,\n" +
                "NodeId        nodeID,\n" +
                "Origin        origin,\n" +
                "Destination   destinationUrl,\n" +
                "flowId        flowID,\n" +
                "msgId         ID,\n" +
                "msgHash       LleUB7RFrbRUQaTzytsWCNVceLdifexJsNDp6FaP16FKankJ2SW5v3E2kbm8oMPiBNhU3AJVEuXfryyXV9C2\\+A==,\n" +
                "bltHash       b0Ij2751059Z\\+uUMRNF7vmZWsQSDwiNhMm\\+KX1A9g1bT8fCM1zlPS6yrTtT0ytSzGH2IAL6duoj4glv9mcvV6Q==,\n" +
                "inResponseTo  inResponseToId,\n" +
                "statusCode    urn:oasis:names:tc:SAML:2.0:status:Success";

        boolean matches = actualLogMessage.matches(regexp);
        Assert.assertTrue(matches);

    }

    /**
     * Test method for
     * {@link MessageLoggerBean#createLogMessage()}
     * when {@link MessageLoggerBean#MessageLoggerBean(String, byte[], String, String, String, String, String, String)}
     * constructor is used with all correctly filled in parameters.
     * <p>
     * Must succeed.
     */
    @Test
    public void createLogMessageWithBltHash() {
        final String opType = "opType";
        byte[] msgObj = "lightRequest".getBytes();
        String msgId = "ID";
        String origin = "origin";
        String destinationUrl = "destinationUrl";
        String flowId = "flowID";
        String nodeId = "nodeID";
        String tokenStringBase64 = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvblByb3h5c2VydmljZVJlcXVlc3R8NmE4NDlkZDAtYjkyZS00MWVkLTk3OGYtYTUyMzY4Y2IzNDVlfDIwMTctMTItMTIgMDg6Mzg6MDIgMTIyfGRKZ1N3eGFLZGNQUXhFaXdhaGQ2WDliV1Y0MDgxeGVjTkxlQWgzcENPbk09\"";

        MessageLoggerBean loggerBean =
                new  MessageLoggerBean(opType, msgObj, msgId, origin, destinationUrl, flowId, nodeId, tokenStringBase64);

        String actualLogMessage = loggerBean.createLogMessage();

        String regexp = "\\s" +
                "Timestamp     \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z,\\s" +
                "OpType        opType,\n" +
                "NodeId        nodeID,\n" +
                "Origin        origin,\n" +
                "Destination   destinationUrl,\n" +
                "flowId        flowID,\n" +
                "msgId         ID,\n" +
                "msgHash       LleUB7RFrbRUQaTzytsWCNVceLdifexJsNDp6FaP16FKankJ2SW5v3E2kbm8oMPiBNhU3AJVEuXfryyXV9C2\\+A==,\n" +
                "bltHash       b0Ij2751059Z\\+uUMRNF7vmZWsQSDwiNhMm\\+KX1A9g1bT8fCM1zlPS6yrTtT0ytSzGH2IAL6duoj4glv9mcvV6Q==";

        boolean matches = actualLogMessage.matches(regexp);
        Assert.assertTrue(matches);
    }

}