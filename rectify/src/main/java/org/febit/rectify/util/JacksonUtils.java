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
package org.febit.rectify.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zqq90
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
