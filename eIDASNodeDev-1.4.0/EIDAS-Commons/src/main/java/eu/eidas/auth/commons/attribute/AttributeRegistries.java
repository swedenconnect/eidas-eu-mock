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
package eu.eidas.auth.commons.attribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

import eu.eidas.util.Preconditions;

/**
 * Static factory methods pertaining to {@link AttributeRegistry}.
 *
 * @since 1.1
 */
public final class AttributeRegistries {

    private static final AttributeRegistry EMPTY = new AttributeRegistry(MemoryAttributeDefinitionDao.EMPTY);

    /**
     * Copies an attribute registry in memory from another existing AttributeRegistry.
     *
     * @param attributeRegistry the attribute registry to copy.
     */
    @Nonnull
    public static AttributeRegistry copyOf(@Nonnull AttributeRegistry attributeRegistry,
                                           @Nullable AttributeRegistry... otherRegistries) {
        Preconditions.checkNotNull(attributeRegistry, "attributeRegistry");
        if (null == otherRegistries || otherRegistries.length <= 0) {
            return of(attributeRegistry.getAttributes());
        }
        ImmutableSortedSet<AttributeDefinition<?>>[] definitions = new ImmutableSortedSet[otherRegistries.length + 1];
        definitions[0] = attributeRegistry.getAttributes();
        for (int i = 0; i < otherRegistries.length; i++) {
            definitions[i + 1] = otherRegistries[i].getAttributes();
        }
        return of(Iterables.concat(definitions));
    }

    /**
     * Returns an empty immutable registry.
     * <p>
     * This convenient method is equivalent to calling:
     * <p>
     * {@code AttributeRegistry empty = AttributeRegistries.of();}
     *
     * @return an empty immutable registry.
     */
    @Nonnull
    public static AttributeRegistry empty() {
        return EMPTY;
    }

    /**
     * Creates an attribute registry based on the given configuration file.
     * <p>
     * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the configuration file.
     */
    @Nonnull
    public static AttributeRegistry fromFile(@Nonnull String fileName) {
        return fromFile(fileName, null);
    }

    /**
     * Creates an attribute registry based on the given configuration file.
     * <p>
     * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the configuration file.
     * @param defaultPath optional path to registry file.
     */
    @Nonnull
    public static AttributeRegistry fromFile(@Nonnull String fileName, @Nullable String defaultPath) {
        Preconditions.checkNotNull(fileName, "fileName");
        return new AttributeRegistry(new FileAttributeDefinitionDao(fileName, defaultPath));
    }

    /**
     * Creates an attribute registry based on the given configuration files.
     * <p>
     * The given files must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the first configuration file.
     * @param fileNames the names of the other configuration files.
     */
    @Nonnull
    public static AttributeRegistry fromFiles(@Nonnull String fileName, @Nullable String defaultPath, @Nonnull String... fileNames) {
        Preconditions.checkNotNull(fileName, "fileName");
        Preconditions.checkNotNull(fileNames, "fileNames");
        return new AttributeRegistry(new FileAttributeDefinitionDao(fileName, defaultPath, fileNames));
    }

    /**
     * Creates an attribute registry based on the given AttributeDefinitions.
     *
     * @param definitions the attribute definitions constituting the registry.
     */
    @Nonnull
    public static AttributeRegistry of(@Nonnull Iterable<AttributeDefinition<?>> definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        MemoryAttributeDefinitionDao attributeDefinitionDao = MemoryAttributeDefinitionDao.copyOf(definitions);
        if (MemoryAttributeDefinitionDao.EMPTY == attributeDefinitionDao) {
            return EMPTY;
        }
        return new AttributeRegistry(attributeDefinitionDao);
    }

    /**
     * Creates an attribute registry based on the given AttributeDefinitions.
     *
     * @param definitions the attribute definitions constituting the registry.
     */
    @Nonnull
    public static AttributeRegistry of(@Nonnull AttributeDefinition<?>... definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        MemoryAttributeDefinitionDao attributeDefinitionDao = MemoryAttributeDefinitionDao.copyOf(definitions);
        if (MemoryAttributeDefinitionDao.EMPTY == attributeDefinitionDao) {
            return EMPTY;
        }
        return new AttributeRegistry(attributeDefinitionDao);
    }

    private AttributeRegistries() {
    }
}
