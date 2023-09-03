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

import jakarta.annotation.Nullable;
import org.febit.lang.util.ArraysUtils;
import org.febit.wit.lang.Bag;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IndexedArrayBag implements Bag, Serializable {

    private static final long serialVersionUID = 1L;

    private final Indexer<String> indexer;
    private final Object[] values;

    private IndexedArrayBag(Indexer<String> indexer, @Nullable Object[] values) {
        this.indexer = indexer;
        this.values = values == null ? new Object[0] : values;
    }

    public static IndexedArrayBag of(Indexer<String> indexer, @Nullable Object[] values) {
        return new IndexedArrayBag(indexer, values);
    }

    private int resolveIndex(@Nullable Object key) {
        if (key instanceof Number) {
            return ((Number) key).intValue();
        }
        if (key instanceof String) {
            var i = this.indexer.getIndex((String) key);
            return i == null ? -1 : i;
        }
        return -1;
    }

    @Nullable
    @Override
    public Object get(@Nullable Object key) {
        return ArraysUtils.get(values,
                resolveIndex(key)
        );
    }

    @Override
    public void set(@Nullable Object key, @Nullable Object value) {
        int i = resolveIndex(key);
        var vars = this.values;
        if (i < 0 || i >= vars.length) {
            throw new NoSuchElementException(String.valueOf(key));
        }
        vars[i] = value;
    }

    public Iterator<String> keys() {
        return this.indexer.iterator();
    }

    public int size() {
        return this.indexer.size();
    }

}
