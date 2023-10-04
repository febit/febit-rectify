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
package org.febit.lang.modeler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructSpecsTest {

    final Schema schema = Schema.parseStruct("demo",
            "long id",
            "string name",
            "int level",
            "string color"
    );

    @Test
    void asObjectArray() {
        StructSpec<Object[]> model = StructSpecs.asArray();
        Object[] struct = model.create(schema);

        model.set(struct, schema.field("id"), 12345L);
        model.set(struct, schema.field("name"), "Mr.R");
        model.set(struct, schema.field("color"), "blue");

        assertEquals(12345L, struct[0]);
        assertEquals("Mr.R", struct[1]);
        assertNull(struct[2]);
        assertEquals("blue", struct[3]);

        assertEquals(struct[0],
                model.get(struct, schema.field("id"))
        );
        assertEquals(struct[1],
                model.get(struct, schema.field("name"))
        );
        assertEquals(struct[2],
                model.get(struct, schema.field("level"))
        );
        assertEquals(struct[3],
                model.get(struct, schema.field("color"))
        );
    }
}
