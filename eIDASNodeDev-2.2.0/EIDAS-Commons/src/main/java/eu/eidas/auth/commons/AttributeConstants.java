/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.commons;

/**
 * This enum class contains all the eIDAS Node, Commons and Specific errors constant identifiers.
 */
public enum AttributeConstants {

    /**
     * Represents the attribute's name index.
     */
    ATTR_NAME_INDEX(0),
    /**
     * Represents the attribute's type index.
     */
    ATTR_TYPE_INDEX(1),
    /**
     * Represents the attribute's value index.
     */
    ATTR_VALUE_INDEX(2),
    /**
     * Represents the attribute's status index.
     */
    ATTR_STATUS_INDEX(3),
    /**
     * Represents the number of allowed tuples.
     */
    NUMBER_TUPLES(4);

    /**
     * Represents the constant's value.
     */
    private final transient int attribute;

    /**
     * Solo Constructor.
     *
     * @param attr The Attribute Constant value.
     */
    AttributeConstants(final int attr) {

        this.attribute = attr;
    }

    /**
     * Return the Constant Value.
     *
     * @return The constant value.
     */
    public int intValue() {

        return attribute;
    }
}
