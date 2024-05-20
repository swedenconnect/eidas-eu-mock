/*
 * Copyright (c) 2024 by European Commission
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Test class for {@link EidasProtocolVersion}
 */
public class EidasProtocolVersionTest {

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(String)}
     * when the protocol version exists
     * This test tests protocol version 1.2
     * <p>
     * Must succeed
     */
    @Test
    public void testFromStringKnownProtocolVersion1_2() {
        String protocolVersion = "1.2";

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.fromString(protocolVersion);

        Assert.assertEquals(EidasProtocolVersion.PROTOCOL_VERSION_1_2, eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(String)}
     * when the protocol version exists
     * This test tests protocol version 1.3
     * <p>
     * Must succeed
     */
    @Test
    public void testFromStringKnownProtocolVersion1_3() {
        String protocolVersion = "1.3";

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.fromString(protocolVersion);

        Assert.assertEquals(EidasProtocolVersion.PROTOCOL_VERSION_1_3, eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(String)}
     * when the protocol version doesn't exist
     * <p>
     * Must succeed - return null
     */
    @Test
    public void testFromStringUnknownProtocolVersion() {
        String protocolVersion = "0.9";

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.fromString(protocolVersion);

        Assert.assertNull(eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(List)}
     * when a list of protocol versions (all existing)
     * <p>
     * Must succeed - return a list with the existing protocol versions.
     */
    @Test
    public void testFromStringKnownProtocolVersions() {
        List<String> protocolVersionsList = Arrays.asList("1.2");

        List<EidasProtocolVersion> eidasProtocolVersionList = EidasProtocolVersion.fromString(protocolVersionsList);

        Assert.assertNotNull(eidasProtocolVersionList);
        Assert.assertFalse(eidasProtocolVersionList.isEmpty());
        Assert.assertEquals(1, eidasProtocolVersionList.size());
        Assert.assertTrue(eidasProtocolVersionList.contains(EidasProtocolVersion.PROTOCOL_VERSION_1_2));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(List)}
     * when the list of protocol versions contains existing and not existing protocol versions.
     * <p>
     * Must succeed - return a list only with the existing protocol versions.
     */
    @Test
    public void testFromStringKnownAndUnknownProtocolVersions() {
        List<String> protocolVersionsList = Arrays.asList("1.2", "0.9");

        List<EidasProtocolVersion> eidasProtocolVersionList = EidasProtocolVersion.fromString(protocolVersionsList);

        Assert.assertNotNull(eidasProtocolVersionList);
        Assert.assertFalse(eidasProtocolVersionList.isEmpty());
        Assert.assertEquals(1, eidasProtocolVersionList.size());
        Assert.assertEquals(EidasProtocolVersion.PROTOCOL_VERSION_1_2, eidasProtocolVersionList.get(0));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(List)}
     * when the given protocol versions list is null
     * <p>
     * Must succeed - return an empty list
     */
    @Test
    public void testFromStringNullProtocolVersionsList() {
        List<String> protocolVersionsList = null;

        List<EidasProtocolVersion> eidasProtocolVersionList = EidasProtocolVersion.fromString(protocolVersionsList);

        Assert.assertNotNull(eidasProtocolVersionList);
        Assert.assertTrue(eidasProtocolVersionList.isEmpty());
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#fromString(List)}
     * when the given protocol versions list is empty
     * <p>
     * Must succeed - return an empty list
     */
    @Test
    public void testFromStringEmptyProtocolVersionsList() {
        List<String> protocolVersionsList = new ArrayList<>();

        List<EidasProtocolVersion> eidasProtocolVersionList = EidasProtocolVersion.fromString(protocolVersionsList);

        Assert.assertNotNull(eidasProtocolVersionList);
        Assert.assertTrue(eidasProtocolVersionList.isEmpty());
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getHighestProtocolVersion(List)}
     * when the list contains multiple eIDAS Protocol versions
     * <p>
     * Must succeed - return the highest protocol version within the list
     */
    @Test
    public void testGetHighestProtocolVersion() {
        List<EidasProtocolVersion> protocolVersionsList = Arrays.asList(
                EidasProtocolVersion.PROTOCOL_VERSION_1_3,
                EidasProtocolVersion.PROTOCOL_VERSION_1_2);

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(protocolVersionsList);

        Assert.assertEquals(EidasProtocolVersion.PROTOCOL_VERSION_1_3, eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getHighestProtocolVersion(List)}
     * when the list contains multiple eIDAS Protocol versions
     * <p>
     * Must succeed - return the highest protocol version within the list
     */
    @Test
    public void testGetHighestProtocolVersionWithUnorderdList() {
        List<EidasProtocolVersion> protocolVersionsList = Arrays.asList(
                EidasProtocolVersion.PROTOCOL_VERSION_1_2,
                EidasProtocolVersion.PROTOCOL_VERSION_1_3);

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(protocolVersionsList);

        Assert.assertEquals(EidasProtocolVersion.PROTOCOL_VERSION_1_3, eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getHighestProtocolVersion(List)}
     * when the list is empty
     * <p>
     * Must succeed - return null
     */
    @Test
    public void testGetHighestProtocolVersionWithEmtpyList() {
        List<EidasProtocolVersion> protocolVersionsList = new ArrayList<>();

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(protocolVersionsList);

        Assert.assertNull(eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getHighestProtocolVersion(List)}
     * when the list is null
     * <p>
     * Must succeed - return null
     */
    @Test
    public void testGetHighestProtocolVersionWithNullList() {
        List<EidasProtocolVersion> protocolVersionsList = null;

        EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(protocolVersionsList);

        Assert.assertNull(eidasProtocolVersion);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isHigherProtocolVersion(EidasProtocolVersion)}
     * when the current eIDAS Protocol version is higher version
     * <p>
     * Must succeed - return true
     */
    @Test
    public void testIsHigherProtocolVersion() {
        boolean isHigher = EidasProtocolVersion.PROTOCOL_VERSION_1_3
                .isHigherProtocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_2);

        Assert.assertTrue(isHigher);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isHigherProtocolVersion(EidasProtocolVersion)}
     * when the current eIDAS Protocol version is same version
     * <p>
     * Must succeed - return true
     */
    @Test
    public void testIsHigherProtocolVersionWithSameVersion() {
        boolean isHigher = EidasProtocolVersion.PROTOCOL_VERSION_1_2
                .isHigherProtocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_2);

        Assert.assertTrue(isHigher);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isHigherProtocolVersion(EidasProtocolVersion)}
     * when the current eIDAS Protocol version is lower version
     * <p>
     * Must succeed - return false
     */
    @Test
    public void testIsHigherProtocolVersionWithLowerVersion() {
        boolean isHigher = EidasProtocolVersion.PROTOCOL_VERSION_1_2
                .isHigherProtocolVersion(EidasProtocolVersion.PROTOCOL_VERSION_1_3);

        Assert.assertFalse(isHigher);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isHigherProtocolVersion(EidasProtocolVersion)}
     * when the given eIDAS Protocol version is null
     * <p>
     * Must succeed - return true
     */
    @Test
    public void testIsHigherProtocolVersionWithNullVersion() {
        boolean isHigher = EidasProtocolVersion.PROTOCOL_VERSION_1_2.isHigherProtocolVersion(null);

        Assert.assertTrue(isHigher);
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when both collections contain a single matching protocol version String
     * must succeed and return the matching string
     */
    @Test
    public void testGetMatchingProtocolVersionsFromStringSingleMatching(){
        List<String> configuredVersions = Arrays.asList("1.2");
        List<String> destinationVersions = Arrays.asList("1.2");
        Set<String> matchingVersions = EidasProtocolVersion.getMatchingProtocolVersionsByString(configuredVersions,destinationVersions);
        Assert.assertTrue(matchingVersions.contains("1.2"));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when both collections contain a single non-matching protocol version String
     * must succeed and return empty Set
     */
    @Test
    public void testGetMatchingProtocolVersionsFromStringSingleNonMatching(){
        List<String> configuredVersions = Arrays.asList("1.2");
        List<String> destinationVersions = Arrays.asList("1.1");
        Set<String> matchingVersions = EidasProtocolVersion.getMatchingProtocolVersionsByString(configuredVersions,destinationVersions);
        Assert.assertTrue(matchingVersions.isEmpty());
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when both collections contain multiple matching protocol version Strings
     * must succeed and return all matching protocol versions
     */
    @Test
    public void testGetMatchingProtocolVersionsFromStringMultiAllMatching(){
        List<String> configuredVersions = Arrays.asList("1.2","1.1");
        List<String> destinationVersions = Arrays.asList("1.1","1.2");
        Set<String> matchingVersions = EidasProtocolVersion.getMatchingProtocolVersionsByString(configuredVersions,destinationVersions);
        Assert.assertTrue(matchingVersions.containsAll(Set.of("1.2","1.1")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when both collections contain multiple protocol version Strings where one String matches between the two
     * must succeed and return the single matching protocol version
     */
    @Test
    public void testGetMatchingProtocolVersionsFromStringMultiOneMatching(){
        List<String> configuredVersions = Arrays.asList("1.2","1.1");
        List<String> destinationVersions = Arrays.asList("1.3","1.2");
        Set<String> matchingVersions = EidasProtocolVersion.getMatchingProtocolVersionsByString(configuredVersions,destinationVersions);
        Assert.assertTrue(matchingVersions.contains("1.2"));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#getMatchingProtocolVersionsByString(Collection, Collection)}
     * when both collections contain multiple protocol version Strings where no String matches between the two
     * must succeed and return an empty set
     */
    @Test
    public void testGetMatchingProtocolVersionsFromStringMultiNoneMatching(){
        List<String> configuredVersions = Arrays.asList("1.2","1.1");
        List<String> destinationVersions = Arrays.asList("1.3","1.4");
        Set<String> matchingVersions = EidasProtocolVersion.getMatchingProtocolVersionsByString(configuredVersions,destinationVersions);
        Assert.assertTrue(matchingVersions.isEmpty());
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when both collections contain the same protocol version
     * must return true
     */
    @Test
    public void isInteroperableSingleValue() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(List.of("1.2"), List.of("1.2")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when both collections contain a different protocol version
     * must return false
     */
    @Test
    public void isInteroperableSingleValueNoMatch() {
        Assert.assertFalse(EidasProtocolVersion.isInteroperableWith(List.of("1.1"), List.of("1.2")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when both collections contain multiple idempotent protocol versions
     * must return true
     */
    @Test
    public void isInteroperableMultiValue() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(Arrays.asList("1.1","1.2"), Arrays.asList("1.2","1.1")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when both collections contain one mutual protocol version
     * must return true
     */
    @Test
    public void isInteroperableMultiValueOneMatch() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(Arrays.asList("1.2","1.1"), Arrays.asList("1.3","1.2")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when both collections contain multiple values without a matching protocol version
     * must return false
     */
    @Test
    public void isInteroperableMultiValueNoMatch() {
        Assert.assertFalse(EidasProtocolVersion.isInteroperableWith(Arrays.asList("1.2","1.1"), Arrays.asList("1.3","1.4")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection is null and the other contains one protocol version
     * must return true
     */
    @Test
    public void isInteroperableWithNull() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(null, Arrays.asList("1.3")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection is null and the other contains multiple protocol versions
     * must return true
     */
    @Test
    public void isInteroperableWithNullMulti() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(null, Arrays.asList("1.3","1.4")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection is empty and the other contains one protocol version
     * must return true
     */
    @Test
    public void isInteroperableWithEmptyArray() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(List.of(), List.of("1.2")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection is empty and the other contains multiple protocol versions
     * must return true
     */
    @Test
    public void isInteroperableWithEmptyArrayMultiple() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(List.of(), Arrays.asList("1.3","1.4")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection contains an empty string and the other contains one protocol version
     * must return false
     */
    @Test
    public void isInteroperableWithEmptyString() {
        Assert.assertFalse(EidasProtocolVersion.isInteroperableWith(List.of(""), List.of("1.4")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection contains an empty string and the other contains multiple protocol versions
     * must return false
     */
    @Test
    public void isInteroperableWithEmptyStringMultiple() {
        Assert.assertFalse(EidasProtocolVersion.isInteroperableWith(List.of(""), Arrays.asList("1.3","1.4")));
    }

    /**
     * Test method for
     * {@link EidasProtocolVersion#isInteroperableWith(Collection, Collection)}
     * when one collection contains an empty and valid string and the other contains multiple protocol versions
     * with one common protocol version
     * must return true
     */
    @Test
    public void isInteroperableWithEmptyStringAndVersionMultiple() {
        Assert.assertTrue(EidasProtocolVersion.isInteroperableWith(Arrays.asList("","1.3"), Arrays.asList("1.3","1.4")));
    }
}
