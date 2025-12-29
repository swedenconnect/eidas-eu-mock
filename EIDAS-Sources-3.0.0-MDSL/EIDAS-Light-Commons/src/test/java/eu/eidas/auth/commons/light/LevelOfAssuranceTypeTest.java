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

package eu.eidas.auth.commons.light;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LevelOfAssuranceTypeTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link LevelOfAssuranceType#stringValue()}
     * {@link LevelOfAssuranceType#NOTIFIED} maps to notified
     * <p>
     * Must succeed.
     */
    @Test
    public void stringValueNotified() {
        Assert.assertEquals("notified", LevelOfAssuranceType.NOTIFIED.stringValue());
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#stringValue()}
     * {@link LevelOfAssuranceType#NON_NOTIFIED} maps to notified
     * <p>
     * Must succeed.
     */
    @Test
    public void stringValueNonNotified() {
        Assert.assertEquals("nonNotified", LevelOfAssuranceType.NON_NOTIFIED.stringValue());
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromString(String)}
     * notified maps to {@link LevelOfAssuranceType#NOTIFIED}
     * <p>
     * Must succeed.
     */
    @Test
    public void fromStringNotified() {
        final LevelOfAssuranceType levelOfAssuranceType = LevelOfAssuranceType.fromString("notified");
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceType);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromString(String)}
     * nonNotified maps to {@link LevelOfAssuranceType#NON_NOTIFIED}
     * <p>
     * Must succeed.
     */
    @Test
    public void fromStringNonNotified() {
        final LevelOfAssuranceType levelOfAssuranceType = LevelOfAssuranceType.fromString("nonNotified");
        Assert.assertEquals(LevelOfAssuranceType.NON_NOTIFIED, levelOfAssuranceType);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromString(String)}
     * unknown value maps to null
     * <p>
     * Must succeed.
     */
    @Test
    public void fromStringElse() {
        final LevelOfAssuranceType levelOfAssuranceType = LevelOfAssuranceType.fromString("Else");
        Assert.assertNull(levelOfAssuranceType);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a notified level of assurance exact matches notified levelOfAssurance
     * <p>
     * Must succeed.
     */
    @Test
    public void fromLoAValueNotified() {
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue(ILevelOfAssurance.EIDAS_LOA_HIGH);
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceTypeHigh);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a non notified level of assurance follows all rules
     * <p>
     * Must succeed.
     */
    @Test
    public void fromLoAValueNonNotified() {
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue("http://eidas.memberstate.ms/loa/A");
        Assert.assertEquals(LevelOfAssuranceType.NON_NOTIFIED, levelOfAssuranceTypeHigh);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a non notified level of assurance makes use of the EIDAS LoA Prefix
     * <p>
     * Must fail.
     */
    @Test
    public void fromLoAValueNonNotifiedPrefix() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values.");
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue("http://eidas.europa.eu/LoA/NonNotified");
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceTypeHigh);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a non notified level of assurance makes use of the EIDAS LoA Prefix in lowercase
     * <p>
     * Must fail.
     */
    @Test
    public void fromLoAValueNonNotifiedLowercasePrefix() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values.");
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue("http://eidas.europa.eu/loa/NonNotified");
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceTypeHigh);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a notified level of assurance does not respect URI path case sensitivity
     * <p>
     * Must fail.
     */
    @Test
    public void fromLoAValuePrefixUppercasePath() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values.");
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue("http://eidas.europa.eu/LoA/HIGH");
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceTypeHigh);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a notified level of assurance does not respect URI path case sensitivity (on prefix)
     * <p>
     * Must fail.
     */
    @Test
    public void fromLoAValueLowercasePrefix() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values.");
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue("http://eidas.europa.eu/loa/high");
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceTypeHigh);
    }

    /**
     * Test method for
     * {@link LevelOfAssuranceType#fromLoAValue(String)}
     * when a notified level of assurance respects URI host insensitivity but implementation requires exact matching
     * Must fail.
     */
    @Test
    public void fromLoAValueNotifiedHigh() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values.");
        final LevelOfAssuranceType levelOfAssuranceTypeHigh = LevelOfAssuranceType.fromLoAValue("http://EIDAS.europa.eu/LoA/high");
        Assert.assertEquals(LevelOfAssuranceType.NOTIFIED, levelOfAssuranceTypeHigh);
    }
}