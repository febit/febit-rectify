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
package org.febit.rectify.flink;

import org.febit.rectify.RectifierConf;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author zqq90
 */
public class StringDeserializationSchema extends BaseDeserializationSchema<String> {

    private static final long serialVersionUID = 1L;

    private transient Charset charset;

    public StringDeserializationSchema(RectifierConf conf) {
        this(conf, StandardCharsets.UTF_8);
    }

    public StringDeserializationSchema(RectifierConf conf, Charset charset) {
        super(conf);
        Objects.requireNonNull(charset);
        this.charset = charset;
    }

    @Override
    protected String deserializeRaw(byte[] message) {
        return new String(message, charset);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(charset.name());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String charsetName = in.readUTF();
        this.charset = Charset.forName(charsetName);
    }
}
