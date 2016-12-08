package org.talend.components.kafka.runtime;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.talend.components.api.exception.ComponentException;

public class KafkaIndexedRecord implements IndexedRecord, Comparable<IndexedRecord> {

    // schema for KafkaIndexedRecord
    private final Schema schema;

    private final byte[] key;

    private final byte[] value;

    private final boolean useAvro;

    private final boolean simpleAvro;

    // schema for avro value if need, or empty
    private final Schema valueSchema;

    public KafkaIndexedRecord(Schema schema, byte[] key, byte[] value, boolean useAvro, boolean simpleAvro, Schema valueSchema) {
        this.schema = schema;
        this.key = key;
        this.value = value;
        this.useAvro = useAvro;
        this.simpleAvro = simpleAvro;
        this.valueSchema = valueSchema;
    }

    @Override
    public int compareTo(IndexedRecord that) {
        // TODO can be compare binary directly?
        return ReflectData.get().compare(this, that, getSchema());
    }

    @Override
    public void put(int i, Object o) {
        throw new UnsupportedOperationException("Should not write to a read-only item.");
    }

    @Override
    public Object get(int i) {
        int keyPos = schema.getField("key").pos();
        if (i == keyPos) {
            return key;
        } else {
            if (useAvro) {
                try {
                    // TODO make the reader reusable?
                    DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(valueSchema);
                    BinaryDecoder decoder = null;
                    decoder = DecoderFactory.get().binaryDecoder(value, decoder);
                    GenericRecord record = datumReader.read(null, decoder);
                    if (simpleAvro) {
                        if (i > keyPos) {
                            return record.get(i - 1);
                        } else {
                            return record.get(i);
                        }
                    } else {
                        if (i == schema.getField("value").pos()) {
                            return record;
                        } else {
                            return null; // TODO throw error?
                        }
                    }
                } catch (IOException e) {
                    throw new ComponentException(e);
                }
            } else {
                if (i == schema.getField("value").pos()) {
                    return value;
                } else {
                    return null; // TODO throw error?
                }
            }
        }
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public boolean isUseAvro() {
        return useAvro;
    }

    public boolean isSimpleAvro() {
        return simpleAvro;
    }

    public Schema getValueSchema() {
        return valueSchema;
    }
}
