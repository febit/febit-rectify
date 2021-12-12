/*
 * Copyright 2018-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.util;

import org.febit.wit.lang.Bag;

import java.io.Serializable;
import java.util.*;

public class IndexedArrayBag implements Bag, Serializable {

    private static final long serialVersionUID = 1L;

    private final Indexer indexer;
    private final Object[] values;

    private IndexedArrayBag(Indexer indexer, Object[] values) {
        this.indexer = indexer;
        this.values = values == null ? new Object[0] : values;
    }

    public static IndexedArrayBag of(Indexer indexer, Object[] values) {
        return new IndexedArrayBag(indexer, values);
    }

    public static Indexer buildIndexer(Collection<String> keys) {
        return new Indexer(new ArrayList<>(keys));
    }

    private int resolveIndex(Object key) {
        if (key instanceof Number) {
            return ((Number) key).intValue();
        }
        if (key instanceof String) {
            Integer i = this.indexer.getIndex((String) key);
            return i == null ? -1 : i.intValue();
        }
        return -1;
    }

    @Override
    public Object get(Object key) {
        int i = resolveIndex(key);
        Object[] vals = this.values;
        if (i < 0 || i >= vals.length) {
            return null;
        }
        return vals[i];
    }

    @Override
    public void set(Object key, Object value) {
        int i = resolveIndex(key);
        Object[] vals = this.values;
        if (i < 0 || i >= vals.length) {
            throw new NoSuchElementException(String.valueOf(key));
        }
        vals[i] = value;
    }

    public Iterator<String> keys() {
        return this.indexer.iterator();
    }

    public int size() {
        return this.indexer.size();
    }

    public static class Indexer implements Iterable<String>, Serializable {

        private final List<String> keys;
        private final HashMap<String, Integer> indexerMap;

        private Indexer(List<String> keys) {
            this.keys = keys;
            this.indexerMap = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                this.indexerMap.put(keys.get(i), i);
            }
        }

        public int size() {
            return keys.size();
        }

        public Integer getIndex(String key) {
            return indexerMap.get(key);
        }

        @Override
        public Iterator<String> iterator() {
            return keys.iterator();
        }
    }
}
