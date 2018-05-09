/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.auth.commons.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XMLHelper {

    /**
     * Creates a text node with the given content and appends it as child to the given element.
     *
     * @param domElement  the element to receive the text node
     * @param textContent the content for the text node
     */
    public static void appendTextContent(Element domElement, String textContent) {
        if (textContent != null) {
            Document parentDocument = domElement.getOwnerDocument();
            Text textNode = parentDocument.createTextNode(textContent);
            domElement.appendChild(textNode);
        }
    }
}
