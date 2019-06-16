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
