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

import com.fasterxml.jackson.databind.JavaType;
import org.febit.lang.func.Function1;
import org.febit.rectify.lib.IFunctions;
import org.febit.rectify.lib.IProto;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;
import static org.febit.lang.util.JacksonUtils.json;
import static org.febit.lang.util.JacksonUtils.prettyJson;
import static org.febit.lang.util.JacksonUtils.yaml;

@SuppressWarnings({"unused"})
public class JacksonFunctions implements IFunctions {

    private static final JavaType T_OBJECT = TYPE_FACTORY.constructType(Object.class);
    private static final JavaType TYPE_LIST = TYPE_FACTORY
            .constructCollectionLikeType(ArrayList.class, Object.class);
    private static final JavaType TYPE_MAP = TYPE_FACTORY
            .constructMapType(LinkedHashMap.class, Object.class, Object.class);

    /**
     * Namespace: JSON.
     */
    @Alias(value = {"JSON"}, keepOriginName = false)
    public static final JsonProto JSON = new JsonProto();
    /**
     * Namespace: YAML.
     */
    @Alias(value = {"YAML"}, keepOriginName = false)
    public static final YamlProto YAML = new YamlProto();

    public static class JsonProto implements IProto {

        public final Function1<Object, String> toString = json()::toString;
        public final Function1<Object, String> toPrettyString = prettyJson()::toString;

        public final Function1<Object, Object> toMap = json()::toMap;
        public final Function1<Object, Object> toList = json()::toList;

        public final Function1<String, Object> parse = text -> parseIfPresent(text, T_OBJECT);
        public final Function1<String, Object> parseAsMap = text -> parseIfPresent(text, TYPE_MAP);
        public final Function1<String, Object> parseAsList = text -> parseIfPresent(text, TYPE_LIST);

        private static Object parseIfPresent(String text, JavaType type) {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return json().parse(text, type);
        }

        private static String toPrettyJsonString(Object obj) {
            return prettyJson().toString(obj);
        }
    }

    public static class YamlProto implements IProto {

        public final Function1<Object, String> toString = yaml()::toString;

        public final Function1<Object, Object> toMap = yaml()::toMap;
        public final Function1<Object, Object> toList = yaml()::toList;

        public final Function1<String, Object> parse = text -> parseIfPresent(text, T_OBJECT);
        public final Function1<String, Object> parseAsMap = text -> parseIfPresent(text, TYPE_MAP);
        public final Function1<String, Object> parseAsList = text -> parseIfPresent(text, TYPE_LIST);

        private static Object parseIfPresent(String text, JavaType type) {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return yaml().parse(text, type);
        }
    }

}
