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
package eu.eidas.auth.commons;

import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * This class holds static helper methods for Date Operations.
 */
public final class DateUtil {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class.getName());

    /**
     * yyyy Date format size.
     */
    private static final int YEAR_DATE_SIZE = 4;

    /**
     * yyyyMM Date format size.
     */
    private static final int MONTH_DATE_SIZE = 6;

    /**
     * yyyyMMdd Date format size.
     */
    private static final int FULL_DATE_SIZE = 8;

    /**
     * Private constructor. Prevents the class from being instantiated.
     */
    private DateUtil() {
        // empty constructor
    }

    /**
     * Fulfils dateValue with a valid date. The following rules are applied:
     * a) If the dateValue only contains the year then fulfils with last year's day.
     * e.g. this method returns 19951231 to the 1995 dateValue.
     * b) If the dateValue contains the year and the month then fulfils with last month's day.
     * e.g. this method returns 19950630 to the 199505 dateValue.
     *
     * @param dateValue The date to be fulfilled.
     * @return The dateValue fulfilled.
     */
    @Nonnull
    private static String fulfilDate(@Nonnull String dateValue) {
        Preconditions.checkNotLonger(dateValue, "dateValue", FULL_DATE_SIZE);

        final StringBuilder strBuf = new StringBuilder(FULL_DATE_SIZE);
        strBuf.append(dateValue);
        // if the IdP just provides the year then we must fulfil the date.
        try {
            if (dateValue.length() == YEAR_DATE_SIZE) {
                strBuf.append(EIDASValues.LAST_MONTH);
                YearMonth yearMonth = YearMonth.parse(strBuf.toString(), DateTimeFormatter.ofPattern("yyyyMM"));
                strBuf.append(yearMonth.atEndOfMonth().getDayOfMonth());
            } else if (dateValue.length() == MONTH_DATE_SIZE) {
                YearMonth yearMonth = YearMonth.parse(dateValue, DateTimeFormatter.ofPattern("yyyyMM"));
                strBuf.append(yearMonth.atEndOfMonth().getDayOfMonth());
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateValue, e);
        }

        return strBuf.toString();
    }

    /**
     * Validates the dateValue format: a) if has a valid size; b) if has a numeric
     * value; Note: dateValue must have the format yyyyMMdd.
     *
     * @param dateValueTmp The date to be validated.
     * @param pattern      The accepted date format.
     * @return true if the date has a valid format.
     */
    public static boolean isValidFormatDate(final String dateValueTmp,
                                            final String pattern) {

        boolean retVal = true;
        try {
            final String dateValue = fulfilDate(dateValueTmp);

            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(dateValue, fmt);
        } catch (IllegalArgumentException e) {
            // We catch Exception because we only have to return false value!
            LOG.info("BUSINESS EXCEPTION : error validating date {}", e);
            retVal = false;
        }
        return retVal;
    }

    /**
     * Calculates the age for a given date string.
     *
     * @param dateVal The date to be validated.
     * @param now     The current date.
     * @param pattern The date pattern.
     * @return The age value.
     */
    public static int calculateAge(final String dateVal, final LocalDate now,
                                   final String pattern) {

        if (isValidFormatDate(dateVal, pattern)) {
            try {
                final String dateValueTemp = fulfilDate(dateVal);
                final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
                final LocalDate date = LocalDate.parse(dateValueTemp, fmt);
                // Calculating age
                return Period.between(date, now).getYears();
            } catch (final IllegalArgumentException e) {
                LOG.info("BUSINESS EXCEPTION : Invalid date format (" + pattern + ") or an invalid dateValue.");
                throw new SecurityEIDASException(
                        EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorMessage()),
                        e);
            }
        } else {
            LOG.info("BUSINESS EXCEPTION : Couldn't calculate Age, invalid date!");
            throw new SecurityEIDASException(
                    EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorMessage()));
        }

    }

}
