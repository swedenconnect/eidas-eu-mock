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
package eu.eidas.auth.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.util.Preconditions;

/**
 * Locates resources on the classpath or on the filesystem.
 *
 * @since 1.1
 */
public final class ResourceLocator {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ResourceLocator.class);

    public static URL getResource(@Nonnull final String name) throws IOException {
        Preconditions.checkNotNull(name, "name");
        boolean traceEnabled = LOG.isTraceEnabled();
        URL url = null;
        // 1) try with the ClassLoader
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (null != contextClassLoader) {
            url = contextClassLoader.getResource(name);
            if (traceEnabled) {
                if (null == url) {
                    LOG.trace("Could not find file \"" + name + "\" in Context ClassLoader: " + contextClassLoader);
                } else {
                    LOG.trace("Found file \"" + name + "\" at URL: \"" + url.toExternalForm()
                                      + "\" in Context ClassLoader: " + contextClassLoader);
                }
            }
        }
        ClassLoader classClassLoader = null;
        if (null == url) {
            classClassLoader = ResourceLocator.class.getClassLoader();
            if (null != classClassLoader && classClassLoader != contextClassLoader) {
                url = classClassLoader.getResource(name);
                if (traceEnabled) {
                    if (null == url) {
                        LOG.trace("Could not find file \"" + name + "\" in Class ClassLoader: " + classClassLoader);
                    } else {
                        LOG.trace("Found file \"" + name + "\" at URL: \"" + url.toExternalForm()
                                          + "\" in Class ClassLoader: " + classClassLoader);
                    }
                }
            }
        }
        if (null == url) {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (null != systemClassLoader && systemClassLoader != contextClassLoader
                    && systemClassLoader != classClassLoader) {
                url = systemClassLoader.getResource(name);
                if (traceEnabled) {
                    if (null == url) {
                        LOG.trace("Could not find file \"" + name + "\" in System ClassLoader: " + systemClassLoader);
                    } else {
                        LOG.trace("Found file \"" + name + "\" at URL: \"" + url.toExternalForm()
                                          + "\" in System ClassLoader: " + systemClassLoader);
                    }
                }
            }
        }

        // 2) if the ClassLoader cannot find it, try using the filesystem
        if (null == url) {
            try {
                url = AccessController.doPrivileged(new PrivilegedExceptionAction<URL>() {

                    @Nonnull
                    private URL getFileUrl(@Nonnull String path) throws IOException {
                        File file = new File(path);
                        if (file.exists()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.trace("Found file \"" + path + "\" on the filesystem path: \""
                                                  + file.getCanonicalPath() + "\"");
                            }
                            return file.toURI().toURL();
                        } else {
                            throw new FileNotFoundException(
                                    "File \"" + path + "\" cannot be found from path: \"" + file.getCanonicalPath()
                                            + "\"");
                        }
                    }

                    @Override
                    public URL run() throws IOException {
                        return getFileUrl(name);
                    }
                });
            } catch (PrivilegedActionException pae) {
                LOG.error("", pae);
                throw (IOException) pae.getException();
            }
        }
        return url;
    }

    private ResourceLocator() {
    }
}
