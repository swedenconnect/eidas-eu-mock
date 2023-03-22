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

package eu.eidas.auth.commons.validation;

import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for {@link eu.eidas.auth.commons.validation.GenderProtocolVersionValidator}
 */
 public class GenderProtocolVersionValidatorTest {

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return false
     * when eidas protocol version is 1.1 and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testInvalidGenderUnspecifiedForProtocolVersion1_1() {
        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_1)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.UNSPECIFIED);

        Assert.assertFalse(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return true
     * when eidas protocol version is 1.2 and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderUnspecifiedForProtocolVersion1_2() {
        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_2)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.UNSPECIFIED);

        Assert.assertTrue(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return false
     * when eidas protocol version is 1.2 and gender is Not Specified
     * <p>
     * Must succeed.
     */
    @Test
    public void testInvalidGenderNotSpecifiedForProtocolVersion1_2() {
        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_2)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.NOT_SPECIFIED);

        Assert.assertFalse(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return true
     * when eidas protocol version is 1.1 and gender is Not Specified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderNotSpecifiedForProtocolVersion1_1() {
        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_1)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.NOT_SPECIFIED);

        Assert.assertTrue(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return true
     * when eidas protocol version is 1.2;1.1 and gender is Not Specified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderNotSpecifiedForProtocolVersion1_1And1_2() {
        List<EidasProtocolVersion> protocolVersionList = new ArrayList<>();
        protocolVersionList.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2);
        protocolVersionList.add(EidasProtocolVersion.PROTOCOL_VERSION_1_1);

        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersions(protocolVersionList)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.NOT_SPECIFIED);

        Assert.assertTrue(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return true
     * when eidas protocol version is 1.2;1.1 and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderUnspecifiedForProtocolVersion1_1And1_2() {
        List<EidasProtocolVersion> protocolVersionList = new ArrayList<>();
        protocolVersionList.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2);
        protocolVersionList.add(EidasProtocolVersion.PROTOCOL_VERSION_1_1);

        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersions(protocolVersionList)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.UNSPECIFIED);

        Assert.assertTrue(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return true
     * when eidas protocol version is empty and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderUnspecifiedForEmptyProtocolVersion() {
        List<EidasProtocolVersion> protocolVersionList = new ArrayList<>();

        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersions(protocolVersionList)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.UNSPECIFIED);

        Assert.assertTrue(actualIsValid);
    }

    /**
     * Test method for
     * {@link GenderProtocolVersionValidator#isValid(Object)} must return false
     * when eidas protocol version is empty and gender is Unspecified
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidGenderNotSpecifiedForEmptyProtocolVersion() {
        List<EidasProtocolVersion> protocolVersionList = new ArrayList<>();

        ValueValidator<Gender> genderValidator = GenderProtocolVersionValidator
                .Builder()
                .protocolVersions(protocolVersionList)
                .build();

        boolean actualIsValid = genderValidator.isValid(Gender.NOT_SPECIFIED);

        Assert.assertFalse(actualIsValid);
    }
}
