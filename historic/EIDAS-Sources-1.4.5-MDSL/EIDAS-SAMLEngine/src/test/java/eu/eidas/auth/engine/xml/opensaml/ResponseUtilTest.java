package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;

/**
 * Test Class for the {@link ResponseUtil} class.
 */
public class ResponseUtilTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    /**
     * Need to load the EIDASSAMLEngine.
     */
    @BeforeClass
    public static void setUpClass() {
        ProtocolEngineFactory.getDefaultProtocolEngine("CONF1");
    }

    /**
     * Test method for
     * {@link ResponseUtil#extractVerifiedAssertion(Response, boolean, String, long, long, DateTime, String)}
     * when the response is a successful one with one assertion.
     * <p/>
     * Must succeed.
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void extractVerifiedAssertion() throws EIDASSAMLEngineException {

        Response samlResponse = ResponseTestHelper.generateTestResponse(1, 1, 1, StatusCode.SUCCESS_URI);
        String userIpAddress = "demoIpAddress";
        boolean verifyBearerIpAddress = false;
        long beforeSkewTimeInMillis = 1000;
        long afterSkewTimeInMillis = 1000;
        DateTime now = DateTime.now();
        String audienceRestriction = null;

        Assertion assertion = ResponseUtil.extractVerifiedAssertion(
                samlResponse,
                verifyBearerIpAddress,
                userIpAddress,
                beforeSkewTimeInMillis,
                afterSkewTimeInMillis,
                now,
                audienceRestriction);

        Assert.assertNotNull(assertion);
    }


    /**
     * Test method for
     * {@link ResponseUtil#extractVerifiedAssertion(Response, boolean, String, long, long, DateTime, String)}
     * when the response is not a successful and with zero assertions.
     * <p/>
     * Must succeed.
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void extractVerifiedAssertionNoAssertionStatusCodeNotSuccess() throws EIDASSAMLEngineException {

        Response samlResponse = ResponseTestHelper.generateTestResponse(0, 1, 1, StatusCode.REQUESTER_URI);
        String userIpAddress = "demoIpAddress";
        boolean verifyBearerIpAddress = false;
        long beforeSkewTimeInMillis = 1000;
        long afterSkewTimeInMillis = 1000;
        DateTime now = DateTime.now();
        String audienceRestriction = null;

        Assertion assertion = ResponseUtil.extractVerifiedAssertion(
                samlResponse,
                verifyBearerIpAddress,
                userIpAddress,
                beforeSkewTimeInMillis,
                afterSkewTimeInMillis,
                now,
                audienceRestriction);

        Assert.assertNull(assertion);
    }


    /**
     * Test method for
     * {@link ResponseUtil#extractVerifiedAssertion(Response, boolean, String, long, long, DateTime, String)}
     * when the response is not a successful and with zero assertions.
     * <p/>
     * Must fail and throw {@link EIDASSAMLEngineException}.
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void extractVerifiedAssertion2AssertionsStatusCodeSucess() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage("Error (no. message.validation.error.code) processing request : message.validation.error.code - Assertion is other that null for failure SAML Responses or other that 1 for sucessful SAML Responses.");

        Response samlResponse = ResponseTestHelper.generateTestResponse(2, 1, 1, StatusCode.SUCCESS_URI);
        String userIpAddress = "demoIpAddress";
        boolean verifyBearerIpAddress = false;
        long beforeSkewTimeInMillis = 1000;
        long afterSkewTimeInMillis = 1000;
        DateTime now = DateTime.now();
        String audienceRestriction = null;

        ResponseUtil.extractVerifiedAssertion(
                samlResponse,
                verifyBearerIpAddress,
                userIpAddress,
                beforeSkewTimeInMillis,
                afterSkewTimeInMillis,
                now,
                audienceRestriction);
    }

}