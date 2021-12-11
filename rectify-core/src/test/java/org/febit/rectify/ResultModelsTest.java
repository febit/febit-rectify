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
package org.febit.rectify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultModelsTest {

    final Schema schema = Schema.parseFieldLines("demo", ""
            + "long id\n"
            + "string name\n"
            + "int level\n"
            + "string color\n");

    @Test
    void asObjectArray() {
        ResultModel<Object[]> model = ResultModels.asObjectArray();
        Object[] struct = model.newStruct(schema);

        model.setField(struct, schema.getField("id"), 12345L);
        model.setField(struct, schema.getField("name"), "Mr.R");
        model.setField(struct, schema.getField("color"), "blue");

        assertEquals(12345L, struct[0]);
        assertEquals("Mr.R", struct[1]);
        assertNull(struct[2]);
        assertEquals("blue", struct[3]);

        assertEquals(struct[0],
                model.getField(struct, schema.getField("id"))
        );
        assertEquals(struct[1],
                model.getField(struct, schema.getField("name"))
        );
        assertEquals(struct[2],
                model.getField(struct, schema.getField("level"))
        );
        assertEquals(struct[3],
                model.getField(struct, schema.getField("color"))
        );
    }
}
