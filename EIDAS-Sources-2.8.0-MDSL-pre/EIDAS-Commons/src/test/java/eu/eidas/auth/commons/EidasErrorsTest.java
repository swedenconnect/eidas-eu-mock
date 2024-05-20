/*
 * Copyright (c) 2022 by European Commission
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

package eu.eidas.auth.commons;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;


/**
 * Class to test the correct functioning of {@link EidasErrors}
 */
public class EidasErrorsTest {

    /**
     * Method to test the retrieval of values loaded in {@link EidasErrors}
     * Must succeed.
     */
    @Test
    public void testGet() {
        assertEquals("202002",EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()));
        assertEquals("invalid.connector.samlrequest",EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
    }

    @Test
    public void testEidasErrorKeysAreInProperties() {
        final List<String> missingKeys = Stream.of(EidasErrorKey.values())
                .map(ek -> Arrays.asList(ek.errorMessage(), ek.errorCode()))
                .flatMap(Collection::stream)
                .filter(key -> EidasErrors.get(key) == null)
                .sorted()
                .collect(Collectors.toList());

                Assert.assertTrue(
                        "eidasErrors.properties is missing "+ missingKeys.size() +" keys defined in EidasErrorKey: \n" +
                        String.join("\n", missingKeys), missingKeys.isEmpty());
    }

    @Test
    public void testPropertiesAreInEidasErrorKey() throws NoSuchFieldException, IllegalAccessException {
        Field propertiesField = EidasErrors.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        final HashSet<String> propertyKeys = new HashSet<>(((ImmutableMap<String, String>) propertiesField.get(null)).keySet());
        final Set<String> eidasErrorKeyValueSet = Stream.of(EidasErrorKey.values())
                .map(ek -> Arrays.asList(ek.errorMessage(), ek.errorCode()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        propertyKeys.removeAll(eidasErrorKeyValueSet);
        final List<String> propertyKeysSorted = propertyKeys.stream().sorted().collect(Collectors.toList());

        Assert.assertTrue(
                "eidasError.properties contains " + propertyKeys.size() + " keys not defined in EidasErrorKey\n" +
                        String.join("\n", propertyKeysSorted), propertyKeys.isEmpty());
    }
}