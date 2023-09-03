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
import org.apache.commons.lang3.tuple.Pair;
import org.febit.lang.util.Pairs;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Indexer<T extends Serializable> implements Iterable<T>, Serializable {

    private final List<T> keys;
    private final Map<T, Integer> indexerMap;

    private Indexer(List<T> keys) {
        this.keys = keys;
        var pairs = Pairs.<T, Integer>newArray(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            pairs[i] = Pair.of(keys.get(i), i);
        }
        this.indexerMap = Map.ofEntries(pairs);
    }

    public static <T extends Serializable> Indexer<T> of(Collection<T> keys) {
        return new Indexer<>(List.copyOf(keys));
    }

    public int size() {
        return keys.size();
    }

    @Nullable
    public Integer getIndex(T key) {
        return indexerMap.get(key);
    }

    @Override
    public Iterator<T> iterator() {
        return keys.iterator();
    }
}
