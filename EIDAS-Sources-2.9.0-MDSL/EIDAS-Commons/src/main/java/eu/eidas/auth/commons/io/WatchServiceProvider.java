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

package eu.eidas.auth.commons.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Holds instances of watchService per watched file.
 * <p>
 * The watchservice is bound to the parent, for multiple instance this can be the same parent.
 * we have one instance per file since poll() can only be invoked once.
 */
class WatchServiceProvider {

    private static final ConcurrentMap<Path, WatchService> trackedFiles = new ConcurrentHashMap<>();

    public static synchronized WatchService getInstance(Path path) throws IOException {
        if (!trackedFiles.containsKey(path)) {
            final WatchService newWatchService = FileSystems.getDefault().newWatchService();
            path.getParent().register(newWatchService, StandardWatchEventKinds.ENTRY_MODIFY);
            trackedFiles.put(path, newWatchService);
        }
        return trackedFiles.get(path);
    }




}
