package org.febit.rectify.lib.extra;

import org.febit.rectify.util.FuncMethodDeclare;
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

        assertTrue(map.get("JSON") instanceof Map);
        assertTrue(map.get("YAML") instanceof Map);

        json = (Map<Object, Object>) map.get("JSON");
        yaml = (Map<Object, Object>) map.get("YAML");
    }

    @Test
    void basic() {
        assertTrue(json.get("toString") instanceof FuncMethodDeclare);
        assertTrue(json.get("toPrettyString") instanceof FuncMethodDeclare);
        assertTrue(json.get("toMap") instanceof FuncMethodDeclare);
        assertTrue(json.get("toList") instanceof FuncMethodDeclare);
        assertTrue(json.get("parse") instanceof FuncMethodDeclare);
        assertTrue(json.get("parseAsMap") instanceof FuncMethodDeclare);
        assertTrue(json.get("parseAsList") instanceof FuncMethodDeclare);

        assertTrue(yaml.get("toString") instanceof FuncMethodDeclare);
        assertTrue(yaml.get("toMap") instanceof FuncMethodDeclare);
        assertTrue(yaml.get("toList") instanceof FuncMethodDeclare);
        assertTrue(yaml.get("parse") instanceof FuncMethodDeclare);
        assertTrue(yaml.get("parseAsMap") instanceof FuncMethodDeclare);
        assertTrue(yaml.get("parseAsList") instanceof FuncMethodDeclare);
    }

    @Test
    void json() {
        FuncMethodDeclare method;

        var map = Map.of("id", 123);
        var mapString = "{\"id\":123}";
        var mapPrettyString = "{\n  \"id\": 123\n}";

        var list = List.of("id", 123);
        var listString = "[\"id\",123]";
        var listPrettyString = "[\n  \"id\",\n  123\n]";

        method = (FuncMethodDeclare) json.get("toString");
        assertEquals(mapString, method.apply(map));

        method = (FuncMethodDeclare) json.get("toPrettyString");
        assertEquals(mapPrettyString, method.apply(map));
        assertEquals(listPrettyString, method.apply(list));

        method = (FuncMethodDeclare) json.get("toMap");
        assertEquals(map, method.apply(map));

        method = (FuncMethodDeclare) json.get("toList");
        assertEquals(list, method.apply(list));

        method = (FuncMethodDeclare) json.get("parse");
        assertEquals(map, method.apply(mapString));
        assertEquals(list, method.apply(listString));

        method = (FuncMethodDeclare) json.get("parseAsMap");
        assertEquals(map, method.apply(mapString));

        method = (FuncMethodDeclare) json.get("parseAsList");
        assertEquals(list, method.apply(listString));
    }

    @Test
    void yaml() {
        FuncMethodDeclare method;

        var map = Map.of("id", 123);
        var mapString = "---\nid: 123\n";

        var list = List.of("id", 123);
        var listString = "- id\n- 123\n";

        method = (FuncMethodDeclare) yaml.get("toString");
        assertEquals(mapString, method.apply(map));

        method = (FuncMethodDeclare) yaml.get("toMap");
        assertEquals(map, method.apply(map));

        method = (FuncMethodDeclare) yaml.get("toList");
        assertEquals(list, method.apply(list));

        method = (FuncMethodDeclare) yaml.get("parse");
        assertEquals(map, method.apply(mapString));
        assertEquals(list, method.apply(listString));

        method = (FuncMethodDeclare) yaml.get("parseAsMap");
        assertEquals(map, method.apply(mapString));

        method = (FuncMethodDeclare) yaml.get("parseAsList");
        assertEquals(list, method.apply(listString));
    }

}
