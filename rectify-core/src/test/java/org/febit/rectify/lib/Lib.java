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
package org.febit.rectify.lib;

import org.febit.rectify.wit.function.LibFunction;

import java.util.Map;

public record Lib(Map<String, Object> members) {

    public boolean has(String name) {
        return members.containsKey(name);
    }

    public Object get(String name) {
        var member = members.get(name);
        if (member == null) {
            throw new IllegalArgumentException("No such member: " + name);
        }
        return member;
    }

    public LibFunction function(String name) {
        var member = get(name);
        if (!(member instanceof LibFunction f)) {
            throw new IllegalArgumentException("Member is not a function: " + name);
        }
        return f;
    }

    @SuppressWarnings("unchecked")
    public Lib namespace(String name) {
        var member = get(name);
        if (!(member instanceof Map<?, ?> m)) {
            throw new IllegalArgumentException("Member is not a namespace: " + name);
        }
        return new Lib((Map<String, Object>) m);
    }

}
