package org.febit.rectify.sqlline;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

class JacksonYamlUtils {

    private static final YAMLMapper MAPPER = new YAMLMapper();
    private static final TypeFactory TYPE_FACTORY = MAPPER.getTypeFactory();

    public static <T> T parse(String yaml, JavaType type) {
        if (yaml == null) {
            return null;
        }
        try {
            return MAPPER.readValue(yaml, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parse(Reader reader, JavaType type) {
        if (reader == null) {
            return null;
        }
        try {
            return MAPPER.readValue(reader, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parse(Reader reader, Class<T> type) {
        return parse(reader, TYPE_FACTORY.constructType(type));
    }

    public static <T> T parse(String yaml, Class<T> type) {
        return parse(yaml, TYPE_FACTORY.constructType(type));
    }

}
