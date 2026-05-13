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
package org.febit.rectify.flink.table.factory;

import lombok.RequiredArgsConstructor;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.Row;
import org.febit.rectify.flink.support.ProjectUtils;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

@RequiredArgsConstructor(staticName = "of")
public class RowDataProjectConverter implements RowDataConverter {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DynamicTableSource.DataStructureConverter converter;
    private final int[][] projections;

    @Override
    public @Nullable RowData convert(@Nullable Row value) {
        if (value == null) {
            return null;
        }
        var projected = ProjectUtils.project(value, projections);
        return (RowData) converter.toInternal(projected);
    }
}
