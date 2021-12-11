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

import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.File;
import java.util.Map;

/**
 * Factory that creates a {@link RectifySchema}.
 */
@SuppressWarnings("UnusedDeclaration")
public class RectifySchemaFactory implements SchemaFactory {

    public static final RectifySchemaFactory INSTANCE = new RectifySchemaFactory();

    private RectifySchemaFactory() {
        // Nothing need to do.
    }

    @Override
    public Schema create(SchemaPlus parentSchema, String name,
                         Map<String, Object> operand) {
        final File base =
                (File) operand.get(ModelHandler.ExtraOperand.BASE_DIRECTORY.camelName);
        File dir = new File(base, (String) operand.get("directory"));
        return new RectifySchema(dir);
    }
}
