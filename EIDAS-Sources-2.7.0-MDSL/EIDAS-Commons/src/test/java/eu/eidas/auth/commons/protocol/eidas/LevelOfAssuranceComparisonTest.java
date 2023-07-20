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

package eu.eidas.auth.commons.protocol.eidas;

import org.junit.Assert;
import org.junit.Test;

public class LevelOfAssuranceComparisonTest {

    @Test
    public void fromString() {
        final String exact = LevelOfAssuranceComparison.EXACT.stringValue();
        final String minimal = LevelOfAssuranceComparison.MINIMUM.stringValue();

        Assert.assertEquals(exact, LevelOfAssuranceComparison.fromString(exact).stringValue());
        Assert.assertEquals(minimal, LevelOfAssuranceComparison.fromString(minimal).stringValue());
    }

    @Test(expected = NullPointerException.class)
    public void fromStringNonExistingLoaComparison() {
        LevelOfAssuranceComparison gibberish = LevelOfAssuranceComparison.fromString("gibberish");
        Assert.assertNull(gibberish);
        gibberish.stringValue();
    }
}