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

import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.util.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

/**
 * This class holds static helper methods for Date Operations.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.4 $, $Date: 2010-11-17 05:15:28 $
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
   *
   * @return The dateValue fulfilled.
   */
  @Nonnull
  private static String fulfilDate(@Nonnull String dateValue) {
    Preconditions.checkNotLonger(dateValue, "dateValue", FULL_DATE_SIZE);

    final StringBuilder strBuf = new StringBuilder(FULL_DATE_SIZE);
    strBuf.append(dateValue);
    // if the IdP just provides the year then we must fulfil the date.
    if (dateValue.length() == YEAR_DATE_SIZE) {
      strBuf.append(EIDASValues.LAST_MONTH.toString());
    }
    // if the IdP provides the year and the month then we must fulfil the
    // date.
    if (dateValue.length() == MONTH_DATE_SIZE
      || strBuf.length() == MONTH_DATE_SIZE) {
      // IdP doesn't provide the day, so we will use DateTime to
      // calculate it.
      final String noDayCons = EIDASValues.NO_DAY_DATE_FORMAT.toString();
      final DateTimeFormatter fmt = DateTimeFormat.forPattern(noDayCons);
      final DateTime dateTime = fmt.parseDateTime(strBuf.toString());
      // Append the last month's day.
      strBuf.append(dateTime.dayOfMonth().withMaximumValue().getDayOfMonth());
    }

    return strBuf.toString();
  }

  /**
   * Validates the dateValue format: a) if has a valid size; b) if has a numeric
   * value; Note: dateValue must have the format yyyyMMdd.
   *
   * @param dateValueTmp The date to be validated.
   * @param pattern The accepted date format.
   *
   * @return true if the date has a valid format.
   */
  public static boolean isValidFormatDate(final String dateValueTmp,
    final String pattern) {

    boolean retVal = true;
    try {
      final String dateValue = fulfilDate(dateValueTmp);

      final DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
      fmt.parseDateTime(dateValue);
    } catch (final Exception e) {
      // We catch Exception because we only have to return false
      // value!
        LOG.info("BUSINESS EXCEPTION : error validating date {}", e);
        retVal = false;
    }
    return retVal;
  }

  /**
   * Calculates the age for a given date string.
   *
   * @param dateVal The date to be validated.
   * @param now The current date.
   * @param pattern The date pattern.
   *
   * @return The age value.
   */
  public static int calculateAge(final String dateVal, final DateTime now,
    final String pattern) {

    if (isValidFormatDate(dateVal, pattern)) {
      try {
        final String dateValueTemp = fulfilDate(dateVal);
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
        final DateTime dateTime = fmt.parseDateTime(dateValueTemp);
        // Calculating age
        final Years age = Years.yearsBetween(dateTime, now);

        return age.getYears();
      } catch (final IllegalArgumentException e) {
        LOG.info("BUSINESS EXCEPTION : Invalid date format (" + pattern
                + ") or an invalid dateValue.");
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

  /**
   * Generates the current timestamp.
   *
   * @return timestamp The current timestamp
   */
  public static Timestamp currentTimeStamp() {
    final GregorianCalendar cal = new GregorianCalendar();
    final long millis = cal.getTimeInMillis();
    return new Timestamp(millis);
  }

}
