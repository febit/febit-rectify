/**
 * Copyright 2018-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.impls;

import org.febit.lang.Defaults;
import org.febit.rectify.Input;
import org.febit.util.CollectionUtil;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author zqq90
 */
public class IndexedArrayInput implements Input {

    private static final long serialVersionUID = 1L;

    private final Indexer indexer;
    private final Object[] values;

    public static IndexedArrayInput of(Indexer indexer, Object[] values) {
        return new IndexedArrayInput(indexer, values);
    }

    public static Indexer buildIndexer(String... keys) {
        return new Indexer(keys);
    }

    private IndexedArrayInput(Indexer indexer, Object[] values) {
        this.indexer = indexer;
        this.values = values == null ? Defaults.EMPTY_OBJECTS : values;
    }

    @Override
    public Object get(String key) {
        Integer index = this.indexer.getIndex(key);
        Object[] vals = this.values;
        if (index == null
                || index >= vals.length) {
            return null;
        }
        return vals[index];
    }

    public Iterator<String> keyIterator() {
        return this.indexer.iterator();
    }

    public int size() {
        return this.indexer.size();
    }

    public static class Indexer implements Iterable<String> {

        private final String[] keys;
        private final HashMap<String, Integer> indexerMap;

        private Indexer(String[] keys) {
            this.keys = keys;
            this.indexerMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                this.indexerMap.put(keys[i], i);
            }
        }

        public int size() {
            return keys.length;
        }

        public Integer getIndex(String key) {
            return indexerMap.get(key);
        }

        @Override
        public Iterator<String> iterator() {
            return CollectionUtil.toIter(keys);
        }
    }
}
