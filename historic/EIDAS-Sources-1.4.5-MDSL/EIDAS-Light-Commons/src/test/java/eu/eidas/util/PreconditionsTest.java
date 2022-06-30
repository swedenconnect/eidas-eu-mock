/*
 * ECAS Software
 * Copyright (c) 2013 European Commission
 * Licensed under the EUPL
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 *
 * This product includes the CAS software developed by Yale University,
 * Copyright (c) 2000-2004 Yale University. All rights reserved.
 * THE CAS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 * DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package eu.eidas.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * PreconditionsTest
 *
 * @since 2013-10-07
 */
@SuppressWarnings("ConstantConditions")
public final class PreconditionsTest {

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCheckNotBlankWithBlankUnicodeArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("white space according to Java cannot be null, empty or blank");
        Preconditions.checkNotBlank("\u0009\u000B\u000C\u001C\u001D\u001E\u001F", "white space according to Java");
    }

    @Test
    public void testCheckNotBlankWithMixedBlankArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null, empty or blank");
        Preconditions.checkNotBlank(" \t\r\n ", "reference");
    }

    @Test
    public void testCheckNotBlankWithMultiSpacesArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null, empty or blank");
        Preconditions.checkNotBlank("   ", "reference");
    }

    @Test
    public void testCheckNotBlankWithNonNullArgument() throws Exception {
        Preconditions.checkNotBlank("not null", "reference");
    }

    @Test
    public void testCheckNotBlankWithNullArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null, empty or blank");
        Preconditions.checkNotBlank((String) null, "reference");
    }

    @Test
    public void testCheckNotBlankWithSingleSpaceArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null, empty or blank");
        Preconditions.checkNotBlank(" ", "reference");
    }

    @Test
    public void testCheckNotBlankWithTabArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null, empty or blank");
        Preconditions.checkNotBlank("\t", "reference");
    }

    @Test
    public void testCheckNotBlankWithBlankCharArray() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be blank");
        Preconditions.checkNotBlank("  \t  \n ".toCharArray(), "reference");
    }

    @Test
    public void testCheckNotBlankWithEmptyCharArray() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be empty");
        Preconditions.checkNotBlank(new char[0], "reference");
    }

    @Test
    public void testCheckNotBlankWithZeroedCharArray() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be blank");
        Preconditions.checkNotBlank(new char[5], "reference");
    }

    @Test
    public void testCheckNotNullWithNonNullArgument() throws Exception {
        Preconditions.checkNotNull(new Object(), "object");
    }

    @Test
    public void testCheckNotNullWithNullArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null");
        Preconditions.checkNotNull(null, "reference");
    }

    @Test
    public void testCheckSingleValuedListWithNullArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null");
        Preconditions.checkSingleValuedList(null, "reference");
    }

    @Test
    public void testCheckSingleValuedListWithEmptyArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be empty");
        Preconditions.checkSingleValuedList(Lists.<String>newArrayList(), "reference");
    }

    @Test
    public void testCheckSingleValuedListWithMultiValuedArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference can only have one value");
        Preconditions.checkSingleValuedList(Lists.newArrayList("abc", "def"), "reference");
    }

    @Test
    public void testCheckSingleValuedListWithBlankValuedArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null, empty or blank");
        Preconditions.checkSingleValuedList(Lists.newArrayList("  "), "reference");
    }

    @Test
    public void testCheckSingleValuedListWithSingleValuedArgument() throws Exception {
        Preconditions.checkSingleValuedList(Lists.newArrayList("abc"), "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithAlphabetAndDigitArgument() throws Exception {
        Preconditions.isAlphanumericSpace("ab2c", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithAlphabetAndHyphenArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric or space characters");
        Preconditions.isAlphanumericSpace("ab-c", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithAlphabetAndSpaceArgument() throws Exception {
        Preconditions.isAlphanumericSpace("ab c", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithAlphabetArgument() throws Exception {
        Preconditions.isAlphanumericSpace("abc", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithAlphanumericSpaceArgument() throws Exception {
        Preconditions.isAlphanumericSpace("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 ",
                                          "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithBlankArgument() throws Exception {
        Preconditions.isAlphanumericSpace("  ", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithEmptyArgument() throws Exception {
        Preconditions.isAlphanumericSpace("", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithNonAlphanumericSpaceArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric or space characters");
        Preconditions.isAlphanumericSpace("; ", "reference");
    }

    @Test
    public void testIsAlphanumericSpaceWithNullArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric or space characters");
        Preconditions.isAlphanumericSpace(null, "reference");
    }

    @Test
    public void testIsAlphanumericWithAlphabetAndDigitArgument() throws Exception {
        Preconditions.isAlphanumeric("ab2c", "reference");
    }

    @Test
    public void testIsAlphanumericWithAlphabetAndHyphenArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric characters");
        Preconditions.isAlphanumeric("ab-c", "reference");
    }

    @Test
    public void testIsAlphanumericWithAlphabetAndSpaceArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric characters");
        Preconditions.isAlphanumeric("ab c", "reference");
    }

    @Test
    public void testIsAlphanumericWithAlphabetArgument() throws Exception {
        Preconditions.isAlphanumeric("abc", "reference");
    }

    @Test
    public void testIsAlphanumericWithAlphanumericArgument() throws Exception {
        Preconditions.isAlphanumeric("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", "reference");
    }

    @Test
    public void testIsAlphanumericWithBlankArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric characters");
        Preconditions.isAlphanumeric("  ", "reference");
    }

    @Test
    public void testIsAlphanumericWithEmptyArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric characters");
        Preconditions.isAlphanumeric("", "reference");
    }

    @Test
    public void testIsAlphanumericWithNonAlphanumericArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric characters");
        Preconditions.isAlphanumeric(";abc", "reference");
    }

    @Test
    public void testIsAlphanumericWithNullArgument() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference is null or contains non alphanumeric characters");
        Preconditions.isAlphanumeric(null, "reference");
    }

    @Test
    public void testCheckLengthNullCharSequence() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null");
        Preconditions.checkLength((CharSequence) null, "reference", 5, 6);

    }

    @Test
    public void testCheckLengthCharSequenceShorter() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be shorter than 5");
        Preconditions.checkLength("1234", "reference", 5, 6);
    }

    @Test
    public void testCheckLengthCharSequenceLonger() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be longer than 6");
        Preconditions.checkLength("1234567", "reference", 5, 6);
    }

    @Test
    public void testCheckLengthCharSequenceOk01() throws Exception {
        Preconditions.checkLength("12345", "reference", 4, 5);
    }

    @Test
    public void testCheckLengthCharSequenceOk02() throws Exception {
        Preconditions.checkLength("12345", "reference", 4, 6);
    }

    @Test
    public void testCheckLengthCharSequenceOk03() throws Exception {
        Preconditions.checkLength("12345", "reference", 5, 5);
    }

    @Test
    public void testCheckLengthCharSequenceOk04() throws Exception {
        Preconditions.checkLength("12345", "reference", 5, 6);
    }

    @Test
    public void testCheckLengthNullCollection() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null");
        Preconditions.checkLength((Collection) null, "reference", 5, 6);

    }

    @Test
    public void testCheckLengthCollectionShorter() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be shorter than 5");
        Preconditions.checkLength(Lists.newArrayList("1", "2", "3", "4"), "reference", 5, 6);
    }

    @Test
    public void testCheckLengthCollectionLonger() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be longer than 6");
        Preconditions.checkLength(Lists.newArrayList("1", "2", "3", "4", "5", "6", "7"), "reference", 5, 6);
    }

    @Test
    public void testCheckLengthCollectionOk01() throws Exception {
        Preconditions.checkLength(Lists.newArrayList("1", "2", "3", "4", "5"), "reference", 4, 5);
    }

    @Test
    public void testCheckLengthCollectionOk02() throws Exception {
        Preconditions.checkLength(Lists.newArrayList("1", "2", "3", "4", "5"), "reference", 4, 6);
    }

    @Test
    public void testCheckLengthCollectionOk03() throws Exception {
        Preconditions.checkLength(Lists.newArrayList("1", "2", "3", "4", "5"), "reference", 5, 5);
    }

    @Test
    public void testCheckLengthCollectionOk04() throws Exception {
        Preconditions.checkLength(Lists.newArrayList("1", "2", "3", "4", "5"), "reference", 5, 6);
    }

    @Test
    public void testCheckLengthNullMap() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null");
        Preconditions.checkLength((Map<?, ?>) null, "reference", 5, 6);

    }

    @Test
    public void testCheckLengthMapShorter() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be shorter than 5");
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        Preconditions.checkLength(map, "reference", 5, 6);
    }

    @Test
    public void testCheckLengthMapLonger() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be longer than 6");
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        map.put("5", "5");
        map.put("6", "6");
        map.put("7", "7");
        Preconditions.checkLength(map, "reference", 5, 6);
    }

    @Test
    public void testCheckLengthMapOk01() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        map.put("5", "5");
        Preconditions.checkLength(map, "reference", 4, 5);
    }

    @Test
    public void testCheckLengthMapOk02() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        map.put("5", "5");
        Preconditions.checkLength(map, "reference", 4, 6);
    }

    @Test
    public void testCheckLengthMapOk03() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        map.put("5", "5");
        Preconditions.checkLength(map, "reference", 5, 5);
    }

    @Test
    public void testCheckLengthMapOk04() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        map.put("5", "5");
        Preconditions.checkLength(map, "reference", 5, 6);
    }

    @Test
    public void testCheckLengthNullArray() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be null");
        Preconditions.checkLength((String[]) null, "reference", 5, 6);

    }

    @Test
    public void testCheckLengthArrayShorter() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be shorter than 5");
        Preconditions.checkLength(new String[] {"1", "2", "3", "4"}, "reference", 5, 6);
    }

    @Test
    public void testCheckLengthArrayLonger() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("reference cannot be longer than 6");
        Preconditions.checkLength(new String[] {"1", "2", "3", "4", "5", "6", "7"}, "reference", 5, 6);
    }

    @Test
    public void testCheckLengthArrayOk01() throws Exception {
        Preconditions.checkLength(new String[] {"1", "2", "3", "4", "5"}, "reference", 4, 5);
    }

    @Test
    public void testCheckLengthArrayOk02() throws Exception {
        Preconditions.checkLength(new String[] {"1", "2", "3", "4", "5"}, "reference", 4, 6);
    }

    @Test
    public void testCheckLengthArrayOk03() throws Exception {
        Preconditions.checkLength(new String[] {"1", "2", "3", "4", "5"}, "reference", 5, 5);
    }

    @Test
    public void testCheckLengthArrayOk04() throws Exception {
        Preconditions.checkLength(new String[] {"1", "2", "3", "4", "5"}, "reference", 5, 6);
    }
}
