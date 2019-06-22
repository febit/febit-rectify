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
package org.febit.rectify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GenericStructTest {

    final Schema schema = Schema.parseFieldLines("demo", ""
            + "long id\n"
            + "string name\n"
            + "int level\n"
            + "string color\n");
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test() {
        ResultModel<GenericStruct> model = GenericStruct.model();
        GenericStruct struct = model.newStruct(schema);

        model.setField(struct, schema.getField("id"), 12345L);
        model.setField(struct, schema.getField("name"), "Mr.R");
        model.setField(struct, schema.getField("color"), "blue");

        assertEquals(12345L, struct.get(0));
        assertEquals("Mr.R", struct.get(1));
        assertNull(struct.get(2));
        assertEquals("blue", struct.get(3));

        assertEquals(struct.get(0),
                model.getField(struct, schema.getField("id"))
        );
        assertEquals(struct.get(1),
                model.getField(struct, schema.getField("name"))
        );
        assertEquals(struct.get(2),
                model.getField(struct, schema.getField("level"))
        );
        assertEquals(struct.get(3),
                model.getField(struct, schema.getField("color"))
        );

        struct.set(1, "Mr.L");
        struct.set(3, "Red");
        assertEquals("Mr.L", struct.get(1));
        assertEquals("Red", struct.get(3));
    }

    @Test
    public void indexOutOfBoundsException1() {
        expectedException.expect(ArrayIndexOutOfBoundsException.class);
        GenericStruct struct = GenericStruct.of(schema);
        struct.get(-1);
    }

    @Test
    public void indexOutOfBoundsException2() {
        expectedException.expect(ArrayIndexOutOfBoundsException.class);
        GenericStruct struct = GenericStruct.of(schema);
        struct.get(4);
    }
}
