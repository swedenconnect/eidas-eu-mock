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
package eu.eidas.auth.commons.light.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LightTokenTest {

    private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";

    private static final String ISSUER = "MYSPECIFIC";

    private static LocalDateTime TIMESTAMP = LocalDateTime.parse("1956-10-23 10:52:01 698", AbstractLightToken.LIGHTTOKEN_DATE_FORMAT);

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
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertNotNull(lightToken);
    }

    @Test
    public void testGetKey() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertEquals(KEY, lightToken.getKey());
    }

    @Test
    public void testValidationOnBuildForMissingId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id cannot be null");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.issuer(ISSUER);
        lightToken.createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()));
        lightToken.build();
    }

    @Test
    public void testValidationOnBuildForMissingIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer cannot be null");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.id(ID);
        lightToken.createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()));
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
        lightToken.createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()));
        lightToken.build();
    }

    @Test
    public void testValidationSeparatorInIssuer() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer contains separator character");
        LightToken.Builder lightToken = new LightToken.Builder();
        lightToken.id(ID);
        lightToken.issuer(AbstractLightToken.SEPARATOR);
        lightToken.createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()));
        lightToken.build();
    }

    @Test
    public void testCopyConstructor() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();

        LightToken lightToken2 = LightToken.builder(lightToken1).build();

        assertEquals(lightToken1, lightToken2);
    }

    @Test
    public void testDefaultLightTokenEquals() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        LightToken lightToken2 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertEquals(lightToken1, lightToken2);
        assertTrue(lightToken1.equals(lightToken2));
    }

    @Test
    public void testDefaultLightTokenNotEquals() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        LightToken lightToken2 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        assertNotEquals(lightToken1, lightToken2);
        assertFalse(lightToken1.equals(lightToken2));
    }

    @Test
    public void testHashLightToken() {
        LightToken lightToken1 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        LightToken lightToken2 = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertEquals(lightToken1.hashCode(), lightToken2.hashCode());
    }

    @Test
    public void testGetId() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertThat(lightToken.getId(), is(ID));
    }

    @Test
    public void testGetIssuer() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertThat(lightToken.getIssuer(), is(ISSUER));
    }

    @Test
    public void testGetCreatedOn() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertThat(lightToken.getCreatedOn(), is(TIMESTAMP.atZone(ZoneId.systemDefault())));
    }

    @Test
    public void testGetFormattedCreatedOn() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertThat(lightToken.getFormattedCreatedOn(), is(FORMATTED_TIMESTAMP));
    }

    @Test
    public void testToString() {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP.atZone(ZoneId.systemDefault()))
                .build();
        assertEquals(STRING, lightToken.toString());
    }

}
