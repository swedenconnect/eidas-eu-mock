/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.engine.metadata.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.AbstractProtocolEngine;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.TLSProtocolSocketFactory;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.x509.tls.StrictHostnameVerifier;
import org.opensaml.xml.signature.SignableXMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The base implementation of the {@link MetadataFetcherI} interface.
 * <p>
 * This default implementation only fetches the metadata from the URL and validates its digital signature using the
 * {@link MetadataSignerI#validateMetadataSignature(SignableXMLObject)}.
 *
 * @since 1.1
 */
public abstract class BaseMetadataFetcher implements MetadataFetcherI {

    private static final Logger LOG = LoggerFactory.getLogger(BaseMetadataFetcher.class);

    private static final Pattern HTTP_OR_HTTPS_URL = Pattern.compile("^https?://.*$");

    private static final String DEFAULT_TLS_ENABLED_PROTOCOLS = "TLSv1.1,TLSv1.2";

    private static final Pattern TLS_PROTOCOLS_SPLITTER = Pattern.compile("[,;]");

    protected EntityDescriptor fetchEntityDescriptor(@Nonnull String url) throws EIDASMetadataProviderException {
        if (!isAllowedMetadataUrl(url)) {
            throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA_SOURCE.errorCode(),
                                                     EidasErrorKey.SAML_ENGINE_INVALID_METADATA_SOURCE.errorMessage(),
                                                     "Metadata URL is not secure: \"" + url + "\"");
        }
        URL metadataUrl = null;
        try {
            metadataUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new EIDASMetadataProviderException("Invalid URL : " + url);
        }
        HttpClientBuilder httpClientBuilder = new HttpClientBuilder();

        // This registers a socket factory for the https scheme:
        // specifying a null X509KeyManager and a null X509TrustManager is going to use the default ones from the JVM:
        httpClientBuilder.setHttpsProtocolSocketFactory(newSslSocketFactory());

        HTTPMetadataProvider provider = null;
        EntityDescriptor entityDescriptor;

        try {
            provider =
                    new DomCachingHttpMetadataProvider(null, httpClientBuilder.buildClient(), url);
            provider.setParserPool(AbstractProtocolEngine.getSecuredParserPool());
            provider.initialize();
            XMLObject metadata = provider.getMetadata();
            if (metadata instanceof EntityDescriptor) {
                entityDescriptor = (EntityDescriptor) metadata;
            } else {
                //CAVEAT: the entity descriptor should have its id equal to the url (issuer url)
                entityDescriptor = provider.getEntityDescriptor(url);
            }
            if (null == entityDescriptor) {
                throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                                                         EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                                                         "No entity descriptor for URL \"" + url + "\"");
            }
            if (!entityDescriptor.isValid()) {
                throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorCode(),
                                                         EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorMessage(),
                                                         "Invalid entity descriptor for URL \"" + url + "\"");
            }
        } catch (MetadataProviderException mpe) {
            LOG.error("Error fetching metadata from URL \"" + url + "\": " + mpe, mpe);
            throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorCode(),
                                                     EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorMessage(), mpe);
        } finally {
            if (provider != null) {
                provider.destroy();
            }
        }
        return entityDescriptor;
    }

    @Nonnull
    @Override
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        // 1) fetch
        EntityDescriptor entityDescriptor = fetchEntityDescriptor(url);
        // 2) validate the digital signature
        if (mustValidateSignature(url)) {
            metadataSigner.validateMetadataSignature(entityDescriptor);
        }
        // 3) release the DOM
        entityDescriptor.releaseDOM();
        return entityDescriptor;
    }

    @Nullable
    public IDPSSODescriptor getIDPSSODescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        EntityDescriptor entityDescriptor = getEntityDescriptor(url, metadataSigner);
        return MetadataUtil.getIDPSSODescriptor(entityDescriptor);
    }

    @Nullable
    public SPSSODescriptor getSPSSODescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        EntityDescriptor entityDescriptor = getEntityDescriptor(url, metadataSigner);
        return MetadataUtil.getSPSSODescriptor(entityDescriptor);
    }

    protected boolean isAllowedMetadataUrl(@Nonnull String url) {
        if (StringUtils.isNotBlank(url)) {
            String lowerCaseUrl = url.toLowerCase(Locale.ENGLISH);
            if (mustUseHttps()) {
                return lowerCaseUrl.startsWith("https://");
            } else {
                return HTTP_OR_HTTPS_URL.matcher(lowerCaseUrl).matches();
            }
        }
        return false;
    }

    protected boolean mustUseHttps() {
        return true;
    }

    protected abstract String[] getTlsEnabledProtocols();

    protected String[] getTlsEnabledProtocols(String tlsEnabledProtocols) {
        if (StringUtils.isBlank(tlsEnabledProtocols)) {
            LOG.debug("tlsEnabledProtocols is null, the default protocols [TLSv1.1,TLSv1.2] will be used");
            return TLS_PROTOCOLS_SPLITTER.split(DEFAULT_TLS_ENABLED_PROTOCOLS);
        }
        ImmutableList.Builder<String> enabledProtocols = ImmutableList.builder();
        String[] protocols = TLS_PROTOCOLS_SPLITTER.split(tlsEnabledProtocols);
        for (String protocol : protocols) {
            String trimmed = StringUtils.trimToNull(protocol);
            if (StringUtils.isNotBlank(trimmed)) {
                enabledProtocols.add(trimmed);
            }
        }
        ImmutableList<String> list = enabledProtocols.build();
        if (list.isEmpty()) { return TLS_PROTOCOLS_SPLITTER.split(DEFAULT_TLS_ENABLED_PROTOCOLS); }
        LOG.debug("TLS enabled protocols: {}", list);
        return Iterables.toArray(list, String.class);
    }

    protected boolean mustValidateSignature(@Nonnull String url) {
        return true;
    }

    /**
     * Override this method to plug your own SSLSocketFactory.
     * <p>
     * This default implementation relies on the default one from the JVM, i.e. using the default trustStore
     * ($JRE/lib/security/cacerts).
     *
     * @return the SecureProtocolSocketFactory instance to be used to connect to https metadata URLs.
     */
    @Nonnull
    protected SecureProtocolSocketFactory newSslSocketFactory() {

        HostnameVerifier hostnameVerifier;

        if (!Boolean.getBoolean(DefaultBootstrap.SYSPROP_HTTPCLIENT_HTTPS_DISABLE_HOSTNAME_VERIFICATION)) {
            hostnameVerifier = new StrictHostnameVerifier();
        } else {
            hostnameVerifier = org.apache.commons.ssl.HostnameVerifier.ALLOW_ALL;
        }

        TLSProtocolSocketFactory tlsProtocolSocketFactory = new TLSProtocolSocketFactory(null, null, hostnameVerifier) {
            @Override
            protected void verifyHostname(Socket socket) throws SSLException {
                if (socket instanceof SSLSocket) {
                    SSLSocket sslSocket = (SSLSocket) socket;
                    sslSocket.setEnabledProtocols(getTlsEnabledProtocols());
                    try {
                        sslSocket.startHandshake();
                    } catch (IOException e) {
                        throw new SSLException(e);
                    }
                    SSLSession sslSession = sslSocket.getSession();
                    if ("SSL_NULL_WITH_NULL_NULL".equals(sslSession.getCipherSuite())) {
                        throw new SSLException("SSLSession was invalid: Likely implicit handshake failure: "
                                + "Set system property javax.net.debug=all for details");
                    }
                    super.verifyHostname(sslSocket);
                }
            }
        };

        Protocol.registerProtocol("https", new Protocol("https", tlsProtocolSocketFactory, 443));

        return tlsProtocolSocketFactory;
    }
}
