/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.protocol.impl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * EidasSamlBindingTest
 *
 * @since 1.1
 */
public final class EidasSamlBindingTest {

    @Test
    public void testFromBindingUri() throws Exception {
        for (final EidasSamlBinding value : EidasSamlBinding.values()) {
            assertThat(value, is(EidasSamlBinding.fromBindingUri(
                    (value.getBindingUri() == null ? null : value.getBindingUri().getBindingUri()))));
        }

        assertThat(EidasSamlBinding.POST,
                   is(EidasSamlBinding.fromBindingUri(EidasSamlBinding.POST.getBindingUri().getBindingUri())));
        assertThat(EidasSamlBinding.REDIRECT,
                   is(EidasSamlBinding.fromBindingUri(EidasSamlBinding.REDIRECT.getBindingUri().getBindingUri())));
        assertThat(EidasSamlBinding.EMPTY, is(EidasSamlBinding.fromBindingUri(null)));
    }

    @Test
    public void testFromName() throws Exception {
        for (final EidasSamlBinding value : EidasSamlBinding.values()) {
            assertThat(value, is(EidasSamlBinding.fromName(value.getName())));
        }

        assertThat(EidasSamlBinding.POST, is(EidasSamlBinding.fromName(EidasSamlBinding.POST.getName())));
        assertThat(EidasSamlBinding.REDIRECT, is(EidasSamlBinding.fromName(EidasSamlBinding.REDIRECT.getName())));
        assertThat(EidasSamlBinding.EMPTY, is(EidasSamlBinding.fromName(EidasSamlBinding.EMPTY.getName())));
    }

    @Test
    public void testGetBindingUri() throws Exception {
        assertThat(EidasSamlBinding.POST.getBindingUri(), is(SamlBindingUri.SAML2_POST));
        assertThat(EidasSamlBinding.REDIRECT.getBindingUri(), is(SamlBindingUri.SAML2_REDIRECT));
        assertThat(EidasSamlBinding.EMPTY.getBindingUri(), is((SamlBindingUri) null));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(EidasSamlBinding.POST.getName(), is("POST"));
        assertThat(EidasSamlBinding.REDIRECT.getName(), is("GET"));
        assertThat(EidasSamlBinding.EMPTY.getName(), is("EMPTY"));
    }

    @Test
    public void testToString() throws Exception {
        for (final EidasSamlBinding value : EidasSamlBinding.values()) {
            assertThat(value.toString(), is(value.getName()));
        }

        assertThat(EidasSamlBinding.POST.toString(), is("POST"));
        assertThat(EidasSamlBinding.REDIRECT.toString(), is("GET"));
        assertThat(EidasSamlBinding.EMPTY.toString(), is("EMPTY"));
    }

    @Test
    public void testToBindingUri() throws Exception {
        for (final EidasSamlBinding value : EidasSamlBinding.values()) {
            assertThat(EidasSamlBinding.toBindingUri(value.getName()),
                       is((value.getBindingUri() == null ? null : value.getBindingUri().getBindingUri())));
        }

        assertThat(EidasSamlBinding.toBindingUri("POST"), is(SamlBindingUri.SAML2_POST.getBindingUri()));
        assertThat(EidasSamlBinding.toBindingUri("GET"), is(SamlBindingUri.SAML2_REDIRECT.getBindingUri()));
        assertThat(EidasSamlBinding.toBindingUri("EMPTY"), is((String) null));
    }

    @Test
    public void testToName() throws Exception {
        for (final EidasSamlBinding value : EidasSamlBinding.values()) {
            assertThat(EidasSamlBinding.toName(
                    (value.getBindingUri() == null ? null : value.getBindingUri().getBindingUri())),
                       is(value.getName()));
        }

        assertThat(EidasSamlBinding.toName(SamlBindingUri.SAML2_POST.getBindingUri()), is("POST"));
        assertThat(EidasSamlBinding.toName(SamlBindingUri.SAML2_REDIRECT.getBindingUri()), is("GET"));
        assertThat(EidasSamlBinding.toName(null), is("EMPTY"));
    }
}
