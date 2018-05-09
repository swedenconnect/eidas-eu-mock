/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.commons.light.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LightTokenTest {

    private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";

    private static final String ISSUER = "MYSPECIFIC";

    private static DateTime TIMESTAMP = AbstractLightToken.LIGHTTOKEN_DATE_FORMAT.parseDateTime("1956-10-23 10:52:01 698");

    private static String FORMATTED_TIMESTAMP = "1956-10-23 10:52:01 698";

    private static String STRING = "LightToken{id='f5e7e0f5-b9b8-4256-a7d0-4090141b326d', issuer='MYSPECIFIC', createdOn='1956-10-23 10:52:01 698'}";

    private static String KEY = new StringBuilder()
            .append("MYSPECIFIC")
            .append(AbstractLightToken.SEPARATOR)
            .append("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
            .append(AbstractLightToken.SEPARATOR)
            .append("1956-10-23 10:52:01 698")
            .toString();

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLightTokenCreation() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertNotNull(lightToken);
    }

    @Test
    public void testGetKey() throws Exception {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertEquals(KEY, lightToken.getKey());
    }

    @Test
    public void testValidationOnBuildForMissingId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id cannot be null");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.issuer(ISSUER);
        lightToken.createdOn(TIMESTAMP);
        lightToken.build();
    }

    @Test
    public void testValidationOnBuildForMissingIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer cannot be null");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.id(ID);
        lightToken.createdOn(TIMESTAMP);
        lightToken.build();
    }

    @Test
    public void testValidationOnBuildForMissingCreatedOn() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("createdOn cannot be null");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.id(ID);
        lightToken.issuer(ISSUER);
        lightToken.build();
    }

    @Test
    public void testValidationOnShortIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer cannot be null, empty or blank");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.id(ID);
        lightToken.issuer("");
        lightToken.createdOn(TIMESTAMP);
        lightToken.build();
    }

    @Test
    public void testValidationSeparatorInIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer contains separator character");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.id(ID);
        lightToken.issuer(AbstractLightToken.SEPARATOR);
        lightToken.createdOn(TIMESTAMP);
        lightToken.build();
    }

    @Test
    public void testCopyConstructor() throws Exception {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();

        LightToken lightToken2 = LightToken.builder(lightToken1).build();

        assertEquals(lightToken1, lightToken2);
    }

    @Test
    public void testDefaultLightTokenEquals() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        LightToken lightToken2 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertEquals(lightToken1, lightToken2);
        assertTrue(lightToken1.equals(lightToken2));
    }

    @Test
    public void testDefaultLightTokenNotEquals() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        LightToken lightToken2 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(new DateTime(DateTimeZone.getDefault()))
                .build();
        assertNotEquals(lightToken1, lightToken2);
        assertFalse(lightToken1.equals(lightToken2));
    }

    @Test
    public void testHashLightToken() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        LightToken lightToken2 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertEquals(lightToken1.hashCode(), lightToken2.hashCode());
    }

    @Test
    public void testGetId() throws Exception {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertThat(lightToken.getId(), is(ID));
    }

    @Test
    public void testGetIssuer() throws Exception {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertThat(lightToken.getIssuer(), is(ISSUER));
    }

    @Test
    public void testGetCreatedOn() throws Exception {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertThat(lightToken.getCreatedOn(), is(TIMESTAMP));
    }

    @Test
    public void testGetFormattedCreatedOn() throws Exception {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertThat(lightToken.getFormattedCreatedOn(), is(FORMATTED_TIMESTAMP));
    }

    @Test
    public void testToString() throws Exception {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        assertEquals(STRING, lightToken.toString());
    }

}
