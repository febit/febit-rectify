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

import org.febit.rectify.lib.Lib;
import org.febit.rectify.lib.LibraryTestSupport;
import org.febit.rectify.wit.function.LibFunction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlLibraryTest {

    final Lib yaml = LibraryTestSupport.namespace(YamlLibrary.class, "YAML");

    @Test
    void basic() {
        List.of(
                        "stringify",
                        "toMap",
                        "toList",
                        "parse",
                        "parseAsMap",
                        "parseAsList"
                )
                .forEach(name ->
                        assertDoesNotThrow(() -> yaml.function(name))
                );

    }

    @Test
    void yaml() {
        LibFunction func;

        var map = Map.of("id", 123);
        var mapString = """
                ---
                id: 123
                """;

        var list = List.of("id", 123);
        var listString = """
                - id
                - 123
                """;

        func = yaml.function("stringify");
        assertEquals(mapString, func.apply(map));

        func = yaml.function("toMap");
        assertEquals(map, func.apply(map));

        func = yaml.function("toList");
        assertEquals(list, func.apply(list));

        func = yaml.function("parse");
        assertNull(func.apply());
        assertNull(func.apply(""));
        assertNull(func.apply((Object) null));
        assertEquals(map, func.apply(mapString));
        assertEquals(list, func.apply(listString));

        func = yaml.function("parseAsMap");
        assertEquals(map, func.apply(mapString));

        func = yaml.function("parseAsList");
        assertEquals(list, func.apply(listString));
    }
}
