/*
 * Copyright (c) 2020 by European Commission
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

package eu.eidas.specificcommunication.protocol.validation;

import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IncomingLightRequestValidatorLoAComponent {

    final private static String XML_LOA_TYPE_NOTIFIED = "notified";
    final private static String XML_LOA_TYPE_NON_NOTIFIED = "nonNotified";

    public final static String ERROR_LIGHT_REQUEST_BASE = "Incoming light request is invalid: ";
    final static String ERROR_MORE_THEN_ONE_NOTIFIED_LOA = "more than one type=notified element";
    final static String ERROR_WRONG_TYPE_NOTIFIED = "type=notified element contains non-notified level of Assurance";
    final static String ERROR_WRONG_TYPE_NON_NOTIFIED = "type=notified element contains non-notified level of Assurance";
    final static String ERROR_NO_LOAS_FOUND = "no level of assurance elements present";


    public static void validate(final String lightRequest) throws SpecificCommunicationException {
        try {
            final Document xmlDocument = DocumentBuilderFactoryUtil.parse(new ByteArrayInputStream(lightRequest.getBytes()));
            final NodeList levelOfAssuranceList = xmlDocument.getElementsByTagName("levelOfAssurance"); // xml document should have namespace
            final List<Node> loANodeList = IntStream.range(0, levelOfAssuranceList.getLength()).mapToObj(levelOfAssuranceList::item).collect(Collectors.toList());
            assertNodesAreSecondLevelNodes(xmlDocument, loANodeList);
            final List<Node> levelOfAssuranceTypeNotfied = getNotifiedLoaNodes(loANodeList);
            final List<Node> levelOfAssuranceTypeNonNotfied = getNonNotifiedLoaNodes(loANodeList);

            if (isLightRequestFieldContainsNodesOfType(levelOfAssuranceTypeNotfied, LevelOfAssuranceType.NON_NOTIFIED)) {
                throwValidationException(ERROR_WRONG_TYPE_NOTIFIED);
            }

            if (isLightRequestFieldContainsNodesOfType(levelOfAssuranceTypeNonNotfied, LevelOfAssuranceType.NOTIFIED)) {
                throwValidationException(ERROR_WRONG_TYPE_NON_NOTIFIED);
            }

            if (levelOfAssuranceTypeNotfied.size() > 1) {
                throwValidationException(ERROR_MORE_THEN_ONE_NOTIFIED_LOA);
            }

            if (levelOfAssuranceList.getLength() < 1) {
                throwValidationException(ERROR_NO_LOAS_FOUND);
            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new SpecificCommunicationException(e);
        }
    }

    private static void assertNodesAreSecondLevelNodes(final Document xmlDocument, final List<Node> loANodeList) {
        final Node root = xmlDocument.getFirstChild();
        assert loANodeList.stream().reduce(true, (partialBooleanResult, b) -> partialBooleanResult && b.getParentNode().equals(root), Boolean::logicalAnd);
    }

    private static void throwValidationException(final String errorNoLoasFound) throws SpecificCommunicationException {
        throw new SpecificCommunicationException(ERROR_LIGHT_REQUEST_BASE + errorNoLoasFound);
    }

    private static boolean isLightRequestFieldContainsNodesOfType(final List<Node> levelOfAssuranceNodes, final LevelOfAssuranceType levelOfAssuranceType) {
        return levelOfAssuranceNodes.stream()
                .map(Node::getFirstChild)
                .map(Node::getNodeValue)
                .map(LevelOfAssuranceType::fromLoAValue)
                .anyMatch(levelOfAssuranceType::equals);
    }

    private static List<Node> getNonNotifiedLoaNodes(final List<Node> loANodeList) {
        return loANodeList.stream()
                .filter((Node node) -> {
                    Node attType = node.getAttributes().getNamedItem("type");
                    return attType != null && XML_LOA_TYPE_NON_NOTIFIED.equals(attType.getNodeValue());
                }).collect(Collectors.toList());
    }

    private static List<Node> getNotifiedLoaNodes(final List<Node> loANodeList) {
        return loANodeList.stream()
                .filter((Node node) -> {
                    Node attType = node.getAttributes().getNamedItem("type");
                    return attType == null || XML_LOA_TYPE_NOTIFIED.equals(attType.getNodeValue());
                }).collect(Collectors.toList());
    }
}
