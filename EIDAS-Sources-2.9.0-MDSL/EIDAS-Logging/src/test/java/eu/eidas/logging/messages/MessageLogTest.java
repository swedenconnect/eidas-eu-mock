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

package eu.eidas.logging.messages;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link MessageLog}
 */
public class MessageLogTest {

    /**
     * Test method for {@link MessageLog.Tag}
     * Test the format output of the {@link MessageLog.Tag#toString()} method.
     * <br>
     * Must succeed.
     */
    @Test
    public void testTagFormatting() {
        MessageLog.Tag testTag = new MessageLog.Tag("test");
        testTag.setValue("ultime");

        String expectedValue = "test          ultime,";
        String actualValue = testTag.toString();
        Assert.assertEquals(expectedValue, actualValue);
    }

}
