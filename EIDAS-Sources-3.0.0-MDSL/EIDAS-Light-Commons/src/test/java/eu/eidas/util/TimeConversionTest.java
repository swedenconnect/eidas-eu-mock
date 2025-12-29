/*
 * Copyright (c) 2024 by European Commission
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

package eu.eidas.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Test class illustrating the transition from Joda-Time to Java 8 Time
 * @since 2.9.0
 * @deprecated in 2.9.0
 */
@Deprecated
public class TimeConversionTest {

    private static final int SECONDS_TO_ADD = 30;
    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    private static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final org.joda.time.format.DateTimeFormatter JODA_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd")
            .withLocale(Locale.ENGLISH)
            .withZoneUTC();

    private static final java.time.format.DateTimeFormatter JAVA_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .toFormatter(Locale.ENGLISH);

    private static final org.joda.time.format.DateTimeFormatter JODA_LIGHT_TOKEN_DATE_FORMAT =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss SSS");

    private static final DateTimeFormatter JAVA_LIGHT_TOKEN_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");

    /**
     * Tests the conversion from String to Joda-Time DateTime and back to String.
     * Verifies that the initial and final DateTime objects are equal.
     * This test uses Joda-Time classes.
     */
    @Test
    public void testStringToJodaAndBack() {
        String dateTimeString = "2023-07-15T10:00:00.000Z";
        DateTime dateTime = DateTime.parse(dateTimeString);
        String formatted = dateTime.toString();
        DateTime parsed = DateTime.parse(formatted);
        Assert.assertEquals(dateTime, parsed);
    }

    /**
     * Tests the conversion from String to Java 8 ZonedDateTime and back to String.
     * Verifies that the initial and final ZonedDateTime objects are equal.
     * This test uses Java Time classes.
     */
    @Test
    public void testStringToJavaTimeAndBack() {
        String dateTimeString = "2023-07-15T10:00:00Z";
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        String formatted = zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        ZonedDateTime parsed = ZonedDateTime.parse(formatted, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        Assert.assertEquals(zonedDateTime, parsed);
    }

    /**
     * Tests the conversion from Joda-Time DateTime to Java 8 ZonedDateTime and back to Joda-Time DateTime.
     * Verifies that the initial and final DateTime objects are equal.
     * This test demonstrates interoperability between Joda-Time and Java Time.
     */
    @Test
    public void testJodaToJavaTimeAndBack() {
        DateTime jodaDateTime = new DateTime(2023, 7, 15, 10, 0, DateTimeZone.UTC);
        Instant instant = Instant.ofEpochMilli(jodaDateTime.getMillis());
        ZonedDateTime javaTimeDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        DateTime convertedBack = new DateTime(javaTimeDateTime.toInstant().toEpochMilli(), DateTimeZone.UTC);
        Assert.assertEquals(jodaDateTime, convertedBack);
    }

    /**
     * Tests the conversion from a formatted date string to Joda-Time DateTime,
     * then to Java 8 ZonedDateTime, and back to Joda-Time DateTime.
     * Verifies that the initial and final DateTime objects are equal.
     * This test demonstrates interoperability between Joda-Time and Java Time.
     */
    @Test
    public void testStringToJodaToJavaTimeAndBack() {
        String dateTimeStr = "2023-07-15T10:00:00.000Z";
        DateTime jodaDateTime = DateTime.parse(dateTimeStr);

        Instant instant = Instant.ofEpochMilli(jodaDateTime.getMillis());
        ZonedDateTime javaTimeDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        DateTime convertedBack = new DateTime(javaTimeDateTime.toInstant().toEpochMilli(), DateTimeZone.UTC);

        Assert.assertEquals(jodaDateTime, convertedBack);
    }

    /**
     * Tests the conversion from Java 8 ZonedDateTime to Joda-Time DateTime and back to Java 8 ZonedDateTime.
     * Verifies that the initial and final ZonedDateTime objects are equal, ignoring zone ID differences in the string representation.
     * This test demonstrates interoperability between Java Time and Joda-Time.
     */
    @Test
    public void testJavaTimeToJodaAndBack() {
        ZonedDateTime javaTimeDateTime = ZonedDateTime.of(2023, 7, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        Instant instant = javaTimeDateTime.toInstant();
        DateTime jodaDateTime = new DateTime(instant.toEpochMilli(), DateTimeZone.UTC);
        ZonedDateTime convertedBack = ZonedDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.getMillis()), ZoneId.of("UTC"));

        Assert.assertEquals(javaTimeDateTime.toInstant(), convertedBack.toInstant());
    }

    /**
     * Tests the conversion from a formatted date-time string to Java 8 LocalDateTime,
     * then to Joda-Time DateTime, and back to Java 8 LocalDateTime.
     * Verifies that the initial and final LocalDateTime objects are equal.
     * This test demonstrates interoperability between Java Time and Joda-Time.
     */
    @Test
    public void testStringToJavaTimeToJodaAndBack() {
        String dateTimeStr = "2023-07-15T10:00:00";
        LocalDateTime javaTimeDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Instant instant = javaTimeDateTime.toInstant(ZoneOffset.UTC);
        DateTime jodaDateTime = new DateTime(instant.toEpochMilli(), DateTimeZone.UTC);
        LocalDateTime convertedBack = LocalDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.getMillis()), ZoneId.of("UTC"));

        Assert.assertEquals(javaTimeDateTime, convertedBack);
    }

    /**
     * Tests the conversion from String (simple date) to LocalDate and back to String.
     * Verifies that the initial and final date strings are equal.
     */
    @Test
    public void testStringToDateConversion() {
        String simpleDateString = "2024-07-18";
        java.time.LocalDate localDate = java.time.LocalDate.parse(simpleDateString, java.time.format.DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT));

        DateTime jodaDateTime = new DateTime(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(), DateTimeZone.UTC);

        String convertedDateString = jodaDateTime.toString(SIMPLE_DATE_FORMAT);
        Assert.assertEquals(simpleDateString, convertedDateString);
    }

    /**
     * Tests the conversion from String (simple date time) to LocalDateTime and back to String.
     * Verifies that the initial and final date time strings are equal.
     */
    @Test
    public void testStringToDateTimeConversion() {
        String simpleDateTimeString = "2024-07-18 12:00:00";
        LocalDateTime localDateTime = LocalDateTime.parse(simpleDateTimeString, DateTimeFormatter.ofPattern(SIMPLE_DATE_TIME_FORMAT));

        DateTime jodaDateTime = new DateTime(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(), DateTimeZone.UTC);

        org.joda.time.format.DateTimeFormatter jodaFormatter = org.joda.time.format.DateTimeFormat.forPattern(SIMPLE_DATE_TIME_FORMAT).withZoneUTC();
        String convertedDateTimeString = jodaDateTime.toString(jodaFormatter);

        Assert.assertEquals(simpleDateTimeString, convertedDateTimeString);
    }

    /**
     * Tests the conversion from Joda-Time DateTime to LocalDate.
     * Verifies that the conversion is accurate by converting back to Joda-Time DateTime.
     */
    @Test
    public void testJodaTimeToJavaTimeConversion() {
        DateTime jodaDateTime = new DateTime(2024, 7, 18, 0, 0, DateTimeZone.UTC);
        java.time.LocalDate localDate = java.time.LocalDate.of(jodaDateTime.getYear(), jodaDateTime.getMonthOfYear(), jodaDateTime.getDayOfMonth());

        DateTime convertedJodaDateTime = new DateTime(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(), DateTimeZone.UTC);

        Assert.assertEquals(jodaDateTime.getMillis(), convertedJodaDateTime.getMillis());
    }

    /**
     * Tests the conversion from a formatted date-time string to Joda-Time DateTime,
     * then to Java 8 LocalDate, and back to Joda-Time DateTime.
     * Verifies that the initial and final DateTime objects are equal.
     * This test demonstrates interoperability between Joda-Time and Java Time.
     */
    @Test
    public void testStringToJodaTimeToJavaTimeAndBack() {
        String dateTimeStr = "2024-07-18T00:00:00.000Z";
        DateTime jodaDateTime = DateTime.parse(dateTimeStr);

        java.time.LocalDate localDate = java.time.LocalDate.of(jodaDateTime.getYear(), jodaDateTime.getMonthOfYear(), jodaDateTime.getDayOfMonth());
        DateTime convertedJodaDateTime = new DateTime(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(), DateTimeZone.UTC);

        Assert.assertEquals(jodaDateTime.getMillis(), convertedJodaDateTime.getMillis());
    }

    /**
     * Tests the conversion from Joda-Time DateTime to LocalDateTime.
     * Verifies that the conversion is accurate by converting back to Joda-Time DateTime.
     */
    @Test
    public void testJodaTimeToJavaTimeConversion_DateTime() {
        DateTime jodaDateTime = new DateTime(2024, 7, 18, 12, 0, DateTimeZone.UTC);
        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.of(
                jodaDateTime.getYear(),
                jodaDateTime.getMonthOfYear(),
                jodaDateTime.getDayOfMonth(),
                jodaDateTime.getHourOfDay(),
                jodaDateTime.getMinuteOfHour(),
                jodaDateTime.getSecondOfMinute(),
                jodaDateTime.getMillisOfSecond() * 1000000
        );

        DateTime convertedJodaDateTime = new DateTime(
                localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                DateTimeZone.UTC
        );

        Assert.assertEquals(jodaDateTime.getMillis(), convertedJodaDateTime.getMillis());
    }

    /**
     * Tests the conversion from Joda-Time DateTime string to LocalDateTime.
     * Verifies that the conversion is accurate by converting back to Joda-Time DateTime.
     */
    @Test
    public void testStringToJodaTimeToJavaTimeConversion_DateTime() {
        String jodaDateTimeStr = "2024-07-18T12:00:00.000Z";
        DateTime jodaDateTime = DateTime.parse(jodaDateTimeStr);
        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.of(
                jodaDateTime.getYear(),
                jodaDateTime.getMonthOfYear(),
                jodaDateTime.getDayOfMonth(),
                jodaDateTime.getHourOfDay(),
                jodaDateTime.getMinuteOfHour(),
                jodaDateTime.getSecondOfMinute(),
                jodaDateTime.getMillisOfSecond() * 1000000
        );

        DateTime convertedJodaDateTime = new DateTime(
                localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                DateTimeZone.UTC);

        Assert.assertEquals(jodaDateTime.getMillis(), convertedJodaDateTime.getMillis());
    }

    /**
     * Tests the parsing of a date string using Joda-Time DateTime.
     * Verifies that the parsed DateTime object has the expected year, month, and day.
     */
    @Test
    public void testJodaTimeParsing() {
        String dateStr = "2023-07-15";
        DateTime jodaDateTime = JODA_FORMAT.parseDateTime(dateStr);
        Assert.assertEquals(2023, jodaDateTime.getYear());
        Assert.assertEquals(7, jodaDateTime.getMonthOfYear());
        Assert.assertEquals(15, jodaDateTime.getDayOfMonth());
    }

    /**
     * Tests the parsing of a date string using Java Time LocalDate.
     * Verifies that the parsed LocalDate object has the expected year, month, and day.
     */
    @Test
    public void testJavaTimeParsing() {
        String dateStr = "2023-07-15";
        LocalDate javaLocalDate = LocalDate.parse(dateStr, JAVA_FORMAT);
        Assert.assertEquals(2023, javaLocalDate.getYear());
        Assert.assertEquals(7, javaLocalDate.getMonthValue());
        Assert.assertEquals(15, javaLocalDate.getDayOfMonth());
    }

    /**
     * Tests the parsing of a date string using both Joda-Time DateTime and Java Time LocalDate.
     * Verifies that the parsed year, month, and day are equal in both DateTime and LocalDate.
     */
    @Test
    public void testParsingEquality() {
        String dateStr = "2023-07-15";

        DateTime jodaDateTime = JODA_FORMAT.parseDateTime(dateStr);
        LocalDate javaLocalDate = LocalDate.parse(dateStr, JAVA_FORMAT);

        Assert.assertEquals(jodaDateTime.getYear(), javaLocalDate.getYear());
        Assert.assertEquals(jodaDateTime.getMonthOfYear(), javaLocalDate.getMonthValue());
        Assert.assertEquals(jodaDateTime.getDayOfMonth(), javaLocalDate.getDayOfMonth());
    }

    /**
     * Tests the handling of an invalid date string using Java Time LocalDate parsing.
     * Verifies that a DateTimeParseException is thrown.
     */
    @Test(expected = DateTimeParseException.class)
    public void testInvalidDateParsingJavaTime() {
        String invalidDateStr = "2023-07-32";
        LocalDate.parse(invalidDateStr, JAVA_FORMAT);
    }

    /**
     * Tests the handling of an invalid date string using Joda-Time DateTime parsing.
     * Verifies that an IllegalArgumentException is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDateParsingJodaTime() {
        String invalidDateStr = "2023-07-32";
        JODA_FORMAT.parseDateTime(invalidDateStr);
    }

    /**
     * Tests the formatting of a date using Joda-Time DateTimeFormatter.
     * Verifies that the formatted string is as expected.
     */
    @Test
    public void testJodaTimeFormatting() {
        DateTime dateTime = new DateTime(2023, 7, 15, 10, 0, 0, 123, DateTimeZone.UTC);
        String formatted = JODA_LIGHT_TOKEN_DATE_FORMAT.print(dateTime);
        Assert.assertEquals("2023-07-15 10:00:00 123", formatted);
    }

    /**
     * Tests the formatting of a date using Java Time DateTimeFormatter.
     * Verifies that the formatted string is as expected.
     */
    @Test
    public void testJavaTimeFormatting() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 7, 15, 10, 0, 0, 123_000_000, ZoneOffset.UTC);
        String formatted = JAVA_LIGHT_TOKEN_DATE_FORMAT.format(zonedDateTime);
        Assert.assertEquals("2023-07-15 10:00:00 123", formatted);
    }

    /**
     * Tests the formatting of a date using Java Time DateTimeFormatter.
     * Verifies that the formatted string is as expected.
     */
    @Test
    public void testStringJavaTimeFormatting() {
        String dateTimeStr = "2023-07-15T10:00:00.123";
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr);
        String formatted = JAVA_LIGHT_TOKEN_DATE_FORMAT.format(localDateTime.atZone(ZoneOffset.UTC));
        Assert.assertEquals("2023-07-15 10:00:00 123", formatted);
    }

    /**
     * Tests the formatting of a date-time string using Java Time DateTimeFormatter.
     * Verifies that the formatted string is as expected.
     */
    @Test
    public void testStringToJavaTimeFormatting() {
        String dateTimeStr = "2023-07-15T10:00:00.123";
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String formatted = JAVA_LIGHT_TOKEN_DATE_FORMAT.format(localDateTime.atZone(ZoneOffset.UTC));
        Assert.assertEquals("2023-07-15 10:00:00 123", formatted);
    }

    /**
     * Tests the timestamp formatting and parsing between Joda-Time and Java Time.
     * <p>
     * This test verifies that a given timestamp string can be parsed using Joda-Time and Java Time,
     * and that the resulting epoch milliseconds from both parsing methods are equal.
     */
    @Test
    public void testLightRequestTimestamp() {
        final String expected = "2023-07-15 10:00:00 123";

        final DateTime dateTime = JODA_LIGHT_TOKEN_DATE_FORMAT.parseDateTime(expected);
        final long millis = dateTime.getMillis();

        final LocalDateTime localDateTime = LocalDateTime.parse(expected, JAVA_LIGHT_TOKEN_DATE_FORMAT);
        final long epochMilli = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();

        Assert.assertEquals(millis, epochMilli);
    }

    /**
     * Tests that the formatted output of Joda-Time and Java Time are equivalent.
     */
    @Test
    public void testJodaAndJavaTimeFormattingEquality() {
        DateTime jodaDateTime = new DateTime(2023, 7, 15, 10, 0, 0, 123, DateTimeZone.UTC);
        ZonedDateTime javaZonedDateTime = ZonedDateTime.of(2023, 7, 15, 10, 0, 0, 123_000_000, ZoneOffset.UTC);

        String jodaFormatted = JODA_LIGHT_TOKEN_DATE_FORMAT.print(jodaDateTime);
        String javaFormatted = JAVA_LIGHT_TOKEN_DATE_FORMAT.format(javaZonedDateTime);

        Assert.assertEquals(jodaFormatted, javaFormatted);
    }

    /**
     * Tests that the formatted output of String Joda-Time and String Java Time are equivalent.
     */
    @Test
    public void testStringJodaAndJavaTimeFormattingEquality() {
        String dateTimeStr = "2023-07-15T10:00:00.123";

        DateTime jodaDateTime = DateTime.parse(dateTimeStr);
        LocalDateTime javaLocalDateTime = LocalDateTime.parse(dateTimeStr);

        String jodaFormatted = JODA_LIGHT_TOKEN_DATE_FORMAT.print(jodaDateTime);
        String javaFormatted = JAVA_LIGHT_TOKEN_DATE_FORMAT.format(javaLocalDateTime.atZone(ZoneOffset.UTC));

        Assert.assertEquals(jodaFormatted, javaFormatted);
    }

    /**
     * Tests the consistency of date-time formatting between Joda-Time and Java Time.
     * <p>
     * This test verifies that a given epoch millisecond value can be converted to a date string using
     * both Joda-Time and Java Time, and that the resulting date strings are equal.
     */
    @Test
    public void testDateTimeAttributeValueMarshaller() {
        final long millis = 1215416846466L;

        final DateTime dateTime = new DateTime(Long.valueOf(millis), DateTimeZone.UTC);
        final org.joda.time.format.DateTimeFormatter jodaTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                .withChronology(ISOChronology.getInstance())
                .withLocale(Locale.ENGLISH)
                .withZoneUTC();
        final String jodaDateString = jodaTimeFormatter.print(dateTime);

        final Instant instant = Instant.ofEpochMilli(millis);
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        final java.time.format.DateTimeFormatter javaTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withLocale(Locale.ENGLISH)
                .withZone(java.time.ZoneOffset.UTC);
        final String javaDateString = javaTimeFormatter.format(zonedDateTime);

        Assert.assertEquals(jodaDateString, javaDateString);
    }

    /**
     * Tests the Joda-Time implementation of adding seconds to a current time.
     * Verifies that the final DateTime object has the expected time after adding the specified seconds.
     */
    @Test
    public void testJodaTimeAddition() {
        DateTime currentTime = new DateTime(2023, 7, 15, 10, 0, DateTimeZone.UTC);
        DateTime notOnOrAfter = currentTime.plusSeconds(SECONDS_TO_ADD);
        Assert.assertEquals(new DateTime(2023, 7, 15, 10, 0, 30, DateTimeZone.UTC), notOnOrAfter);
    }

    /**
     * Tests the Java Time implementation of adding seconds to a current time.
     * Verifies that the final ZonedDateTime object has the expected time after adding the specified seconds.
     */
    @Test
    public void testJavaTimeAddition() {
        ZonedDateTime currentTime = ZonedDateTime.of(2023, 7, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime notOnOrAfter = currentTime.plusSeconds(SECONDS_TO_ADD);
        Assert.assertEquals(ZonedDateTime.of(2023, 7, 15, 10, 0, 30, 0, ZoneOffset.UTC), notOnOrAfter);
    }

    /**
     * Tests the Java Time implementation of adding seconds to a current time.
     * Verifies that the final ZonedDateTime object has the expected time after adding the specified seconds.
     */
    @Test
    public void testStringJavaTimeAddition() {
        String currentTimeStr = "2023-07-15T10:00:00";

        ZonedDateTime currentTime = ZonedDateTime.parse(currentTimeStr + "Z");
        ZonedDateTime notOnOrAfter = currentTime.plusSeconds(SECONDS_TO_ADD);
        ZonedDateTime expectedTime = ZonedDateTime.of(2023, 7, 15, 10, 0, 30, 0, ZoneOffset.UTC);

        Assert.assertEquals(expectedTime, notOnOrAfter);
    }

    /**
     * Compares the results of Joda-Time and Java Time to ensure they produce the same output.
     * Verifies that both implementations result in the same instant in time after adding the specified seconds.
     */
    @Test
    public void testTimeAdditionEquality() {
        DateTime jodaCurrentTime = new DateTime(2023, 7, 15, 10, 0, DateTimeZone.UTC);
        DateTime jodaNotOnOrAfter = jodaCurrentTime.plusSeconds(SECONDS_TO_ADD);

        ZonedDateTime javaCurrentTime = ZonedDateTime.of(2023, 7, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime javaNotOnOrAfter = javaCurrentTime.plusSeconds(SECONDS_TO_ADD);

        long jodaMillis = jodaNotOnOrAfter.getMillis();
        long javaMillis = javaNotOnOrAfter.toInstant().toEpochMilli();

        Assert.assertEquals(jodaMillis, javaMillis);
    }

    /**
     * Tests the Joda-Time implementation of converting Instant to DateTime.
     * Verifies that the converted DateTime object has the expected time.
     */
    @Test
    public void testJodaTimeConversion() {
        Instant testInstant = Instant.parse("2023-07-15T10:00:00Z");
        DateTime notOnOrAfter = new DateTime(testInstant.toEpochMilli(), DateTimeZone.UTC);

        Assert.assertEquals(2023, notOnOrAfter.getYear());
        Assert.assertEquals(7, notOnOrAfter.getMonthOfYear());
        Assert.assertEquals(15, notOnOrAfter.getDayOfMonth());
        Assert.assertEquals(10, notOnOrAfter.getHourOfDay());
        Assert.assertEquals(0, notOnOrAfter.getMinuteOfHour());
        Assert.assertEquals(0, notOnOrAfter.getSecondOfMinute());
    }

    /**
     * Tests the Java Time implementation of converting Instant to ZonedDateTime.
     * Verifies that the converted ZonedDateTime object has the expected time.
     */
    @Test
    public void testJavaTimeConversion() {
        Instant testInstant = Instant.parse("2023-07-15T10:00:00Z");
        ZonedDateTime notOnOrAfter = ZonedDateTime.ofInstant(testInstant, ZoneOffset.UTC);

        Assert.assertEquals(2023, notOnOrAfter.getYear());
        Assert.assertEquals(7, notOnOrAfter.getMonthValue());
        Assert.assertEquals(15, notOnOrAfter.getDayOfMonth());
        Assert.assertEquals(10, notOnOrAfter.getHour());
        Assert.assertEquals(0, notOnOrAfter.getMinute());
        Assert.assertEquals(0, notOnOrAfter.getSecond());
    }

    /**
     * Compares the results of Joda-Time and Java Time to ensure they produce the same output.
     * Verifies that both implementations result in the same instant in time after conversion.
     */
    @Test
    public void testTimeConversionEquality() {
        final Instant testInstant = Instant.parse("2023-07-15T10:00:00Z");

        DateTime jodaNotOnOrAfter = new DateTime(testInstant.toEpochMilli());
        long jodaMillis = jodaNotOnOrAfter.getMillis();

        ZonedDateTime javaNotOnOrAfter = ZonedDateTime.ofInstant(testInstant, ZoneId.systemDefault());
        long javaMillis = javaNotOnOrAfter.toInstant().toEpochMilli();

        Assert.assertEquals(jodaMillis, javaMillis);
    }

    /**
     * Tests the conversion from Joda-Time DateTime to Java Time Instant.
     * The old code converts a Joda-Time DateTime to Java Time Instant using ofEpochMilli.
     * The new code converts a Java Time ZonedDateTime to Java Time Instant using toInstant.
     * This test verifies that both methods produce the same Instant value.
     */
    @Test
    public void testJodaToJavaInstantConversion() {
        DateTime issueDateTime = new DateTime(2023, 7, 15, 10, 0, DateTimeZone.UTC);
        Instant jodaInstant = issueDateTime != null ? Instant.ofEpochMilli(issueDateTime.getMillis()) : null;

        ZonedDateTime issueDateTimeJava = ZonedDateTime.of(2023, 7, 15, 10, 0, 0, 0, ZoneId.of("UTC"));
        Instant javaInstant = issueDateTimeJava != null ? issueDateTimeJava.toInstant() : null;

        Assert.assertEquals(jodaInstant, javaInstant);
    }

    /**
     * Tests the issue with comparing two dates in different time zones.
     * Converts both dates to UTC and verifies the comparison.
     */
    @Test
    public void testTimeZoneDifferenceWithStringDates() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String notOnOrAfterString = "2024-08-06T16:15:00.703+02:00";
        String nowString = "2024-08-06T14:10:01.703Z";

        ZonedDateTime notOnOrAfter = ZonedDateTime.parse(notOnOrAfterString, formatter);
        ZonedDateTime now = ZonedDateTime.parse(nowString, formatter);

        ZonedDateTime notOnOrAfterUTC = notOnOrAfter.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime nowUTC = now.withZoneSameInstant(ZoneOffset.UTC);

        Assert.assertTrue(notOnOrAfterUTC.isAfter(nowUTC));
    }

}
