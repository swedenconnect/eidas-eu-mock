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

package eu.eidas.node.logging.messages;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Util class created for test purpose.
 * It provides util methods that help verifying message log content.
 */
public class MessageLogTestUtils {

    private static final String DATE_FORMAT = "\\d{4}-\\d{2}-\\d{2}";
    private static final String TIME_FORMAT = "\\d{2}:\\d{2}(:\\d{2})*(.\\d{3,9})*";
    private static final String DATE_OFFSET = "Z";

    private static final String TAG_DATE_FORMAT = DATE_FORMAT + "T" + TIME_FORMAT + DATE_OFFSET;
    private static final String TIMESTAMP_TITLE = MessageLogTestUtils.formatTitle("Timestamp");
    private static final String TIMESTAMP_TAG_REGEX = "^" + TIMESTAMP_TITLE + TAG_DATE_FORMAT + ",$";
    private static final Pattern TIMESTAMP_TAG_PATTERN = Pattern.compile(TIMESTAMP_TAG_REGEX);

    public static void verifyTimestampTag(String timestampTagValue) {
        String errorMsg = timestampTagValue + " doesn't fit date pattern " + TIMESTAMP_TAG_REGEX;
        Assert.assertTrue(errorMsg, TIMESTAMP_TAG_PATTERN.matcher(timestampTagValue).matches());
    }

    public static void verifyTag(String tagTitle, String tagValue, String valueToVerify) {
        MessageLog.Tag tag = getTag(tagTitle, tagValue);
        Assert.assertEquals(tag.toString(), valueToVerify);
    }

    public static MessageLog.Tag getTag(String title, String value) {
        MessageLog.Tag tag = new MessageLog.Tag(title);
        tag.setValue(value);
        return tag;
    }

    public static String getTagValue(String title, String value) {
        MessageLog.Tag tag = new MessageLog.Tag(title);
        tag.setValue(value);
        return tag.toString();
    }

    public static String formatTitle(String title) {
        return MessageLogTestUtils.padLeftToSize(title, MessageLog.Tag.TITLE_SIZE);
    }

    public static String padLeftToSize(String text, int size) {
        String paddedText = text;
        while (paddedText.length() < size) {
            paddedText += " ";
        }
        return paddedText;
    }

    /**
     * Test method for the util method used for this tests
     */
    @Test
    public void testPaddingMethod() {
        String text = "Test";

        String expectedText = "Test    ";
        String actualText = padLeftToSize(text, 8);
        Assert.assertEquals(expectedText, actualText);
    }

    @Test
    public void testTimestampPattern() {
        Pattern timestampPattern = Pattern.compile(TAG_DATE_FORMAT);

        String testVal = "2020-03-06T15:04Z";
        Assert.assertTrue(timestampPattern.matcher(testVal).matches());

        testVal = "2020-03-06T15:04:46Z";
        Assert.assertTrue(timestampPattern.matcher(testVal).matches());

        testVal = "2020-03-06T15:04:46.345Z";
        Assert.assertTrue(timestampPattern.matcher(testVal).matches());
    }
}
