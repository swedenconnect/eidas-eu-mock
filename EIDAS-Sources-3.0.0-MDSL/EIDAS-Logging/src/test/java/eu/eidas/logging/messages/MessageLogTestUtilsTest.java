/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.logging.messages;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Test class for {@link MessageLogTestUtils}.
 * <p>
 * Verifies the correct behavior of utility methods used for formatting and validating
 * message log content such as timestamp patterns and string padding.
 */
public class MessageLogTestUtilsTest {

    /**
     * Test method for {@link MessageLogTestUtils#padLeftToSize(String, int)}.
     * Verifies that padding adds spaces to the right to reach the desired length.
     * <p>
     * Must succeed.
     */
    @Test
    public void testPaddingMethod() {
        String text = "Test";

        String expectedText = "Test    ";
        String actualText = MessageLogTestUtils.padLeftToSize(text, 8);
        Assert.assertEquals(expectedText, actualText);
    }

    /**
     * Test method for the regular expression {@link MessageLogTestUtils#TAG_DATE_FORMAT}.
     * Verifies that various timestamp formats conform to the expected pattern.
     * <p>
     * Must succeed.
     */
    @Test
    public void testTimestampPattern() {
        Pattern timestampPattern = Pattern.compile(MessageLogTestUtils.TAG_DATE_FORMAT);

        String testVal = "2020-03-06T15:04Z";
        Assert.assertTrue(timestampPattern.matcher(testVal).matches());

        testVal = "2020-03-06T15:04:46Z";
        Assert.assertTrue(timestampPattern.matcher(testVal).matches());

        testVal = "2020-03-06T15:04:46.345Z";
        Assert.assertTrue(timestampPattern.matcher(testVal).matches());
    }
}
