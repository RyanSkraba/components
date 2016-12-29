package org.talend.components.kafka.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.coders.ByteCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.StandardCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;

import com.fasterxml.jackson.annotation.JsonCreator;

public class KafkaIndexedRecordCoder extends StandardCoder<KafkaIndexedRecord> {

    private static final KafkaIndexedRecordCoder INSTANCE = new KafkaIndexedRecordCoder();

    private static final StringUtf8Coder stringCoder = StringUtf8Coder.of();

    private static final ByteArrayCoder byteArrayCoder = ByteArrayCoder.of();

    private static final ByteCoder byteCoder = ByteCoder.of();

    @JsonCreator
    public static KafkaIndexedRecordCoder of() {
        return INSTANCE;
    }

    @Override
    public void encode(KafkaIndexedRecord value, OutputStream outStream, Context context) throws CoderException, IOException {
        Context nested = context.nested();
        stringCoder.encode(value.getSchema().toString(), outStream, nested);
        byteArrayCoder.encode(value.getKey(), outStream, nested);
        byteArrayCoder.encode(value.getValue(), outStream, nested);
        byteCoder.encode((byte) (value.isUseAvro() ? 1 : 0), outStream, nested);
        if (value.isUseAvro()) {
            byteCoder.encode((byte) (value.isSimpleAvro() ? 1 : 0), outStream, nested);
            stringCoder.encode(value.getValueSchema().toString(), outStream, nested);
        }
    }

    @Override
    public KafkaIndexedRecord decode(InputStream inStream, Context context) throws CoderException, IOException {
        Context nested = context.nested();
        Schema schema = new Schema.Parser().parse(stringCoder.decode(inStream, nested));
        byte[] key = byteArrayCoder.decode(inStream, nested);
        byte[] value = byteArrayCoder.decode(inStream, nested);
        boolean useAvro = byteCoder.decode(inStream, nested) == (byte) 1;
        boolean simpleAvro = false;
        Schema valueSchema = null;
        if (useAvro) {
            simpleAvro = byteCoder.decode(inStream, nested) == (byte) 1;
            valueSchema = new Schema.Parser().parse(stringCoder.decode(inStream, nested));
        }
        return new KafkaIndexedRecord(schema, key, value, useAvro, simpleAvro, valueSchema);
    }

    @Override
    public List<? extends Coder<?>> getCoderArguments() {
        return null; // no argument so return null
    }

    @Override
    public void verifyDeterministic() throws NonDeterministicException {
        // no argument so no need for this method
    }
}
