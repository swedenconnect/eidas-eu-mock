/*
 * Copyright (c) 2023 by European Commission
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Parameterized test class for {@link EidasProtocolVersion}
 */
@RunWith(value = Parameterized.class)
public class EidasProtocolVersionInteroperabilityTest {

    @Parameterized.Parameter(value = 0)
    public List<String> destinationVersions;

    @Parameterized.Parameter(value = 1)
    public List<String> configuredVersions;

    @Parameterized.Parameter(value = 2)
    public Set<String> matchingVersions;

    @Parameterized.Parameter(value = 3)
    public boolean interoperable;

    @Parameterized.Parameters(name = "Versions {0} vs {1} interop is {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            //  Destination / Retrieved         Configured                  Matching             Interoperable
                {Arrays.asList("1.2"),          Arrays.asList("1.2"),       Set.of("1.2"),       true},
                {Arrays.asList("1.1"),          Arrays.asList("1.2"),       Set.of(),            false},
                {Arrays.asList("1.1","1.2"),    Arrays.asList("1.2","1.1"), Set.of("1.2","1.1"), true},
                {Arrays.asList("1.3","1.2"),    Arrays.asList("1.2","1.1"), Set.of("1.2"),       true},
                {Arrays.asList("1.3","1.4"),    Arrays.asList("1.2","1.1"), Set.of(),            false},
                {null,                          Arrays.asList("1.2"),       null,                true},
                {null,                          Arrays.asList("1.3","1.4"), null,                true},
                {Arrays.asList(),               Arrays.asList("1.2"),       Set.of(),            true},
                {Arrays.asList(),               Arrays.asList("1.3","1.4"), Set.of(),            true},
                {Arrays.asList(""),             Arrays.asList("1.2"),       Set.of(),            false},
                {Arrays.asList(""),             Arrays.asList("1.3","1.4"), Set.of(),            false},
                {Arrays.asList("","1.3"),       Arrays.asList("1.3","1.4"), Set.of("1.3"),       true},
        });
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when both collections contain multiple protocol version Strings where no String matches between the two
     * must succeed and return an empty set
     */
    @Test
    public void isInteroperableWith() {
        Assert.assertEquals(interoperable, EidasProtocolVersion.isInteroperableWith(configuredVersions, destinationVersions));
        Assert.assertEquals(interoperable, EidasProtocolVersion.isInteroperableWith(destinationVersions, configuredVersions));
    }

    /**
     * Parameterized Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when given 2 collections of strings, should return the intersection.
     */
    @Test
    public void getMatchingProtocolVersionsByString() {
        if((configuredVersions == null || destinationVersions == null) && matchingVersions == null) {
            return; // this method does not handle null, so don't test for null
        }
        assert configuredVersions != null;
        assert destinationVersions != null;
        Assert.assertEquals(matchingVersions, EidasProtocolVersion.getMatchingProtocolVersionsByString(configuredVersions, destinationVersions));
        Assert.assertEquals(matchingVersions, EidasProtocolVersion.getMatchingProtocolVersionsByString(destinationVersions, configuredVersions));
    }
}