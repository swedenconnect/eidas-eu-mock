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

package eu.eidas.node.auth;

import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.AbstractEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.node.utils.EidasAttributesUtil;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for the {@link EidasNodeValidationUtil}
 */
public class EidasNodeValidationUtilTestCase {

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Testing cases with null parameters.
     *
     * Must succeed.
     */
    @Test
    public void testIsRequestLoAValid() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());
        IEidasAuthenticationRequest eidasAuthenticationRequest = mockIEidasAuthenticationRequestNullLoa();

        checkInvalidLoARequest("Null check for values", null, null);
        checkInvalidLoARequest("Null check for request", null, allowedLoAs);
        checkInvalidLoARequest("Null check for Level", eidasAuthenticationRequest, null);

        checkInvalidLoARequest("Null check for request.levelOfAssurance", eidasAuthenticationRequest, allowedLoAs);
    }



    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where requested notified LoA is lower than allowed LoA.
     *
     * Must return true.
     */
    @Test
    public void testIsRequestNotifiedLoAValidLowerThanAllowed() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        checkValidLoARequest("Normal case LOW<=High (minimum)", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where requested notified LoA is equal to allowed LoA.
     *
     * Must return true.
     */
    @Test
    public void testIsRequestNotifiedLoAValidEqualToAllowed() {
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
        checkValidLoARequest("Normal case HIGH<=High (minimum)", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where requested notified LoA is higher than allowed LoA.
     *
     * Must return false.
     */
    @Test
    public void testIsRequestNotifiedLoAValidHigherThanAllowed(){
        List<String> allowedLoAs = Arrays.asList(NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue());

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
        checkInvalidLoARequest("Error case HIGH<=substantial (minimum)", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where the requested non notified LoA does not match the notified LoA in allowed LoAs.
     *
     * Must return false .
     */
    @Test
    public void testIsRequestNonNotifiedLoAMismatchNotifiedInvalid() {
        List<String> allowedLoAs = Arrays.asList("http://eidas.europa.eu/NonNotified/LoA/Test");

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        checkInvalidLoARequest("Should not match: notified is different from non notified LoA",
                eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where the requested non notified LoA does not match the non notified LoA in allowed LoAs.
     *
     * Must return false.
     */
    @Test
    public void testIsRequestNonNotifiedLoAMismatchNonNotifiedInValid() {
        List<String> allowedLoAs = Arrays.asList("http://eidas.europa.eu/NonNotified/LoA/Test");

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance("http://eidas.europa.eu/NonNotified/LoA/notAMatch")
                .build();
        checkInvalidLoARequest("Should not match: LoA are not the same", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isRequestLoAValid(IEidasAuthenticationRequest, List)} method
     * Where the requested non notified LoA matches the non notified LoA in allowed LoAs.
     *
     * Must return true.
     */
    @Test
    public void testIsRequestNonNotifiedLoAMatchValid(){
        List<String> allowedLoAs = Arrays.asList("http://eidas.europa.eu/NonNotified/LoA/Test");

        IEidasAuthenticationRequest
        eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance("http://eidas.europa.eu/NonNotified/LoA/Test")
                .build();
        checkValidLoARequest("Should be valid for matching LoAs", eidasAuthenticationRequest, allowedLoAs);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isLoAValid(LevelOfAssuranceComparison, String, String)} method
     * Where the notified LoA in response is lower than the requested notified LoA.
     *
     * Must return false.
     */
    @Test
    public void testIsResponseNotifiedLoAValidLowerRequest() {
        String allowedLoA = NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue();

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        checkInvalidNotifiedLoaResponse(allowedLoA, eidasAuthenticationRequest.getLevelOfAssurance());
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isLoAValid(LevelOfAssuranceComparison, String, String)} method
     * Where the notified LoA in response equals the requested notified LoA.
     *
     * Must return true.
     */
    @Test
    public void testIsResponseNotifiedLoAValidEqualsRequest() {
        String allowedLoA = NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue();

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue())
                .build();
        checkValidNotifiedLoaResponse(allowedLoA, eidasAuthenticationRequest.getLevelOfAssurance());
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isLoAValid(LevelOfAssuranceComparison, String, String)} method
     * Where the notified LoA in response is higher than the requested notified LoA.
     *
     * Must return true.
     */
    @Test
    public void testIsResponseNotifiedLoAValidHigherRequest(){
        String allowedLoA = NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue();

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
        checkValidNotifiedLoaResponse(allowedLoA, eidasAuthenticationRequest.getLevelOfAssurance());
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isNonNotifiedLoAValid(LevelOfAssuranceComparison, List, String)} method
     * Where the non notified LoA in response does not match the requested non notified LoA.
     *
     * Must return false.
     */
    @Test
    public void testIsResponseNonNotifiedLoAValidMismatch() {
        List<ILevelOfAssurance> loaList = new ArrayList<>();
        loaList.add(LevelOfAssurance.build("loa:nonNotified"));

        IEidasAuthenticationRequest eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance("loa:wrongNonNotified")
                .build();
        checkInvalidNonNotifiedLoaResponse(loaList, eidasAuthenticationRequest.getNonNotifiedLevelsOfAssurance().get(0));
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isNonNotifiedLoAValid(LevelOfAssuranceComparison, List, String)} method
     * Where the non notified LoA in response does not match the requested non notified LoA.
     *
     * Must return false.
     */
    @Test
    public void testIsResponseNonNotifiedLoAValidMatch() {
        List<ILevelOfAssurance> loaList = new ArrayList<>();
        loaList.add(LevelOfAssurance.build("loa:nonNotified"));

        IEidasAuthenticationRequest
                eidasAuthenticationRequest = getEidasRequestBuilder()
                .levelOfAssurance("loa:nonNotified")
                .build();
        checkValidNonNotifiedLoaResponse(loaList, eidasAuthenticationRequest.getNonNotifiedLevelsOfAssurance().get(0));
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains a higher notified loa and returns true
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNotified() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.LOW.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue());
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertTrue(isBetterOrEqual);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains 1 overlapping loa and returns true
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNonNotified() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.LOW.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList("loa:nonNotifiedLoA", "loa:nonNotifiedLoA2");
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertTrue(isBetterOrEqual);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains a lower notified loa and returns false
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNotifiedNotEquals() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList(NotifiedLevelOfAssurance.LOW.stringValue());
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertFalse(isBetterOrEqual);
    }

    /**
     * Test for the {@link EidasNodeValidationUtil#isEqualOrBetterLoAs(List, List)}
     * Where the list of better loas contains no overlapping loa and returns false
     * <p>
     * Must succeed.
     */
    @Test
    public void isEqualOrBetterLoAsNonNotifiedNotEqual() {
        final List<String> loas = Arrays.asList(NotifiedLevelOfAssurance.HIGH.stringValue(), "loa:nonNotifiedLoA");
        final List<String> betterloas = Arrays.asList("loa:Someother");
        boolean isBetterOrEqual = EidasNodeValidationUtil.isEqualOrBetterLoAs(loas, betterloas);
        Assert.assertFalse(isBetterOrEqual);
    }

    private void checkInvalidLoARequest(String message, IEidasAuthenticationRequest request, List<String> allowedLoAs) {
        Assert.assertFalse(message, EidasNodeValidationUtil.isRequestLoAValid(request, allowedLoAs));
    }

    private void checkValidLoARequest(String message, IEidasAuthenticationRequest request, List<String> allowedLoAs) {
        Assert.assertTrue(message, EidasNodeValidationUtil.isRequestLoAValid(request, allowedLoAs));
    }

    private void checkInvalidNotifiedLoaResponse(String requestLoa, String stringMaxLoa){
        Assert.assertFalse(EidasNodeValidationUtil.isLoAValid(LevelOfAssuranceComparison.MINIMUM, requestLoa, stringMaxLoa));
    }

    private void checkValidNotifiedLoaResponse(String requestLoa, String stringMaxLoa){
        Assert.assertTrue(EidasNodeValidationUtil.isLoAValid(LevelOfAssuranceComparison.MINIMUM, requestLoa, stringMaxLoa));
    }

    private void checkInvalidNonNotifiedLoaResponse(List<ILevelOfAssurance> requestLoAs, String stringMaxLoa){
        Assert.assertFalse(EidasNodeValidationUtil.isNonNotifiedLoAValid(LevelOfAssuranceComparison.EXACT, requestLoAs, stringMaxLoa));
    }

    private void checkValidNonNotifiedLoaResponse(List<ILevelOfAssurance> requestLoAs, String stringMaxLoa){
        Assert.assertTrue(EidasNodeValidationUtil.isNonNotifiedLoAValid(LevelOfAssuranceComparison.EXACT, requestLoAs, stringMaxLoa));
    }

    private EidasAuthenticationRequest.Builder getEidasRequestBuilder() {
        final EidasAuthenticationRequest.Builder requestBuilder = EidasAuthenticationRequest.builder()
                .id("testId")
                .issuer("testIssuer")
                .citizenCountryCode("testCountryCode")
                .destination("testDestination");
        return requestBuilder;
    }

//    TODO check if testGetUserFriendlyLoa method
    @Test
    public void testGetUserFriendlyLoa(){
        Assert.assertEquals(EidasAttributesUtil.getUserFriendlyLoa("http://eidas.europa.eu/LoA/"), "");
        Assert.assertEquals(EidasAttributesUtil.getUserFriendlyLoa("low"), "low");
        Assert.assertEquals(EidasAttributesUtil.getUserFriendlyLoa("http://eidas.europa.eu/LoA/low"), "low");
    }

    private IEidasAuthenticationRequest mockIEidasAuthenticationRequestNullLoa() {
        IEidasAuthenticationRequest eidasAuthenticationRequest = Mockito.mock(AbstractEidasAuthenticationRequest.class);

        Mockito.when(eidasAuthenticationRequest.getLevelsOfAssurance()).thenReturn(null);
        return eidasAuthenticationRequest;
    }
}
