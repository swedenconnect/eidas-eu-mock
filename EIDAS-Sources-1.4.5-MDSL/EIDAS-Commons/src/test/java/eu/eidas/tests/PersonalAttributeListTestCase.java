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

import eu.eidas.auth.commons.IPersonalAttributeList;
import eu.eidas.auth.commons.PersonalAttribute;
import eu.eidas.auth.commons.PersonalAttributeList;
import eu.eidas.auth.commons.PersonalAttributeString;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * The PersonalAttributeList's Test Case.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.5 $, $Date: 2010-11-17 05:17:02 $
 */
public final class PersonalAttributeListTestCase {

  /**
   * isAgeOver constant value.
   */
  private static final String ISAGEOVER_CONS = "http://www.stork.gov.eu/1.0/isAgeOver";

  /**
   * An empty attribute.
   */
  @SuppressWarnings("unused")
  private static final PersonalAttributeList EMPTY_ATTR_LIST =
    new PersonalAttributeList(0);

  /**
   * An attribute with a complex value (canonicalResidenceAddress).
   */
  private static PersonalAttribute complexAttrValue = null;

  /**
   * Simple attribute value list string.
   */
  private static final String SIMPLE_ATTRLIST =
    "http://www.stork.gov.eu/1.0/isAgeOver:true:[15,]:Available;";

  /**
   * Simple attribute value list string.
   */
  private static final String SIMPLE_ATTRLIST2 =
    "http://www.stork.gov.eu/1.0/isAgeOver:true:[18,]:Available;";

  /**
   * Simple attribute value list string.
   **/
    private static final String SIMPLE_ATTRLIST3 = "http://www.stork.gov.eu/1.0/isAgeOver:true:[15,]:Available;http://www.stork.gov.eu/1.0/isAgeOver:true:[18,]:Available;";

  /**
   * Simple attribute value list string.
   */
  private static final String COMPLEX_ATTRLIST =
    "canonicalResidenceAddress:true:[postalCode=4100,apartmentNumber=Ed. B,"
    + "state=Porto,countryCodeAddress=PT,streetNumber=379,"
    + "streetName=Avenida Sidonio Pais,town=Porto,]:Available;";
  /**
   * Mix attribute list string.
   */
  private static final String STR_MIX_ATTR_LIST =
    "isAgeOver:true:[15,]:Available;canonicalResidenceAddress:true:["
    + "postalCode=4100,apartmentNumber=Ed.B,state=Porto,countryCodeAddress=PT,"
    + "streetNumber=379,streetName=Avenida Sidonio Pais,town=Porto,]:"
    + "Available;";

  /**
   * Attribute List example.
   */
  @SuppressWarnings({ "serial" })
  private static final PersonalAttribute ATTR_VALUE = new PersonalAttribute("age",
    "age", true, new ArrayList<String>() {
      {
        add("15");
      }
    });

  /**
   * Init PersonalAttributeListTestCase class.
   */
  @SuppressWarnings("serial")
  @BeforeClass
  public static void runsBeforeTheTestSuite() {
    final Map<String, String> values = new HashMap<String, String>() {
      {
        put("countryCodeAddress", "PT");
        put("state", "Porto");
        put("town", "Porto");
        put("postalCode", "4100");
        put("streetName", "Avenida Sidonio Pais");
        put("streetNumber", "379");
        put("apartmentNumber", "Ed. B");
      }
    };

    complexAttrValue =
      new PersonalAttribute("canonicalResidenceAddress", "Address", true, values);

  }

  /**
   * Testing Personal Attribute List add method. Personal Attribute list must be
   * size 1 - Simple attribute.
   */
  @Test
  public void testAddSimpleAttr() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(ATTR_VALUE);
    Assert.assertEquals(1, attrList.size());
  }

  /**
   * Testing Personal Attribute List add method. Personal Attribute list must be
   * size 1 - Complex attribute.
   */
  @Test
  public void testAddCompleAttr() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(complexAttrValue);
    Assert.assertEquals(1, attrList.size());
  }

  /**
   * Testing Personal Attribute List add method. Personal Attribute list must be
   * size 0 - no attribute.
   */
  @Test
  public void testAddNull() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(null);
    Assert.assertEquals(0, attrList.size());
  }

  /**
   * Testing Personal Attribute List add method. Same attribute name added
   * twice. Personal Attribute list must be size 2 - IsAgeOver attribute added
   * twice.
   */
  @SuppressWarnings("serial")
  @Test
  public void testAddSameAttrName() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertEquals(2, attrList.size());
  }

  /**
   * Testing Personal Attribute List add method. Same attribute name added
   * twice. Personal Attribute list must be size 2 - IsAgeOver attribute added
   * twice.
   */
  @SuppressWarnings("serial")
  @Test
  public void testAddSameAttrNameEmpty() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertEquals(2, attrList.size());
  }

  /**
   * Testing Personal Attribute List put method. Personal Attribute list must be
   * size 1 - Simple Value.
   */
  @Test
  public void testPutSimpleAttr() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(ATTR_VALUE);
    Assert.assertEquals(1, attrList.size());
  }

  /**
   * Testing Personal Attribute List put method. Personal Attribute list must be
   * size 1 - Complex Value.
   */
  @Test
  public void testPutComplexAttr() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(complexAttrValue);
    Assert.assertEquals(1, attrList.size());
  }

  /**
   * Testing Personal Attribute List put method. Personal Attribute list must be
   * size 0 - no attribute.
   */
  @Test
  public void testPutNull() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(null);
    Assert.assertEquals(0, attrList.size());
  }

  /**
   * Testing Personal Attribute List put method. Personal Attribute list must be
   * size 2 - IsAgeOver attribute added twice.
   */
  @SuppressWarnings("serial")
  @Test
  public void testPutSameAttrName() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });

    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertEquals(2, attrList.size());
  }

  /**
   * Testing Personal Attribute List put method. Personal Attribute list must be
   * size 2 - IsAgeOver attribute added twice.
   */
  @SuppressWarnings("serial")
  @Test
  public void testPutSameAttrNameEmpty() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("");
        }
      });

    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertEquals(2, attrList.size());
  }

  /**
   * Testing Personal Attribute List get method. Personal Attribute list must be
   * size 1 - Simple attribute.
   */
  @Test
  public void testGetSimpleAttr() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(ATTR_VALUE);
    Assert.assertEquals(ATTR_VALUE, attrList.getByFriendlyName(ATTR_VALUE.getFriendlyName()));
  }

  /**
   * Testing Personal Attribute List add method. Personal Attribute list must be
   * size 1 - Complex attribute.
   */
  @Test
  public void testGetCompleAttr() {
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(complexAttrValue);
    Assert.assertEquals(complexAttrValue.toString(),
      attrList.getByFriendlyName(complexAttrValue.getFriendlyName()).toString());
  }

  /**
   * Testing Personal Attribute List get method. Personal Attribute list must be
   * size 2 - IsAgeOver attribute.
   */
  @SuppressWarnings("serial")
  @Test
  public void testGetIsAgeOverAttr() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertEquals(2, attrList.size());
    Assert.assertEquals(SIMPLE_ATTRLIST,
      attrList.getByFriendlyName(attrValueUnder.getFriendlyName()).toString());
    Assert.assertEquals(SIMPLE_ATTRLIST,
      attrList.getByFriendlyName(attrValueOver.getFriendlyName()).toString());
  }

  /**
   * Testing Personal Attribute List populate method. Personal Attribute list
   * must be size 1 - Simple attribute.
   */
  @Test
  public void testPopulateSimpleAttr() {
    String strAttrList = SIMPLE_ATTRLIST;
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertEquals(1, attrList.size());
  }

  /**
   * Testing Personal Attribute List populate method. Personal Attribute list
   * must be size 1 - Complex attribute.
   */
  @Test
  public void testPopulateComplexAttr() {
    String strAttrList = COMPLEX_ATTRLIST;
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertEquals(1, attrList.size());
  }

  /**
   * Testing Personal Attribute List populate method. Personal Attribute list
   * must be size 1 - Simple and Complex attribute.
   */
  @Test
  public void testPopulateMixAttrs() {
    String strAttrList = STR_MIX_ATTR_LIST;
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertEquals(2, attrList.size());
  }

  /**
   * Testing Personal Attribute List toString method using add.
   */
  @SuppressWarnings("serial")
  @Test
  public void testToStringFromAdd() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    String stringList = PersonalAttributeString.toStringList(attrList);
    Assert.assertTrue(stringList.contains(SIMPLE_ATTRLIST));
    Assert.assertTrue(stringList.contains(SIMPLE_ATTRLIST2));
  }

  /**
   * Testing Personal Attribute List toString method using put.
   *
   */
  @SuppressWarnings("serial")
  @Test
  public void testToStringFromPut() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    String stringList = PersonalAttributeString.toStringList(attrList);
      Assert.assertTrue(stringList.contains(SIMPLE_ATTRLIST));
      Assert.assertTrue(stringList.contains(SIMPLE_ATTRLIST2));
  }

  /**
   * Testing Personal Attribute List toString method using populate.
   */
  @Test
  public void testToStringFromSimplePopulate() {
    final String strAttrList = "isAgeOver:true";
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertEquals("isAgeOver:true:[]:NotAvailable;", attrList.toString());
  }

  /**
   * Testing Personal Attribute List toString method using populate.
   */
  @Test
  public void testToStringFromPopulate() {
    String strAttrList = SIMPLE_ATTRLIST3;
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertEquals(SIMPLE_ATTRLIST3, attrList.toString());
  }

  /**
   * Testing Personal Attribute List populate method, with invalid values.
   */
  @Test
  public void testPopulateWithInvalidValuesFormat() {
    final IPersonalAttributeList attrList;
    try {
      String strAttrList = "name:type:values:status;";
      attrList = PersonalAttributeString.fromStringList( strAttrList);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  /**
   * Testing Personal Attribute List populate method, with invalid format.
   */
  @Test
  public void testPopulateWithInvalidFormat() {
    try {
      String strAttrList = "name:type::status;";
      final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  /**
   * Testing Personal Attribute List clone method using add.
   */
  @SuppressWarnings("serial")
  @Test
  public void testCloneFromAdd() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertNotSame(attrList, PersonalAttributeList.copyOf(attrList));
  }

  /**
   * Testing Personal Attribute List clone method using put.
   */
  @SuppressWarnings("serial")
  @Test
  public void testCloneFromPut() {
    final PersonalAttribute attrValueUnder =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("15");
        }
      });

    final PersonalAttribute attrValueOver =
      new PersonalAttribute(ISAGEOVER_CONS, ISAGEOVER_CONS, true, new ArrayList<String>() {
        {
          add("18");
        }
      });
    final PersonalAttributeList attrList = new PersonalAttributeList(1);
    attrList.add(attrValueUnder);
    attrList.add(attrValueOver);
    Assert.assertNotSame(attrList, PersonalAttributeList.copyOf(attrList));
  }

  /**
   * Testing Personal Attribute List clone method using populate.
   */
  @Test
  public void testCloneFromPopulate() {
    String strAttrList = SIMPLE_ATTRLIST3;
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertNotSame(attrList, PersonalAttributeList.copyOf(attrList));
  }

  /**
   * Testing Personal Attribute List iterator.
   */
  @Test
  public void testIterator() {
    String strAttrList = SIMPLE_ATTRLIST3;
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    final Iterator<PersonalAttribute> itAttr = attrList.iterator();
    while (itAttr.hasNext()) {
      final PersonalAttribute attr = itAttr.next();
      Assert.assertEquals(ISAGEOVER_CONS, attr.getName());
    }
  }

  @Test
  public void testCopyOf() {
    String strAttrList = "http://www.stork.gov.eu/1.0/isAgeOver:true:[15,]:Available;http://www.stork.gov.eu/1.0/age:false:[,]:;";
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    PersonalAttributeList attrList2 = PersonalAttributeList.copyOf(attrList);
    Assert.assertEquals(attrList, attrList2);
  }

  @Test
  @Ignore //TODO STORK related test should be removed or moved elsewhere
  public void testToString() {
    String strAttrList = "http://www.stork.gov.eu/1.0/isAgeOver:true:[15,]:Available;http://www.stork.gov.eu/1.0/age:false:[15,]:Available;";
    final IPersonalAttributeList attrList = PersonalAttributeString.fromStringList( strAttrList);

    Assert.assertEquals("http://www.stork.gov.eu/1.0/isAgeOver:true:[15,]:Available;http://www.stork.gov.eu/1.0/age:false:[15,]:Available;", attrList.toString());
  }
}
