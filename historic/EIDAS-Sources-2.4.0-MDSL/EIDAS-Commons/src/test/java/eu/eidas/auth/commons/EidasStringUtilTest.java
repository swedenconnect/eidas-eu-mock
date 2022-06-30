/*
 * Copyright (c) 2018 by European Commission
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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class EidasStringUtilTest {

    @Test
    public void get2TokensSemicolon() {
        List<String> tokens = EidasStringUtil.getTokens("CEF:eIDAS-ref:1.4.2;CEF:eIDAS-ref:2.2");
        Assert.assertEquals(2, tokens.size());
    }

    @Test
    public void get2TokensComma() {
        List<String> tokens = EidasStringUtil.getTokens("CEF:eIDAS-ref:1.4.2,CEF:eIDAS-ref:2.2");
        Assert.assertEquals(2, tokens.size());
    }

    @Test
    public void get1TokenNoDelimiter() {
        List<String> tokens = EidasStringUtil.getTokens("CEF:eIDAS-ref:1.4.2_CEF:eIDAS-ref:2.2");
        Assert.assertEquals(1, tokens.size());
    }


    @Test
    public void getTokenNullInput() {
        List<String> tokens = EidasStringUtil.getTokens(null);
        Assert.assertEquals(0, tokens.size());
    }

    @Test
    public void getTokenEmptyInput() {
        String EMPTY_STRING = "";
        List<String> tokens = EidasStringUtil.getTokens(EMPTY_STRING);
        Assert.assertEquals(0, tokens.size());
    }
}