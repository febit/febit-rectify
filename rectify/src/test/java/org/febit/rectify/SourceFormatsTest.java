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
package org.febit.rectify;

import lombok.val;
import org.febit.rectify.impls.AccessLogSourceFormat;
import org.febit.rectify.impls.DirectSourceFormat;
import org.febit.rectify.impls.JsonSourceFormat;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SourceFormatsTest {

    @Test
    public void testSupports() {
        List<String> supports = SourceFormats.supports();
        assertTrue(supports.contains("direct"));
        assertTrue(supports.contains("json"));
        assertTrue(supports.contains("access"));
    }

    @Test
    public void testCreate() {
        SourceFormat format;

        format = SourceFormats.create("direct", null);
        assertNotNull(format);
        assertTrue(format instanceof DirectSourceFormat);

        format = SourceFormats.create("json", null);
        assertNotNull(format);
        assertTrue(format instanceof JsonSourceFormat);

        val props = new HashMap<String, String>();
        props.put("keys", "");
        format = SourceFormats.create("access", props);
        assertNotNull(format);
        assertTrue(format instanceof AccessLogSourceFormat);
    }
}
