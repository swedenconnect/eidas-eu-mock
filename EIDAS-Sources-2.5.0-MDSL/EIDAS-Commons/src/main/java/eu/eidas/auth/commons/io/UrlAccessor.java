/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.util.Preconditions;

/**
 * A SingletonAccessor based on a URL.
 *
 * @since 1.1
 */
public final class UrlAccessor<T> implements SingletonAccessor<T> {

    @Nonnull
    private final StreamMarshaller<T> streamMarshaller;

    @Nonnull
    private final URL url;

    public UrlAccessor(@Nonnull StreamMarshaller<T> streamMarshaller, @Nonnull URL url) {
        Preconditions.checkNotNull(streamMarshaller, "streamMarshaller");
        Preconditions.checkNotNull(url, "url");
        this.streamMarshaller = streamMarshaller;
        this.url = url;
    }

    @Nullable
    @Override
    public T get() throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoInput(true);
        try (InputStream inputStream = urlConnection.getInputStream()) {
            return streamMarshaller.unmarshal(inputStream);
        }
    }

    @Nonnull
    public URL getUrl() {
        return url;
    }

    @Override
    public void set(@Nonnull T newValue) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoOutput(true);
        try (OutputStream outputStream = urlConnection.getOutputStream()) {
            streamMarshaller.marshal(newValue, outputStream);
        }
    }
}
