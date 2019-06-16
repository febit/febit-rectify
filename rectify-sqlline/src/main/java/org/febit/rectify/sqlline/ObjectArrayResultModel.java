package org.febit.rectify.sqlline;

import org.febit.rectify.ResultModel;
import org.febit.rectify.Schema;

/**
 * @author zqq90
 */
class ObjectArrayResultModel implements ResultModel<Object[]> {

    private static final ObjectArrayResultModel INSTANCE = new ObjectArrayResultModel();

    static ObjectArrayResultModel get() {
        return INSTANCE;
    }

    @Override
    public Object[] newStruct(Schema schema) {
        return new Object[schema.fieldSize()];
    }

    @Override
    public void setField(Object[] record, Schema.Field field, Object val) {
        record[field.pos()] = val;
    }

    @Override
    public Object getField(Object[] record, Schema.Field field) {
        return record[field.pos()];
    }
}
