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
package eu.eidas.tests;

import eu.eidas.auth.commons.DateUtil;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Test class for {@link DateUtil}
 */
public final class DateUtilTestCase {

    /**
     * Format date.
     */
    private static final String FORMAT = "yyyyMMdd";

    /**
     * Expected 10 value.
     */
    private static final int TEN = 10;

    /**
     * Expected 11 value.
     */
    private static final int ELEVEN = 11;

    /**
     * The testing Date ("current" date).
     */
    private static final LocalDate TEST_DATE = LocalDate.of(2011, 10, 10);

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given year against
     * the testDate: 2011-10-10. Must return 10.
     */
    @Test
    public void calculateAgeFromYear() {
        Assert.assertTrue(TEN == DateUtil.calculateAge("2000", TEST_DATE, FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given year and month
     * against the testDate: 2011-10-10. Must return 11.
     */
    @Test
    public void calculateAgeFromEarlyMonth() {
        Assert.assertTrue(ELEVEN == DateUtil.calculateAge("200001", TEST_DATE,
                FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given year and month
     * against the testDate: 2011-10-10. Must return 10.
     */
    @Test
    public void calculateAgeFromSameMonth() {
        Assert.assertTrue(TEN == DateUtil.calculateAge("200010", TEST_DATE, FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given year and month
     * against the testDate: 2011-10-10. Must return 10.
     */
    @Test
    public void calculateAgeFromLaterMonth() {
        Assert.assertTrue(TEN == DateUtil.calculateAge("200011", TEST_DATE, FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10. Must return 11.
     */
    @Test
    public void calculateAgeFromEarlyFullDate() {
        Assert.assertTrue(ELEVEN == DateUtil.calculateAge("20000101", TEST_DATE,
                FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10. Must return 11.
     */
    @Test
    public void calculateAgeFromSameDay() {
        Assert.assertTrue(ELEVEN == DateUtil.calculateAge("20001010", TEST_DATE,
                FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10. Must return 10.
     */
    @Test
    public void calculateAgeFromLaterFullDate() {
        Assert.assertTrue(TEN == DateUtil
                .calculateAge("20001011", TEST_DATE, FORMAT));
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10. Must return a
     * DateTimeParseException exception.
     */
    @Test(expected = DateTimeParseException.class)
    public void calculateAgeFromInvalidDate() {
        DateUtil.calculateAge("200", TEST_DATE, FORMAT);
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10. Must return a
     * SecurityEIDASException exception.
     */
    @Test(expected = SecurityEIDASException.class)
    public void calculateAgeFromInvalidMonth() {
        DateUtil.calculateAge("200013", TEST_DATE, FORMAT);
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10.
     * The input date "20000230" (February 30th, 2000) is intentionally invalid.
     * {@link LocalDate#parse(CharSequence, DateTimeFormatter)} will adjust
     * the date to the last day of February 2000, which is February 29th.
     */
    @Test
    public void calculateAgeFromInvalidDay() {
        DateUtil.calculateAge("20000230", TEST_DATE, FORMAT);
    }

    /**
     * Tests the {@link DateUtil#calculateAge} method for the given full date
     * against the testDate: 2011-10-10. Must return a
     * SecurityEIDASException exception.
     */
    @Test(expected = SecurityEIDASException.class)
    public void calculateAgeFromNullDate() {
        DateUtil.calculateAge(null, TEST_DATE, FORMAT);
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
     * Must return true
     */
    @Test
    public void isValidFormatDateFromYear() {
        Assert.assertTrue(DateUtil.isValidFormatDate("2000", FORMAT));
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year and
     * month. Must return true.
     */
    @Test
    public void isValidFormatDateFromMonth() {
        Assert.assertTrue(DateUtil.isValidFormatDate("200001", FORMAT));
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
     * Must return false.
     */
    @Test
    public void isValidFormatDate() {
        Assert.assertTrue(DateUtil.isValidFormatDate("20000101", FORMAT));
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
     * Must return false.
     */
    @Test(expected = DateTimeParseException.class)
    public void isValidFormatDateInvalidYear() {
        DateUtil.isValidFormatDate("200", FORMAT);
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
     * Must return false.
     */
    @Test
    public void isValidFormatDateInvalidMonth() {
        Assert.assertFalse(DateUtil.isValidFormatDate("200013", FORMAT));
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
     * Must return true.
     */
    @Test
    public void isValidFormatDateInvalidDate() {
        Assert.assertTrue(DateUtil.isValidFormatDate("20010229", FORMAT));
    }

    /**
     * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
     * Must return false.
     */
    @Test
    public void isValidFormatDateNullDate() {
        Assert.assertFalse(DateUtil.isValidFormatDate(null, FORMAT));
    }

}
