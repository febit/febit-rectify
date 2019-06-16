package org.febit.rectify.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JacksonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final TypeFactory TYPE_FACTORY = MAPPER.getTypeFactory();

    private static final JavaType TYPE_NAMED_MAP = TYPE_FACTORY
            .constructMapType(LinkedHashMap.class, String.class, Object.class);

    public static String toJsonString(Object data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parse(String json, JavaType type) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parse(String json, Class<T> type) {
        return parse(json, TYPE_FACTORY.constructType(type));
    }

    public static Map<String, Object> parseAsNamedMap(String json) {
        return parse(json, TYPE_NAMED_MAP);
    }

}
