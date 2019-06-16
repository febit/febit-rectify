package org.febit.rectify.sqlline;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Source;
import org.febit.rectify.RectifierConf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zqq90
 */
class RectifyTable extends AbstractTable implements ScannableTable {

    protected final Source source;
    protected final RectifierConf conf;
    protected RelDataType rowTypeCaching;

    RectifyTable(Source source, RectifierConf rectifierConf) {
        this.source = source;
        this.conf = rectifierConf;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        RelDataType type = rowTypeCaching;
        if (type != null) {
            return rowTypeCaching;
        }
        type = DataTypeUtils.toDataType(conf.schema(), typeFactory);
        rowTypeCaching = type;
        return type;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
        return new AbstractEnumerable<Object[]>() {
            public Enumerator<Object[]> enumerator() {
                try {
                    return RectifyEnumerator.create(conf, source, cancelFlag);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    @Override
    public String toString() {
        return "RectifyTable";
    }
}
