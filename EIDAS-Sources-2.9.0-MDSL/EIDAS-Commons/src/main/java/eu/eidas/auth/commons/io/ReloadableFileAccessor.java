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

import eu.eidas.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper handling a reloadable file on the filesystem to marshal and unmarshal the content of a given file into one
 * instance of the given type.
 *
 * @since 1.1
 */
public final class ReloadableFileAccessor<T> implements SingletonAccessor<T> {

    /**
     * Immutable Class to make the inner state atomic ie one field cannot be modified without modifying the state as a
     * whole.
     */
    @SuppressWarnings("PackageVisibleField")
    private static final class AtomicState<T> {

        @Nonnull
        final File file;

        final long lastModified;

        @Nullable
        final T value;

        final boolean fileLockedForWriting;

        private final WatchService watchService;

        AtomicState(@Nonnull File fileToAccess, long lastMod, WatchService watchService, @Nullable T val, boolean fileLockedForWr) {
            file = fileToAccess;
            lastModified = lastMod;
            value = val;
            fileLockedForWriting = fileLockedForWr;
            this.watchService = watchService;
        }

        boolean externallyModified() {
            final WatchKey update = watchService.poll();
            final boolean changed = update != null && update.pollEvents().stream().anyMatch(
                    watchEvent -> watchEvent.context().toString().equals(file.getName())
            );
            if(update != null) update.reset();
            return !fileLockedForWriting && lastModified != file.lastModified() || changed;
        }
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReloadableFileAccessor.class);

    @Nonnull
    private final FileMarshaller<T> fileMarshaller;

    @Nonnull
    private final String filename;

    private final ReentrantLock lock = new ReentrantLock();

    private final WatchService watchService;

    @Nonnull
    private final AtomicReference<AtomicState<T>> referenceToState = new AtomicReference<AtomicState<T>>();

    @SuppressWarnings("squid:S2637")
    public ReloadableFileAccessor(@Nonnull FileMarshaller<T> fileMarshaller, @Nonnull String filename) {
        this(fileMarshaller, filename, getResourceIgnoredException(filename));
    }

    @SuppressWarnings("squid:S2637")
    public ReloadableFileAccessor(@Nonnull FileMarshaller<T> marshaller,
                                  @Nonnull String fileName,
                                  @Nonnull URL fileUrl) {
        Preconditions.checkNotNull(marshaller, "fileMarshaller");
        Preconditions.checkNotBlank(fileName, "fileName");
        File file;
        fileMarshaller = marshaller;
        filename = fileName;
        try {
            file = newFile(filename, fileUrl);
            watchService = WatchServiceProvider.getInstance(file.toPath());
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        referenceToState.set(new AtomicState<T>(file, 0L, watchService, null, false));
    }

    private static URL getResourceIgnoredException(@Nonnull String path) {
        try {
            return ResourceLocator.getResource(path);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @Nonnull
    private static File newFile(@Nonnull String resourceName, @Nonnull URL fileUrl) throws IOException {
        if (!"file".equals(fileUrl.getProtocol())) {
            throw new IOException(
                    "Resource \"" + resourceName + "\" is not available at a file URL: \"" + fileUrl.toExternalForm()
                            + "\"");
        }
        URI fileUri;
        try {
            fileUri = fileUrl.toURI();
        } catch (URISyntaxException use) {
            throw new IOException(
                    "File \"" + resourceName + "\" at: \"" + fileUrl.toExternalForm() + "\" has an invalid URI syntax: "
                            + use, use);
        }
        File file = new File(fileUri);
        String absolutePath = file.getCanonicalPath();
        if (!file.exists()) {
            throw new IOException("File \"" + resourceName + "\" cannot be found from path: \"" + absolutePath + "\"");
        }
        if (file.isDirectory()) {
            throw new IOException(
                    "File \"" + resourceName + "\" represents a directory (path: \"" + absolutePath + "\")");
        }
        if (!file.canRead()) {
            throw new IOException("File \"" + resourceName + "\" cannot be read at path: \"" + absolutePath + "\"");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found file \"" + resourceName + "\" on the filesystem path: \"" + absolutePath + "\"");
        }
        return file;
    }

    private boolean checkIfModified() throws IOException {
        AtomicState<T> currentState = referenceToState.get();
        if (currentState.externallyModified()) {
            reload(currentState);
            return true;
        }
        return false;
    }

    private File findFile() throws IOException {
        URL fileUrl = ResourceLocator.getResource(filename);
        return newFile(filename, fileUrl);
    }

    @Override
    @Nullable
    public final T get() throws IOException {
        return getState().value;
    }

    @Nonnull
    public String getFilename() {
        return filename;
    }

    public final long getLastModified() throws IOException {
        return getState().lastModified;
    }

    private AtomicState<T> getState() throws IOException {
        checkIfModified();
        return referenceToState.get();
    }

    /**
     * Should only happen when the file is externally modified by an administrator.
     * <p>
     * Is called after the "currentState.externallyModified()" condition which is always false while this DAO is
     * writing.
     */
    private synchronized void reload(AtomicState<T> currentState) throws IOException {
        // We lock because we want to prevent reloading while writing.
        if (!lock.tryLock()) {
            // if we cannot acquire the lock, and if there is already a cached result, we keep returning this stale result rather than blocking
            //noinspection VariableNotUsedInsideIf
            if (null != referenceToState.get().value) {
                return;
            } else {
                // We care if 2 threads load the initial value concurrently - we prefer that one thread loads it and the rest waits.
                lock.lock();
            }
        } // else we acquired the lock
        try {
            // compute new state:
            File file = findFile();
            long lastModified = file.lastModified();
            T value = fileMarshaller.unmarshal(file);
            AtomicState<T> newState = new AtomicState<T>(file, lastModified, watchService, value, false);

            referenceToState.compareAndSet(currentState, newState);
            // if OK, updated
            // else another thread already changed the AtomicState
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void set(@Nonnull T newValue) throws IOException {
        // we want synchronized writes into the file
        lock.lock();
        try {
            // Warning: a write is ignored if the file had been modified in between by an operator
            boolean modified = checkIfModified();
            if (modified) {
                // we are losing all the delta modifications between this set() and the last get()
                return;
            }

            AtomicState<T> currentState = referenceToState.get();
            URI uri = currentState.file.toURI();
            // lock before starting to write in the file:
            // Note that this is needed to prevent a partial write on the disk to change the date of the file
            // which would then trigger a reload for all other threads needing to use the cached value while the file is being written.
            // Without this mechanism, all the other reading threads would want to reload the file and race for the acquisition of the lock
            // which is already held by this thread, so all these other threads would wait on the lock until the end of this method.
            // With this mechanism, all these other reading threads do not wait on the lock but keep working with the previously cached value
            // until writing to the file is finished and only then will the new value be seen by all threads.
            referenceToState.set(
                    new AtomicState<T>(currentState.file, currentState.lastModified, watchService, currentState.value, true));

            File output = new File(uri);
            fileMarshaller.marshal(newValue, output);

            long lastModified = output.lastModified();

            referenceToState.set(new AtomicState<T>(output, lastModified,watchService, newValue, false));
        } finally {
            try {
                AtomicState<T> currentState = referenceToState.get();
                if (currentState.fileLockedForWriting) {
                    referenceToState.set(
                            new AtomicState<T>(currentState.file, currentState.lastModified, watchService, currentState.value, false));
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
