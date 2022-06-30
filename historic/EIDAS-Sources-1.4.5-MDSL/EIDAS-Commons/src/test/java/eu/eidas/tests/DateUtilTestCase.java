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
package eu.eidas.tests;

import java.sql.Timestamp;
import java.util.Properties;

import eu.eidas.auth.commons.EidasParameters;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.eidas.auth.commons.DateUtil;
import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;

/**
 * The DateUtil's Test Case.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.2 $, $Date: 2010-11-17 05:17:03 $
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
  private static final DateTime TESTDATE = new DateTime(2011, 10, 10, 15, 20,
    0, 0);

  /**
   * Init DateUtilTestCase class.
   */
  @BeforeClass
  public static void runsBeforeTheTestSuite() {
    /*final Properties configs = new Properties();
    configs.setProperty("invalidAgeDateValue.code", "35");
    configs.setProperty("invalidAttributeValue.code", "34");
    configs
      .setProperty(
        "invalidAttributeValue.message",
        "Unexpected or invalid content was encountered within a "
        + "<saml:Attribute> or <saml:AttributeValue> element.");
    EIDASUtil.createInstance(configs);*/
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given year against
   * the testDate: 2011-10-10 15:20:00.0. Must return 10.
   */
  @Test
  public void calculateAgeFromYear() {
    Assert.assertTrue(TEN == DateUtil.calculateAge("2000", TESTDATE, FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given year and month
   * against the testDate: 2011-10-10 15:20:00.0. Must return 11.
   */
  @Test
  public void calculateAgeFromEarlyMonth() {
    Assert.assertTrue(ELEVEN == DateUtil.calculateAge("200001", TESTDATE,
      FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given year and month
   * against the testDate: 2011-10-10 15:20:00.0. Must return 10.
   */
  @Test
  public void calculateAgeFromSameMonth() {
    Assert.assertTrue(TEN == DateUtil.calculateAge("200010", TESTDATE, FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given year and month
   * against the testDate: 2011-10-10 15:20:00.0. Must return 10.
   */
  @Test
  public void calculateAgeFromLaterMonth() {
    Assert.assertTrue(TEN == DateUtil.calculateAge("200011", TESTDATE, FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return 11.
   */
  @Test
  public void calculateAgeFromEarlyFullDate() {
    Assert.assertTrue(ELEVEN == DateUtil.calculateAge("20000101", TESTDATE,
      FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return 11.
   */
  @Test
  public void calculateAgeFromSameDay() {
    Assert.assertTrue(ELEVEN == DateUtil.calculateAge("20001010", TESTDATE,
      FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return 10.
   */
  @Test
  public void calculateAgeFromLaterFullDate() {
    Assert.assertTrue(TEN == DateUtil
      .calculateAge("20001011", TESTDATE, FORMAT));
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return a
   * SecurityEIDASException exception.
   */
  @Test(expected = SecurityEIDASException.class)
  public void calculateAgeFromInvalidDate() {
    DateUtil.calculateAge("200", TESTDATE, FORMAT);
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return a
   * SecurityEIDASException exception.
   */
  @Test(expected = SecurityEIDASException.class)
  public void calculateAgeFromInvalidMonth() {
    DateUtil.calculateAge("200013", TESTDATE, FORMAT);
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return a
   * SecurityEIDASException exception.
   */
  @Test(expected = SecurityEIDASException.class)
  public void calculateAgeFromInvalidDay() {
    DateUtil.calculateAge("20000230", TESTDATE, FORMAT);
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return a
   * SecurityEIDASException exception.
   */
  @Test(expected = SecurityEIDASException.class)
  public void calculateAgeFromNullDate() {
    DateUtil.calculateAge(null, TESTDATE, FORMAT);
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return a
   * SecurityEIDASException exception.
   */
  @Test(expected = SecurityEIDASException.class)
  public void calculateAgeFromNullCurDate() {
    DateUtil.calculateAge("2000", null, FORMAT);
  }

  /**
   * Tests the {@link DateUtil#calculateAge} method for the given full date
   * against the testDate: 2011-10-10 15:20:00.0. Must return a
   * SecurityEIDASException exception.
   */
  @Test(expected = SecurityEIDASException.class)
  public void calculateAgeFromNullFormat() {
    DateUtil.calculateAge("2000", TESTDATE, null);
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
  @Test
  public void isValidFormatDateInvalidYear() {
    Assert.assertFalse(DateUtil.isValidFormatDate("200", FORMAT));
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
   * Must return false.
   */
  @Test
  public void isValidFormatDateInvalidDate() {
    Assert.assertFalse(DateUtil.isValidFormatDate("20010229", FORMAT));
  }

  /**
   * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
   * Must return false.
   */
  @Test
  public void isValidFormatDateNullDate() {
    Assert.assertFalse(DateUtil.isValidFormatDate(null, FORMAT));
  }

  /**
   * Tests the {@link DateUtil#isValidFormatDate} method for the given year.
   * Must return false.
   */
  @Test
  public void isValidFormatDateNullFormat() {
    Assert.assertFalse(DateUtil.isValidFormatDate("2000", null));
  }

  /**
   * Tests the {@link DateUtil#currentTimeStamp()} method for the current
   * TimeStamp (TS). Must return true.
   * Obvious test not really testing the dateUtil class !!!
   */
  @Test
  public void testCurrentTimeStampBefore() {
    Timestamp ts = DateUtil.currentTimeStamp();
    Assert.assertNotSame(ts, DateUtil.currentTimeStamp());
  }

  /**
   * Tests the {@link DateUtil#currentTimeStamp()} method for the current
   * TimeStamp (TS). Must return true.
   */
  @Test
  public void testCurrentTimeStampAfter() {
    Timestamp ts = DateUtil.currentTimeStamp();
    if (DateUtil.currentTimeStamp().before(ts)){
       Assert.fail("dateUtil current time stamp before the one instantiated previously !");
    }
    //Assert.assertEquals(DateUtil.currentTimeStamp(), ts);
  }

}
