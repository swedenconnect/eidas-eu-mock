package eu.eidas.auth.commons;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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