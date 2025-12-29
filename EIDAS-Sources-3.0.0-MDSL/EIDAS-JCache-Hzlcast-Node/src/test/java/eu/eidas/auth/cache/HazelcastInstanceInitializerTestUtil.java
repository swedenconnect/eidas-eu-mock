/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.auth.cache;

import com.hazelcast.core.HazelcastInstance;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;

/**
 * Util class with methods to be used by the tests in other Junit classes.
 */
public class HazelcastInstanceInitializerTestUtil {

    private HazelcastInstanceInitializerTestUtil() {
    }

    /**
     * Method to create, configure and initialize an instance of {@link HazelcastInstanceInitializerNode}.
     *
     * @param hazelcastConfigFileName the {@link HazelcastInstanceInitializerNode} configurations filename
     * @return a new configured instance of {@link HazelcastInstanceInitializerNode}
     * @throws FileNotFoundException when the file from {@param hazelcastConfigFileName} is not found
     */
    @Nonnull
    public static HazelcastInstanceInitializerNode createHazelcastInstanceInitializer(final String hazelcastConfigFileName) throws FileNotFoundException {
        HazelcastInstanceInitializerNode hazelcastInstanceInitializerNode = new HazelcastInstanceInitializerNode();
        hazelcastInstanceInitializerNode.setHazelcastConfigfileName(hazelcastConfigFileName);
        hazelcastInstanceInitializerNode.initializeInstance();
        return hazelcastInstanceInitializerNode;
    }

    /**
     * Method to shutdown an instance of {@link HazelcastInstanceInitializerNode}.
     *
     * @param actualHazelcastInstance the instance to shutdown.
     */
    public static void shutdownHazelcastInstance(@Nonnull final HazelcastInstance actualHazelcastInstance) {
        actualHazelcastInstance.shutdown();
    }

}
