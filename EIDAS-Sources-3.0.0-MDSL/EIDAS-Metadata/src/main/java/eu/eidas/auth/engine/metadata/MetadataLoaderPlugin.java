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
package eu.eidas.auth.engine.metadata;

import eu.eidas.engine.exceptions.EIDASMetadataException;

import java.util.List;

/**
 * EIDAS metadata loader interface. The Loader is providing metadata fetching capabilities additional/different than the
 * HTTP/HTTPS client fetcher. Mainly used for testing, but it can help connecting to a centralized metadata provider
 * infrastructure, so no direct HTTP/HTTPS connections needed to get remote metadata.
 *
 */
public interface MetadataLoaderPlugin {

    void addListenerContentChanged( IStaticMetadataChangeListener listener);

    List<EntityDescriptorContainer> getEntityDescriptors() throws EIDASMetadataException;

}
