/*
 * Copyright (c) 2018 by European Commission
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

package eu.eidas.node.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link LoggingSanitizer}.
 */
public class LoggingSanitizerTest {

    /**
     * The same as {@link LoggingSanitizer#SANITIZED_SUFIX}
     */
    private String SANITIZED_SUFIX = " (Sanitized)";


    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter contains Carriage Return (ASCII 13, \r) and Line Feed (ASCII 10, \n) characters.
     */
    @Test
    public void testRemoveCRLFInjectionStringWithCRLF() {
        final String message = "DEMO-SP\\n2017-04-28; 13:10:29.328 [ajp-nio-8009-exec-10] INFO gOtThEmIlK\\n\\r\\n\\r";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter does contains Carriage Return (ASCII 13, \r) and Line Feed (ASCII 10, \n) characters.
     */
    @Test
    public void testRemoveCRLFInjection() {
        final String message = "DEMO-SP 2017-04-28; 13:10:29.328 [ajp-nio-8009-exec-10] INFO gOtThEmIlK";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalNotApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an URL.
     */
    @Test
    public void testRemoveCRLFInjectionUrl() {
        final String message = "http://localhost$-_.+!*'(),:8080/EidasNode/ColleagueRequest";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalNotApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an URL with CRLF.
     */
    @Test
    public void testRemoveCRLFInjectionUrlWithCRLF() {
        final String message = "http://localhost$-_.+!*'(),:8080/\\n\\rEidasNode/ColleagueRequest";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an ID.
     */
    @Test
    public void testRemoveCRLFInjectionId() {
        final String message = "_3WcFUc9H8b8a1kHoS.DfLcmyUR9WREN_2W6ejrWJFPrueVt-I6GcWaZx76EzQzv";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalNotApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an ID with CRLF.
     */
    @Test
    public void testRemoveCRLFInjectionIdWithCRLF() {
        final String message = "_3WcFUc9H8b8a1kHoS.DfLcmyUR9WREN_2W6ejrWJFPrueVt-I6GcWaZx76EzQzv\\r";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an Hash.
     */
    @Test
    public void testRemoveCRLFInjectionHash() {
        final String message = "NU3P2SBD8BRUN9yEWoO5RbZP/BijYjrciZnKtrlxU6zexWYRlOWzdWH0HpofKynOcYVb/eO/OWzK26WtwkoEBA==";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalNotApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an Hash with CRLF.
     */
    @Test
    public void testRemoveCRLFInjectionHashWithCRLF() {
        final String message = "NU3P2SBD8BRUN9yEWoO5RbZP/BijYjrciZnKtr\\nlxU6zexWYRlOWzdWH0HpofKynOcYVb/eO/OWzK26WtwkoEBA==";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalApplied(message, messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is Null.
     */
    @Test
    public void testRemoveCRLFInjectionNullParameter() {
        final String message = "NU3P2SBD8BRUN9yEWoO5RbZP/BijYjrciZnKtrlxU6zexWYRlOWzdWH0HpofKynOcYVb/eO/OWzK26WtwkoEBA==";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalNotApplied(message,messageWithoutCRLFInjection);
    }

    /**
     * Tests {@link LoggingSanitizer#removeCRLFInjection(String)} when,
     * the String received as parameter is an empty string.
     */
    @Test
    public void testRemoveCRLFInjectionEmptyParameter() {
        final String message = "";
        final String messageWithoutCRLFInjection = LoggingSanitizer.removeCRLFInjection(message);
        assertCrlfRemovalNotApplied(message, messageWithoutCRLFInjection);
    }


    /**
     * Auxiliar method that contains the assertions when CRLF removal was applied.
     *
     * @param messageIn the inital string before CRLF removal
     * @param messageOut the output string after CRLF removal
     */
    private void assertCrlfRemovalApplied(String messageIn, String messageOut) {
        Assert.assertNotEquals(messageIn,messageOut);
        SANITIZED_SUFIX = " (Sanitized)";
        Assert.assertTrue("The Sanitized Sufix was not appended",messageOut.contains(SANITIZED_SUFIX));
    }

    /**
     * Auxiliar method that contains the assertions when CRLF removal was applied.
     *
     * @param messageIn the inital string before CRLF removal
     * @param messageOut the output string after CRLF removal
     */
    private void assertCrlfRemovalNotApplied(String messageIn, String messageOut) {
        Assert.assertEquals(messageIn,messageOut);
        Assert.assertFalse("The Sanitized Sufix was appended",messageOut.contains(SANITIZED_SUFIX));
    }

}