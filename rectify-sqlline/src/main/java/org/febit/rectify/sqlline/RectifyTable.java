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
import org.febit.rectify.SourceFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

class RectifyTable extends AbstractTable implements ScannableTable {

    protected final Source source;
    protected final RectifierConf conf;
    protected final SourceFormat<String, Object> sourceFormat;
    protected RelDataType rowTypeCaching;

    RectifyTable(Source source, RectifierConf rectifierConf, SourceFormat<String, Object> sourceFormat) {
        this.source = source;
        this.conf = rectifierConf;
        this.sourceFormat = sourceFormat;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        var type = rowTypeCaching;
        if (type != null) {
            return rowTypeCaching;
        }
        type = DataTypeUtils.toDataType(conf.resolveSchema(), typeFactory);
        rowTypeCaching = type;
        return type;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
        return new AbstractEnumerable<>() {
            public Enumerator<Object[]> enumerator() {
                try {
                    return RectifyEnumerator.create(conf, source, sourceFormat, cancelFlag);
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
