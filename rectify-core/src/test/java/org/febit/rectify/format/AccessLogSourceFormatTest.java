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
package org.febit.rectify.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccessLogSourceFormatTest {

    @Test
    public void test() {
        String log = "100.001.002.003 - - [2/Aug/2018:01:02:03 +0800] \"PUT /path/to/action HTTP/1.1\" 200 111 \"-\" \"HttpClient\" 0.011"
                + " \"test.febit.org\" [-some thing very bad \"\" {{}{}}\"\"]";
        String[] values = AccessLogSourceFormat.parse(log);

        assertEquals("100.001.002.003", values[0]);
        assertNull(values[1]);
        assertNull(values[2]);
        assertEquals("2/Aug/2018:01:02:03 +0800", values[3]);
        assertEquals("PUT /path/to/action HTTP/1.1", values[4]);
        assertEquals("200", values[5]);
        assertNull(values[7]);
        assertEquals("0.011", values[9]);
        assertEquals("test.febit.org", values[10]);
        assertEquals("-some thing very bad \"\" {{}{}}\"\"", values[11]);
    }
}
