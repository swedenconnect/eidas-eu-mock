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

import java.util.regex.Pattern;

/**
 * Utility class providing helper methods to format and verify {@link MessageLog.Tag} objects
 * and timestamp patterns used in message logging.
 * <p>
 * This class is intended for use in unit tests to validate log content format.
 */
public class MessageLogTestUtils {

    private static final String DATE_FORMAT = "\\d{4}-\\d{2}-\\d{2}";
    private static final String TIME_FORMAT = "\\d{2}:\\d{2}(:\\d{2})*(.\\d{3,9})*";
    private static final String DATE_OFFSET = "Z";

    /**
     * The complete expected timestamp format for log entries.
     * Format: yyyy-MM-ddTHH:mm[:ss][.SSS]Z
     */
    public static final String TAG_DATE_FORMAT = DATE_FORMAT + "T" + TIME_FORMAT + DATE_OFFSET;

    private static final String TIMESTAMP_TITLE = formatTitle("Timestamp");
    private static final String TIMESTAMP_TAG_REGEX = "^" + TIMESTAMP_TITLE + TAG_DATE_FORMAT + ",$";
    private static final Pattern TIMESTAMP_TAG_PATTERN = Pattern.compile(TIMESTAMP_TAG_REGEX);

    /**
     * Verifies that the provided timestamp value matches the expected log format.
     *
     * @param timestampTagValue the timestamp string to validate
     * @throws AssertionError if the timestamp does not match the expected pattern
     */
    public static void verifyTimestampTag(String timestampTagValue) {
        String errorMsg = timestampTagValue + " doesn't fit date pattern " + TIMESTAMP_TAG_REGEX;
        Assert.assertTrue(errorMsg, TIMESTAMP_TAG_PATTERN.matcher(timestampTagValue).matches());
    }

    /**
     * Verifies that the generated {@link MessageLog.Tag} from the given title and value
     * matches the expected string representation.
     *
     * @param tagTitle      the tag title
     * @param tagValue      the tag value
     * @param valueToVerify the expected string value of the tag
     * @throws AssertionError if the tag string does not match the expected value
     */
    public static void verifyTag(String tagTitle, String tagValue, String valueToVerify) {
        MessageLog.Tag tag = getTag(tagTitle, tagValue);
        Assert.assertEquals(tag.toString(), valueToVerify);
    }

    /**
     * Creates a {@link MessageLog.Tag} instance with the specified title and value.
     *
     * @param title the tag title
     * @param value the tag value
     * @return a configured {@link MessageLog.Tag}
     */
    public static MessageLog.Tag getTag(String title, String value) {
        MessageLog.Tag tag = new MessageLog.Tag(title);
        tag.setValue(value);
        return tag;
    }

    /**
     * Builds a string representation of a tag with the specified title and value.
     *
     * @param title the tag title
     * @param value the tag value
     * @return the formatted string representation of the tag
     */
    public static String getTagValue(String title, String value) {
        MessageLog.Tag tag = new MessageLog.Tag(title);
        tag.setValue(value);
        return tag.toString();
    }

    /**
     * Formats a tag title to match the expected alignment used in logs.
     * Adds right-padding to fit the {@code MessageLog.Tag.TITLE_SIZE}.
     *
     * @param title the title to format
     * @return the padded tag title
     */
    public static String formatTitle(String title) {
        return padLeftToSize(title, MessageLog.Tag.TITLE_SIZE);
    }

    /**
     * Adds right-padding (spaces) to a string until it reaches the specified size.
     *
     * @param text the original string
     * @param size the target length
     * @return the padded string
     */
    public static String padLeftToSize(String text, int size) {
        String paddedText = text;
        while (paddedText.length() < size) {
            paddedText += " ";
        }
        return paddedText;
    }

}
