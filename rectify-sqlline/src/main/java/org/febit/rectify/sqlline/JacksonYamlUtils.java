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
package org.febit.rectify.sqlline;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
