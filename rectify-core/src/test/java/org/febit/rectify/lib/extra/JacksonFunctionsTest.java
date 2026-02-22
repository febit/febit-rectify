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
package org.febit.rectify.lib.extra;

import org.febit.rectify.util.FuncFunctionDeclare;
import org.febit.rectify.util.FuncUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class JacksonFunctionsTest {

    final Map<Object, Object> json;
    final Map<Object, Object> yaml;

    {
        var map = new HashMap<String, Object>();
        FuncUtils.scanConstFields(JacksonFunctions.class, map::put);

        assertInstanceOf(Map.class, map.get("JSON"));
        assertInstanceOf(Map.class, map.get("YAML"));

        json = (Map<Object, Object>) map.get("JSON");
        yaml = (Map<Object, Object>) map.get("YAML");
    }

    @Test
    void basic() {
        assertInstanceOf(FuncFunctionDeclare.class, json.get("stringify"));
        assertInstanceOf(FuncFunctionDeclare.class, json.get("prettyStringify"));
        assertInstanceOf(FuncFunctionDeclare.class, json.get("toMap"));
        assertInstanceOf(FuncFunctionDeclare.class, json.get("toList"));
        assertInstanceOf(FuncFunctionDeclare.class, json.get("parse"));
        assertInstanceOf(FuncFunctionDeclare.class, json.get("parseAsMap"));
        assertInstanceOf(FuncFunctionDeclare.class, json.get("parseAsList"));

        assertInstanceOf(FuncFunctionDeclare.class, yaml.get("stringify"));
        assertInstanceOf(FuncFunctionDeclare.class, yaml.get("toMap"));
        assertInstanceOf(FuncFunctionDeclare.class, yaml.get("toList"));
        assertInstanceOf(FuncFunctionDeclare.class, yaml.get("parse"));
        assertInstanceOf(FuncFunctionDeclare.class, yaml.get("parseAsMap"));
        assertInstanceOf(FuncFunctionDeclare.class, yaml.get("parseAsList"));
    }

    @Test
    void json() {
        FuncFunctionDeclare method;

        var map = Map.of("id", 123);
        var mapString = "{\"id\":123}";
        var mapPrettyString = "{\n  \"id\": 123\n}";

        var list = List.of("id", 123);
        var listString = "[\"id\",123]";
        var listPrettyString = "[\n  \"id\",\n  123\n]";

        method = (FuncFunctionDeclare) json.get("stringify");
        assertEquals(mapString, method.apply(map));

        method = (FuncFunctionDeclare) json.get("prettyStringify");
        assertEquals(mapPrettyString, method.apply(map));
        assertEquals(listPrettyString, method.apply(list));

        method = (FuncFunctionDeclare) json.get("toMap");
        assertEquals(map, method.apply(map));

        method = (FuncFunctionDeclare) json.get("toList");
        assertEquals(list, method.apply(list));

        method = (FuncFunctionDeclare) json.get("parse");
        assertEquals(map, method.apply(mapString));
        assertEquals(list, method.apply(listString));

        method = (FuncFunctionDeclare) json.get("parseAsMap");
        assertEquals(map, method.apply(mapString));

        method = (FuncFunctionDeclare) json.get("parseAsList");
        assertEquals(list, method.apply(listString));
    }

    @Test
    void yaml() {
        FuncFunctionDeclare method;

        var map = Map.of("id", 123);
        var mapString = "---\nid: 123\n";

        var list = List.of("id", 123);
        var listString = "- id\n- 123\n";

        method = (FuncFunctionDeclare) yaml.get("stringify");
        assertEquals(mapString, method.apply(map));

        method = (FuncFunctionDeclare) yaml.get("toMap");
        assertEquals(map, method.apply(map));

        method = (FuncFunctionDeclare) yaml.get("toList");
        assertEquals(list, method.apply(list));

        method = (FuncFunctionDeclare) yaml.get("parse");
        assertEquals(map, method.apply(mapString));
        assertEquals(list, method.apply(listString));

        method = (FuncFunctionDeclare) yaml.get("parseAsMap");
        assertEquals(map, method.apply(mapString));

        method = (FuncFunctionDeclare) yaml.get("parseAsList");
        assertEquals(list, method.apply(listString));
    }

}
