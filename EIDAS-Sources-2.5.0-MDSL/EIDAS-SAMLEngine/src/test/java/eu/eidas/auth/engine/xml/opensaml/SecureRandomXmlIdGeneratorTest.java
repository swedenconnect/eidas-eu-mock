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
package eu.eidas.auth.engine.xml.opensaml;

import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SecureRandomXmlIdGeneratorTest
 *
 * @since 2016-07-04
 */
public final class SecureRandomXmlIdGeneratorTest {

    @Test
    public void generateIdentifier() throws Exception {
        for (int i = 0; i < 100; i++) {
            String id = SecureRandomXmlIdGenerator.INSTANCE.generateIdentifier();
            System.out.println("id = \"" + id + "\"");
            assertEquals(64, id.length());
            Pattern pattern = Pattern.compile("_[A-Za-z0-9_\\.\\-]{63}");
            assertTrue(pattern.matcher(id).matches());
        }
    }

    @Test
    public void generateIdentifier1() throws Exception {
        int length = 10;
        for (int i = 0; i < 100; i++) {
            String id = SecureRandomXmlIdGenerator.INSTANCE.generateIdentifier(length);
            System.out.println("id = \"" + id + "\"");
            assertEquals(length, id.length());
            Pattern pattern = Pattern.compile("_[A-Za-z0-9_\\.\\-]{" + (length-1) + "}");
            assertTrue(pattern.matcher(id).matches());
        }
    }
}
