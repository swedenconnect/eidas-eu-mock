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
 * limitations under the Licence
 */

package eu.eidas.auth.cache;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.integration.CompletionListenerFuture;
import javax.cache.processor.EntryProcessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test class for {@link JCacheConcurrentMapAdapter} class.
 */
public class JCacheConcurrentMapAdapterTest {

    /**
     * A constant for a key
     */
    private final static String KEY = "key";

    /**
     * A constant for a value
     */
    private final static String VALUE = "value";

    /**
     * A constant for a different value
     */
    private final static String OTHER_VALUE = "other_value";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#get(Object)}
     * when there is one valid key,value pair in the map
     * <p/>
     * Must succeed.
     */
    @Test
    public void get() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        final String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        final String expectedValue = VALUE;
        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAll(Set)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void getAll() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        final Set<String> keySet = createKeySet();
        jCacheConcurrentMapAdapter.getAll(keySet);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#containsKey(Object)}
     * when there is one valid key,value pair in the map.
     * <p/>
     * Must succed.
     */
    @Test
    public void containsKey() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        boolean actual = jCacheConcurrentMapAdapter.containsKey(KEY);
        Assert.assertTrue(actual);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#loadAll(Set, boolean, CompletionListener)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void loadAll() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        Set<String> keySet = createKeySet();
        CompletionListenerFuture completionListenerFuture = new CompletionListenerFuture();
        jCacheConcurrentMapAdapter.loadAll(keySet, false, completionListenerFuture);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#put(Object, Object)}
     * when the map is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void put() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        String expectedValue = VALUE;
        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAndPut(Object, Object)}
     * when the map is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getAndPut() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();

        jCacheConcurrentMapAdapter.getAndPut(KEY, OTHER_VALUE);
        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        String expectedValue = OTHER_VALUE;
        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAndPut(Object, Object)}
     * when the map contains the key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getAndPutWhenMapContainsKeyValue() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, OTHER_VALUE);

        jCacheConcurrentMapAdapter.getAndPut(KEY, OTHER_VALUE);
        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        String expectedValue = OTHER_VALUE;
        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAll(Set)}
     * when the map is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void putAll() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();

        HashMap<String, String> additionalHashMap = new HashMap<>();
        additionalHashMap.put(KEY, VALUE);

        jCacheConcurrentMapAdapter.putAll(additionalHashMap);
        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        String expectedValue = VALUE;
        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#putIfAbsent(Object, Object)}
     * when the map is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void putIfAbsent() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();

        jCacheConcurrentMapAdapter.putIfAbsent(KEY, VALUE);

        String expectedValue = VALUE;
        String actualValue = jCacheConcurrentMapAdapter.get(KEY);

        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#putIfAbsent(Object, Object)}
     * when the map contains the key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void putIfAbsentWhenMapContainsKeyValue() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        jCacheConcurrentMapAdapter.putIfAbsent(KEY, VALUE);

        String expectedValue = VALUE;
        String actualValue = jCacheConcurrentMapAdapter.get(KEY);

        Assert.assertEquals(expectedValue, actualValue);
    }


    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#remove(Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void remove() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);
        boolean containsKeyBeforeRemove = jCacheConcurrentMapAdapter.containsKey(KEY);
        Assert.assertTrue(containsKeyBeforeRemove);

        jCacheConcurrentMapAdapter.remove(KEY);
        boolean containsKeyAfterRemove = jCacheConcurrentMapAdapter.containsKey(KEY);
        Assert.assertFalse(containsKeyAfterRemove);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#remove(Object, Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void removeWithValueAsParameterMethod() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);
        boolean containsKeyBeforeRemove = jCacheConcurrentMapAdapter.containsKey(KEY);
        Assert.assertTrue(containsKeyBeforeRemove);

        jCacheConcurrentMapAdapter.remove(KEY, VALUE);
        boolean containsKeyAfterRemove = jCacheConcurrentMapAdapter.containsKey(KEY);
        Assert.assertFalse(containsKeyAfterRemove);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAndRemove(Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getAndRemove() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);
        boolean containsKeyBeforeRemove = jCacheConcurrentMapAdapter.containsKey(KEY);
        Assert.assertTrue(containsKeyBeforeRemove);

        final String actualRemovedValue = jCacheConcurrentMapAdapter.getAndRemove(KEY);
        final String expectedValue = VALUE;
        Assert.assertEquals(expectedValue, actualRemovedValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAndRemove(Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getAndRemoveWhenMapDoesNotContainKeyValue() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        final String actualRemovedValue = jCacheConcurrentMapAdapter.getAndRemove(KEY);
        Assert.assertNull(actualRemovedValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#replace(Object, Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void replace() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);
        jCacheConcurrentMapAdapter.replace(KEY, OTHER_VALUE);
        final String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        String expectedValue = OTHER_VALUE;
        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#replace(Object, Object)}
     * when the map is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void replaceWhenMapIsEmpty() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        boolean actualReplaced = jCacheConcurrentMapAdapter.replace(KEY, VALUE);
        Assert.assertFalse(actualReplaced);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#replace(Object, Object, Object)}
     * when the map has one valid key,value pair and value to replace do not match .
     * <p/>
     * Must succeed.
     */
    @Test
    public void replaceWith2ValuesWhenValuesDoNotMatch() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);
        final boolean actualReplaced = jCacheConcurrentMapAdapter.replace(KEY, OTHER_VALUE, VALUE);

        Assert.assertFalse(actualReplaced);
    }


    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#replace(Object, Object, Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void replace2ValuesParametersMethod() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);
        final String valueBeforeReplace = jCacheConcurrentMapAdapter.get(KEY);
        Assert.assertEquals(VALUE, valueBeforeReplace);

        jCacheConcurrentMapAdapter.replace(KEY, VALUE, OTHER_VALUE);
        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        Assert.assertEquals(OTHER_VALUE, actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAndReplace(Object, Object)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getAndReplace() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        String actualValueBeforeGetAndReplace = jCacheConcurrentMapAdapter.getAndReplace(KEY, OTHER_VALUE);
        String expectedValueBeforeGetAndReplace = VALUE;
        Assert.assertEquals(expectedValueBeforeGetAndReplace, actualValueBeforeGetAndReplace);

        String actualValueAfterGetAndReplace = jCacheConcurrentMapAdapter.get(KEY);
        String expecteValueAfterGetAndReplace = OTHER_VALUE;
        Assert.assertEquals(expecteValueAfterGetAndReplace, actualValueAfterGetAndReplace);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getAndReplace(Object, Object)}
     * when the map is empty.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getAndReplaceWhenKeyIsNotFoundInMap() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        String actualValue = jCacheConcurrentMapAdapter.getAndReplace(KEY, VALUE);
        Assert.assertNull(actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#removeAll()}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void removeAll() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        jCacheConcurrentMapAdapter.removeAll();

        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        Assert.assertNull(actualValue);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#removeAll(Set)}
     * when the map has one valid key,value pair.
     * <p/>
     * Must succeed.
     */
    @Test
    public void removeAllWith() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        HashSet<String> keySetToRemove = new HashSet<>();
        keySetToRemove.add(KEY);

        jCacheConcurrentMapAdapter.removeAll(keySetToRemove);

        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        Assert.assertNull(actualValue);
    }

    @Test
    public void clear() {
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        jCacheConcurrentMapAdapter.clear();

        String actualValue = jCacheConcurrentMapAdapter.get(KEY);
        Assert.assertNull(actualValue);
    }

    @Test
    public void getConfiguration() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.put(KEY, VALUE);

        final Configuration<String, String> configuration = jCacheConcurrentMapAdapter.getConfiguration(null);

        Assert.assertNull(configuration);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#invoke(Object, EntryProcessor, Object...)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void invoke() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.invoke(null, null, null);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#invokeAll(Set, EntryProcessor, Object...)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void invokeAll() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.invokeAll(null, null, null);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getName()}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void getName() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.getName();
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#getCacheManager()}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */

    @Test
    public void getCacheManager() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.getCacheManager();
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#close()}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void close() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.close();
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#isClosed()}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void isClosed() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.isClosed();
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#unwrap(Class)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void unwrap() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.unwrap(null);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#registerCacheEntryListener(CacheEntryListenerConfiguration)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void registerCacheEntryListener() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.registerCacheEntryListener(null);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#deregisterCacheEntryListener(CacheEntryListenerConfiguration)}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void deregisterCacheEntryListener() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.deregisterCacheEntryListener(null);
    }

    /**
     * Test method for
     * {@link JCacheConcurrentMapAdapter#iterator()}
     * for all cases.
     * <p/>
     * Must fail and throw {@link UnsupportedOperationException}.
     */
    @Test
    public void iterator() {
        thrown.expect(UnsupportedOperationException.class);

        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = createJCacheConcurrentMapAdapter();
        jCacheConcurrentMapAdapter.iterator();
    }

    /**
     * Auxiliary method for creating an empty instance of {@link JCacheConcurrentMapAdapter}
     *
     * @return the instance of {@link JCacheConcurrentMapAdapter}
     */
    private JCacheConcurrentMapAdapter<String, String> createJCacheConcurrentMapAdapter() {
        final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        final JCacheConcurrentMapAdapter<String, String> jCacheConcurrentMapAdapter = new JCacheConcurrentMapAdapter(map);
        return jCacheConcurrentMapAdapter;
    }

    /**
     * Auxiliary method for creating a set with {@link JCacheConcurrentMapAdapterTest#KEY}
     *
     * @return the instance of {@link Set}
     */
    private Set<String> createKeySet() {
        final HashSet<String> keySet = new HashSet<>();
        keySet.add(KEY);

        return keySet;
    }
}