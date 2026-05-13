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
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.febit.rectify.lib.LibraryTestSupport.export;
import static org.febit.rectify.lib.LibraryTestSupport.namespace;
import static org.junit.jupiter.api.Assertions.*;

class UuidLibraryTest {

    private final Lib library = export(UuidLibrary.class);
    private final Lib uuid = namespace(UuidLibrary.class, "UUID");

    @Test
    void exports() {
        assertNotNull(library.get("UUID"));
        assertSame(uuid.get("timeBased"), uuid.get("v1"));
        assertSame(uuid.get("random"), uuid.get("v4"));
        assertSame(uuid.get("timeBasedEpochRandom"), uuid.get("v7"));
    }

    @Test
    void generators() {
        var v1 = (UUID) uuid.call("timeBased");
        var v4 = (UUID) uuid.call("random");
        var v7 = (UUID) uuid.call("timeBasedEpochRandom");

        assertNotNull(v1);
        assertNotNull(v4);
        assertNotNull(v7);

        assertEquals(1, v1.version());
        assertEquals(4, v4.version());
        assertEquals(7, v7.version());
        assertEquals(2, v1.variant());
        assertEquals(2, v4.variant());
        assertEquals(2, v7.variant());

        assertNotEquals(uuid.call("random"), uuid.call("random"));
    }

    @Test
    void fromString() {
        var value = UUID.randomUUID();
        assertEquals(value, uuid.call("fromString", value.toString()));
        assertThrows(IllegalArgumentException.class, () -> uuid.call("fromString", "not-a-uuid"));
    }
}


