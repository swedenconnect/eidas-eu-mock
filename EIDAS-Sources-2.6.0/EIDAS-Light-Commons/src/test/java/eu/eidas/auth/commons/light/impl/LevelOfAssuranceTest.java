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

package eu.eidas.auth.commons.light.impl;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test class for {@link LevelOfAssurance}.
 */
public class LevelOfAssuranceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for
     * {@link LevelOfAssurance#build(String)}
     * when LevelOfAssurance is built with a valid url
     * <p>
     * Must succeed.
     */
    @Test
    public void testInternalValidateURI() {
        final String uriString = "http://eidas.memberstate.ms/loa/A";

        LevelOfAssurance levelOfAssurance = LevelOfAssurance.build(uriString);

        Assert.assertEquals(uriString, levelOfAssurance.getValue());
    }

    /**
     * Test method for
     * {@link LevelOfAssurance#build(String)}
     * when LevelOfAssurance is built with a valid absolute uri
     * <p>
     * Must Succeed.
     */
    @Test
    public void testInternalValidateAbsoluteURI() {
        LevelOfAssurance.build("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        LevelOfAssurance.build("someprotocol://eidas.memberstate.ms");
    }

    /**
     * Test method for
     * {@link LevelOfAssurance#build(String)}
     * when LevelOfAssurance is built with a plain string or relative uri
     * <p>
     * Must fail.
     */
    @Test
    public void testInternalValidateNotAbsoluteURI() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("LevelOfAssurance URI missing protocol prefix");

        final String plainString = "high";

        LevelOfAssurance levelOfAssurance = LevelOfAssurance.build(plainString);

        Assert.assertEquals(plainString, levelOfAssurance.getValue());
    }

    /**
     * Test method for
     * {@link LevelOfAssurance#build(String)}
     * when param string is empty
     * <p>
     * Must fail.
     */
    @Test
    public void testInternalValidateEmptyString() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("LevelOfAssurance cannot be null, empty or blank");

        LevelOfAssurance.build("");
    }
}
