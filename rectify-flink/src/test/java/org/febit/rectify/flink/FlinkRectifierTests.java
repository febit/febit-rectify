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
package org.febit.rectify.flink;

import org.febit.lang.util.JacksonUtils;
import org.febit.rectify.RectifierConf;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class FlinkRectifierTests {

    static final RectifierConf CONF = RectifierConf.create()
            .name("Demo")
            .frontFilter("$.status > 0")
            .frontFilter("$.status < 100 || \"status should <100\"")
            .column("long", "id", "$.id")
            .column("boolean", "enable", "", "$$ || \"enable is falsely\"")
            .column("int", "status", "$.status")
            .column("string", "content", "\"prefix:\"+$.content");

    static final List<String> SOURCE = Arrays.asList(
            buildInput(6, true, 6, "666"),
            buildInput(5, true, 50, "5555"),
            // not pass: status <= 0
            buildInput(1, true, 0, "First"),
            buildInput(2, true, 20, "Second"),
            // not pass: enable is false
            buildInput(3, false, 20, "third"),
            buildInput(4, true, 40, "fourth")
    );

    private static String buildInput(
            Object id,
            Object enable,
            Object status,
            Object content) {
        return JacksonUtils.jsonify(Map.of(
                "id", id,
                "enable", enable,
                "status", status,
                "content", content
        ));
    }
}
