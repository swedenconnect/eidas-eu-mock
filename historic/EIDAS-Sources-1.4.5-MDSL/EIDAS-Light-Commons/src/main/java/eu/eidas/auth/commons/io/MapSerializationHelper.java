/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */
package eu.eidas.auth.commons.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

/**
 * Serialization helper to serialize Maps in an implementation agnostic manner.
 * <p/>
 * Whatever the internal serial form of the Map implementation, it does not have an impact.
 *
 * @since 1.1
 */
public final class MapSerializationHelper {

    public static final class KeyValue<K, V> {

        private final K key;

        private final V value;

        public KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    private MapSerializationHelper() {
    }

    public static <K, V> ImmutableList<KeyValue<K, V>> asKeyValueList(Map<K, V> map) {
        Set<Map.Entry<K, V>> entrySet = map.entrySet();
        ImmutableList.Builder<KeyValue<K, V>> keyValueViewBuilder = new ImmutableList.Builder<KeyValue<K, V>>();
        for (final Map.Entry<K, V> entry : entrySet) {
            keyValueViewBuilder.add(new KeyValue<K, V>(entry.getKey(), entry.getValue()));
        }
        return keyValueViewBuilder.build();
    }

    @SuppressWarnings({"unchecked", "findsecbugs:OBJECT_DESERIALIZATION"})
    public static <K, V> void readMap(@Nonnull ObjectInputStream in, @Nonnull Map<K, V> map)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            K key = (K) in.readObject();
            V value = (V) in.readObject();
            map.put(key, value);
        }
    }

    public static <K, V> void writeKeyValueList(@Nonnull ObjectOutputStream oos,
                                                @Nonnull ImmutableList<KeyValue<K, V>> keyValueList)
            throws IOException {
        int kvSize = keyValueList.size();
        oos.writeInt(kvSize);
        for (int i = 0; i < kvSize; i++) {
            KeyValue<K, V> keyValue = keyValueList.get(i);
            oos.writeObject(keyValue.getKey());
            oos.writeObject(keyValue.getValue());
        }
    }

    public static <K, V> void writeMap(@Nonnull ObjectOutputStream oos, @Nonnull Map<K, V> map) throws IOException {
        // First, prepare a view in memory:
        ImmutableList<KeyValue<K, V>> keyValueView = asKeyValueList(map);
        // Then write the view to the stream:
        writeKeyValueList(oos, keyValueView);
    }

}
