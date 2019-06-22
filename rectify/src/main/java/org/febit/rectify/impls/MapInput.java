/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify.impls;

import org.febit.rectify.Input;

import java.util.Iterator;
import java.util.Map;

/**
 * @author zqq90
 */
public class MapInput implements Input {

    private static final long serialVersionUID = 1L;
    private final Map<String, Object> attrs;

    private MapInput(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public static MapInput of(Map<String, Object> attrs) {
        return new MapInput(attrs);
    }

    @Override
    public Object get(String key) {
        return attrs.get(key);
    }

    public Iterator<String> keyIterator() {
        return attrs.keySet().iterator();
    }

    public int size() {
        return attrs.size();
    }
}
