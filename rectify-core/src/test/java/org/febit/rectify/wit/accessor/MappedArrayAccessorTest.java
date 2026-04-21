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
package org.febit.rectify.wit.accessor;

import org.febit.rectify.support.MappedArray;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MappedArrayAccessorTest {

    @Test
    void test() {
        var accessor = new MappedArrayAccessor();
        var array = mock(MappedArray.class);

        accessor.set(array, "key1", "value1");
        verify(array).set("key1", "value1");

        accessor.get(array, "key1");
        verify(array).get("key1");
    }

}
