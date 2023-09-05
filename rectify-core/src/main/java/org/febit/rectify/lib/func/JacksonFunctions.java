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
package org.febit.rectify.lib.func;

import com.fasterxml.jackson.databind.JavaType;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;
import org.febit.rectify.function.IFunctions;
import org.febit.rectify.function.ObjFunc;
import org.febit.rectify.function.StrFunc;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;

@SuppressWarnings({"unused"})
public class JacksonFunctions implements IFunctions {

    private static final JavaType T_OBJECT = TYPE_FACTORY.constructType(Object.class);
    private static final JavaType TYPE_LIST = TYPE_FACTORY.constructCollectionLikeType(ArrayList.class, Object.class);
    private static final JavaType TYPE_MAP = TYPE_FACTORY.constructMapType(LinkedHashMap.class, Object.class, Object.class);

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

    public static class JsonProto {
        private static final JacksonWrapper JACKSON = JacksonUtils.json();
        private static final JacksonWrapper PRETTY_JSON = JacksonUtils.prettyJson();

        public final ObjFunc toString = JACKSON::toString;
        public final ObjFunc toPrettyString = PRETTY_JSON::toString;

        public final ObjFunc toMap = JACKSON::toMap;
        public final ObjFunc toList = JACKSON::toList;

        public final StrFunc parse = text -> parseIfPresent(text, T_OBJECT);
        public final StrFunc parseAsMap = text -> parseIfPresent(text, TYPE_MAP);
        public final StrFunc parseAsList = text -> parseIfPresent(text, TYPE_LIST);

        private static Object parseIfPresent(String text, JavaType type) {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return JACKSON.parse(text, type);
        }

        private static String toPrettyJsonString(Object obj) {
            return JacksonUtils.prettyJson().toString(obj);
        }
    }

    public static class YamlProto {
        private static final JacksonWrapper JACKSON = JacksonUtils.yaml();

        public final ObjFunc toString = JACKSON::toString;

        public final ObjFunc toMap = JACKSON::toMap;
        public final ObjFunc toList = JACKSON::toList;

        public final StrFunc parse = text -> parseIfPresent(text, T_OBJECT);
        public final StrFunc parseAsMap = text -> parseIfPresent(text, TYPE_MAP);
        public final StrFunc parseAsList = text -> parseIfPresent(text, TYPE_LIST);

        private static Object parseIfPresent(String text, JavaType type) {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return JACKSON.parse(text, type);
        }
    }

}
