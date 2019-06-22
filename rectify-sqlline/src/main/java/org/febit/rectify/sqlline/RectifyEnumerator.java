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

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.util.Source;
import org.febit.rectify.Rectifier;
import org.febit.rectify.RectifierConf;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enumerator that reads from a log file.
 *
 * @author zqq90
 */
class RectifyEnumerator implements Enumerator<Object[]> {
    private final BufferedReader reader;
    private final Rectifier<String, Object[]> rectifier;
    private final AtomicBoolean cancelFlag;

    private final Queue<Object[]> peddings;

    private RectifyEnumerator(BufferedReader reader, Rectifier<String, Object[]> rectifier, AtomicBoolean cancelFlag) {
        this.reader = reader;
        this.rectifier = rectifier;
        this.cancelFlag = cancelFlag;
        this.peddings = new ArrayDeque<>();
    }

    static RectifyEnumerator create(RectifierConf conf, Source source, AtomicBoolean cancelFlag) throws IOException {
        BufferedReader reader = new BufferedReader(source.reader());
        Rectifier<String, Object[]> rectifier = Rectifier.create(conf, ObjectArrayResultModel.get());
        return new RectifyEnumerator(reader, rectifier, cancelFlag);
    }

    @Override
    public Object[] current() {
        return peddings.element();
    }

    @Override
    public boolean moveNext() {
        return moveNext(true);
    }

    private boolean moveNext(boolean poll) {
        if (poll) {
            peddings.poll();
        }
        if (!peddings.isEmpty()) {
            return true;
        }
        if (cancelFlag.get()) {
            return false;
        }
        String nextLine;
        try {
            nextLine = reader.readLine();
            if (nextLine == null) {
                // EOF
                reader.close();
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        rectifier.process(nextLine, (out, raw, reason) -> {
            if (out != null) {
                peddings.add(out);
            }
            // TODO: should record errors.
        });
        return moveNext(false);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing reader", e);
        }
    }
}
