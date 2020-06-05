package eu.eidas.tests;

import eu.eidas.auth.commons.*;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Commons Single bean test case
 */
public class SingleBeanTestCase {

    private static final String COUNTRY_ID = "KL";
    private static final String COUNTRY_NAME = "KLINGON";
    private static final String COUNTRY_CODE_BELGIUM = "BEL";
    private static final String ERROR_MSG_DUMMY = "DUMMY";

    @Test
    public void testCountrySetGet() {
        Country country = new Country(COUNTRY_ID, COUNTRY_NAME);
        Assert.assertSame(country.getCountryId(), COUNTRY_ID);
        Assert.assertSame(country.getCountryName(), COUNTRY_NAME);
        Country country2 = new Country("", "");
        country2.setCountryId(COUNTRY_ID);
        country2.setCountryName(COUNTRY_NAME);
        Assert.assertEquals(country, country2);
    }

    @Test
    public void testCountryCode() {
        Assert.assertTrue(CountryCodes.hasCountryCodeAlpha3(COUNTRY_CODE_BELGIUM));
        Assert.assertFalse(CountryCodes.hasCountryCodeAlpha3(""));
    }

    @Test
    public void testEIDASSubStatusCode() {
        Assert.assertEquals(EIDASSubStatusCode.AUTHN_FAILED_URI.toString(), "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed");
    }

    @Test
    public void testEidasNodeError() {
        Assert.assertEquals(EidasErrorKey.MISSING_SESSION_ID.errorMessage(), EidasErrorKey.MISSING_SESSION_ID.toString() + EidasErrorKey.MESSAGE_CONSTANT);
        Assert.assertEquals(EidasErrorKey.MISSING_SESSION_ID.errorMessage(ERROR_MSG_DUMMY),
                EidasErrorKey.MISSING_SESSION_ID.toString() + EidasErrorKey.DOT_SEPARATOR + ERROR_MSG_DUMMY + EidasErrorKey.MESSAGE_CONSTANT);
        Assert.assertEquals(EidasErrorKey.MISSING_SESSION_ID.errorCode(), EidasErrorKey.MISSING_SESSION_ID.toString() + EidasErrorKey.CODE_CONSTANT);
        Assert.assertEquals(EidasErrorKey.MISSING_SESSION_ID.errorCode(ERROR_MSG_DUMMY),
                EidasErrorKey.MISSING_SESSION_ID.toString() + EidasErrorKey.DOT_SEPARATOR + ERROR_MSG_DUMMY + EidasErrorKey.CODE_CONSTANT);
    }

    @Test
    public void testEIDASNodeValue() {
        Assert.assertEquals(EIDASValues.ATTRIBUTE.index(1), EIDASValues.ATTRIBUTE.toString() + "1.id");
        Assert.assertEquals(EIDASValues.ATTRIBUTE.value(1), EIDASValues.ATTRIBUTE.toString() + "1.value");
        Assert.assertEquals(EIDASValues.ATTRIBUTE.name(1), EIDASValues.ATTRIBUTE.toString() + "1.name");
        Assert.assertEquals(EIDASValues.ATTRIBUTE.url(1), EIDASValues.ATTRIBUTE.toString() + "1.url");
        Assert.assertEquals(EIDASValues.ATTRIBUTE.beforeSkew(1), EIDASValues.ATTRIBUTE.toString() + "1.skew.notbefore");
        Assert.assertEquals(EIDASValues.ATTRIBUTE.afterSkew(1), EIDASValues.ATTRIBUTE.toString() + "1.skew.notonorafter");
    }
}
