package org.febit.rectify.sqlline;

import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.File;
import java.util.Map;

/**
 * Factory that creates a {@link RectifySchema}.
 *
 * @author zqq90
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
