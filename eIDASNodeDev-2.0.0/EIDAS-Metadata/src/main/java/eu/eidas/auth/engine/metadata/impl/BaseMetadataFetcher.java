/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.auth.engine.metadata.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;
import net.shibboleth.utilities.java.support.httpclient.TLSSocketFactory;
import net.shibboleth.utilities.java.support.httpclient.TLSSocketFactoryBuilder;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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

    private static final Pattern TLS_SPLITTER = Pattern.compile("[,;]");

    protected EntityDescriptor fetchEntityDescriptor(@Nonnull String url) throws EIDASMetadataProviderException {
        EntityDescriptor entityDescriptor;
        if (!isAllowedMetadataUrl(url)) {
            throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA_SOURCE.errorCode(),
                    EidasErrorKey.SAML_ENGINE_INVALID_METADATA_SOURCE.errorMessage(),
                    "Metadata URL is not secure: \"" + url + "\"");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new EIDASMetadataProviderException("Invalid URL : " + url);
        }
        HttpClientBuilder httpClientBuilder = new HttpClientBuilder();

        DomCachingHttpMetadataProvider provider = null;

        try {
            // This registers a socket factory for the https scheme:
            // specifying a null X509KeyManager and a null X509TrustManager is going to use the default ones from the JVM:
            final TLSSocketFactory factory = newSslSocketFactory();
            httpClientBuilder.setTLSSocketFactory(factory);
            provider = new DomCachingHttpMetadataProvider(httpClientBuilder.buildClient(), url);
            provider.setParserPool(OpenSamlHelper.getSecuredParserPool());
            provider.initializeNonFinal();

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
        } catch (ResolverException e) {
            LOG.error("Error fetching metadata from URL \"" + url + "\": " + e, e);
            throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorCode(),
                    EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorMessage(), e);

        } catch (Exception e) {
            e.printStackTrace();
            throw new EIDASMetadataProviderException(e.getMessage());
        } finally {
            if (provider != null) {
                provider.destroy();
            }
        }
        return entityDescriptor;
    }

    @Nonnull
    @Override
    public EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {
        // 1) fetch
        EntityDescriptor entityDescriptor = fetchEntityDescriptor(url);
        // 2) validate the digital signature
        if (mustValidateSignature(url)) {
            metadataSigner.validateMetadataSignature(entityDescriptor);
        }
        // 3) release the DOM
        entityDescriptor.releaseDOM();
        final EidasMetadataParametersI eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(entityDescriptor);
        return eidasMetadataParameters;
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
            return TLS_SPLITTER.split(DEFAULT_TLS_ENABLED_PROTOCOLS);
        }
        ImmutableList.Builder<String> enabledProtocols = ImmutableList.builder();
        String[] protocols = TLS_SPLITTER.split(tlsEnabledProtocols);
        for (String protocol : protocols) {
            String trimmed = StringUtils.trimToNull(protocol);
            if (StringUtils.isNotBlank(trimmed)) {
                enabledProtocols.add(trimmed);
            }
        }
        ImmutableList<String> list = enabledProtocols.build();
        if (list.isEmpty()) {
            return TLS_SPLITTER.split(DEFAULT_TLS_ENABLED_PROTOCOLS);
        }
        LOG.debug("TLS enabled protocols: {}", list);
        return Iterables.toArray(list, String.class);
    }

    protected abstract String[] getTlsEnabledCiphers();

    protected String[] getTlsEnabledCiphers(String tlsEnabledCiphers) {
        if (StringUtils.isBlank(tlsEnabledCiphers)) {
            LOG.debug("tlsEnabledCiphers is null");
            return null;
        }

        ImmutableList.Builder<String> enabledProtocols = ImmutableList.builder();
        String[] protocols = TLS_SPLITTER.split(tlsEnabledCiphers);
        for (String protocol : protocols) {
            String trimmed = StringUtils.trimToNull(protocol);
            if (StringUtils.isNotBlank(trimmed)) {
                enabledProtocols.add(trimmed);
            }
        }
        ImmutableList<String> list = enabledProtocols.build();

        LOG.debug("TLS enabled ciphers: {}", list);
        return Iterables.toArray(list, String.class);
    }

    protected boolean mustValidateSignature(@Nonnull String url) {
        return true;
    }

    protected TLSSocketFactory newSslSocketFactory() {
        X509HostnameVerifier hostnameVerifier;
        if (!Boolean.getBoolean(OpenSamlHelper.SYSPROP_HTTPCLIENT_HTTPS_DISABLE_HOSTNAME_VERIFICATION)) {
            hostnameVerifier = TLSSocketFactory.STRICT_HOSTNAME_VERIFIER;
        } else {
            hostnameVerifier = TLSSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        }

        final TLSSocketFactoryBuilder tlsSocketFactoryBuilder = new TLSSocketFactoryBuilder()
                .setHostnameVerifier(hostnameVerifier)
                .setEnabledProtocols(Arrays.asList(getTlsEnabledProtocols()));

        setTlsEnabledCipherSuites(tlsSocketFactoryBuilder);

        return tlsSocketFactoryBuilder.build();
    }

    /**
     * Method that sets external configuration cipher suites in the TLSSocketFactoryBuilder.
     *
     * If no cipher suites are set from external configuration does not anything so the default will be used.
     *
     * @param tlsSocketFactoryBuilder the TLSSocketFactoryBuilder
     */
    private void setTlsEnabledCipherSuites(TLSSocketFactoryBuilder tlsSocketFactoryBuilder) {
        final String[] tlsEnabledCiphers = getTlsEnabledCiphers();
        if (tlsEnabledCiphers != null) {
            List<String> cipherSuites = Arrays.asList(tlsEnabledCiphers);
            tlsSocketFactoryBuilder.setEnabledCipherSuites(cipherSuites);
        }
    }

    /**
     * Validate "validUntil" field of Metadata (EidasMetadataParametersI form) against saml engine clock
     *
     * @param eidasMetadataParameters
     * @param metadataClock
     * @return
     */
    protected boolean isValidUntilNow(EidasMetadataParametersI eidasMetadataParameters, MetadataClockI metadataClock) {
        final DateTime validUntil = eidasMetadataParameters.getValidUntil();
        if (null == validUntil) {
            return true;
        } else {
            return metadataClock.getCurrentTime().isBefore(validUntil);
        }
    }

}
