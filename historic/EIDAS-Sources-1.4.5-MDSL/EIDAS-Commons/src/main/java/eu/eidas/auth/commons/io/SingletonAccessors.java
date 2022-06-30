package eu.eidas.auth.commons.io;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;

/**
 * Static factory methods pertaining to {@link SingletonAccessor}.
 *
 * @since 1.1
 */
public final class SingletonAccessors {

    static final class ImmutableAccessor<T> implements SingletonAccessor<T> {

        @Nullable
        private final T t;

        ImmutableAccessor(@Nullable T t) {
            this.t = t;
        }

        @Nullable
        @Override
        public T get() {
            return t;
        }

        @Override
        public void set(@Nullable T newValue) {
            throw new UnsupportedOperationException();
        }
    }

    static final class LazyAccessor<T> implements SingletonAccessor<T> {

        private final AtomicReference<T> reference = new AtomicReference<>();

        @Nonnull
        private final SingletonAccessor<T> wrappedAccessor;

        LazyAccessor(@Nonnull SingletonAccessor<T> wrappedaccessor) {
            Preconditions.checkNotNull(wrappedaccessor, "wrappedAccessor");
            wrappedAccessor = wrappedaccessor;
        }

        @Nullable
        @Override
        public T get() throws IOException {
            while (true) {
                T value = reference.get();
                if (null == value) {
                    T newValue = wrappedAccessor.get();
                    if (reference.compareAndSet(null, newValue)) {
                        return newValue;
                    }
                } else {
                    return value;
                }
            }
        }

        @Override
        public void set(@Nonnull T newValue) throws IOException {
            wrappedAccessor.set(newValue);
            reference.set(newValue);
        }
    }

    static final class MutableAccessor<T> implements SingletonAccessor<T> {

        @Nullable
        private volatile T t;

        MutableAccessor(@Nullable T t) {
            this.t = t;
        }

        @Nullable
        @Override
        public T get() {
            return t;
        }

        @Override
        public void set(@Nullable T newValue) {
            t = newValue;
        }
    }

    /**
     * The list of all URL protocols we support and basically which can be loaded by an application server classLoader.
     */
    public enum UrlProtocol {

        /**
         * URL protocol for a file in the file system: "file"
         */
        FILE("file"),

        /**
         * URL protocol for an entry from a jar file: "jar"
         */
        JAR("jar"),

        /**
         * URL protocol for an entry from a zip file: "zip"
         */
        ZIP("zip"),

        // Adding support for the virtualFiles used in jboss to access the content of a war

        /**
         * URL protocol for a general JBoss VFS resource: "vfs"
         */
        VFS("vfs"),

        /**
         * URL protocol for an entry from a JBoss jar file: "vfszip"
         */
        VFSZIP("vfszip"),

        /**
         * URL protocol for a JBoss file system resource: "vfsfile"
         */
        VFSFILE("vfsfile"),

        /**
         * URL protocol for an entry from a WebSphere jar file: "wsjar"
         */
        WSJAR("wsjar"),

        // put the ; on a separate line to make merges easier
        ;

        private static final EnumMapper<String, UrlProtocol> MAPPER =
                new EnumMapper<String, UrlProtocol>(new KeyAccessor<String, UrlProtocol>() {

                    @Nonnull
                    @Override
                    public String getKey(@Nonnull UrlProtocol urlProtocol) {
                        return urlProtocol.getProtocol();
                    }
                }, Canonicalizers.trimLowerCase(), values());

        @Nullable
        public static UrlProtocol fromString(@Nonnull String value) {
            return MAPPER.fromKey(value);
        }

        public static EnumMapper<String, UrlProtocol> mapper() {
            return MAPPER;
        }

        @Nonnull
        private final transient String protocol;

        UrlProtocol(@Nonnull String key) {
            protocol = key;
        }

        @Nonnull
        public String getProtocol() {
            return protocol;
        }

        @Override
        public String toString() {
            return protocol;
        }
    }

    @Nonnull
    public static <T> SingletonAccessor<T> immutableAccessor(@Nonnull final T t) {
        return new ImmutableAccessor<>(t);
    }

    @Nonnull
    public static <T> SingletonAccessor<T> mutableAccessor(@Nonnull final T t) {
        return new MutableAccessor<>(t);
    }

    @Nonnull
    public static <T> SingletonAccessor<T> newFileAccessor(@Nonnull String fileName,
                                                           @Nullable String defaultPath,
                                                           @Nonnull FileMarshaller<T> fileMarshaller,
                                                           @Nonnull StreamMarshaller<T> streamMarshaller) {
        Preconditions.checkNotNull(fileName, "fileName");
        Preconditions.checkNotNull(fileMarshaller, "fileMarshaller");
        Preconditions.checkNotNull(streamMarshaller, "streamMarshaller");
        String fileWithPath;
        if (StringUtils.isNotBlank(defaultPath)) {
            fileWithPath = defaultPath + fileName;
        } else {
            fileWithPath = fileName;
        }
        try {
            URL resource = ResourceLocator.getResource(fileWithPath);
            String protocol = resource.getProtocol();
            UrlProtocol urlProtocol = UrlProtocol.fromString(protocol);
            if (null != urlProtocol) {
                switch (urlProtocol) {
                    case FILE:
                        return new ReloadableFileAccessor<T>(fileMarshaller, fileWithPath, resource);
                    default:
                        return new LazyAccessor<T>(new UrlAccessor<T>(streamMarshaller, resource));
                }
            }
            throw new IllegalArgumentException(
                    "\"" + fileWithPath + "\" found at invalid URL: \"" + resource.toExternalForm() + "\"");
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }
    }

    @Nonnull
    public static <T> SingletonAccessor<T> newPropertiesAccessor(@Nonnull String fileName,
                                                                 @Nullable String defaultPath,
                                                                 @Nonnull PropertiesConverter<T> propertiesConverter) {
        Preconditions.checkNotNull(fileName, "fileName");
        Preconditions.checkNotNull(propertiesConverter, "propertiesConverter");
        return newFileAccessor(fileName, defaultPath, new PropertiesBasedFileMarshaller<T>(propertiesConverter),
                               new PropertiesBasedStreamMarshaller<T>(PropertiesFormat.getFormat(fileName),
                                                                      propertiesConverter));
    }

    private SingletonAccessors() {
    }
}
