// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.adapter.beam.io.rowgenerator;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.IndexedRecord;

/**
 * A collection of pre-defined {@link GeneratorFunction} that are used in creating random Avro rows.
 */
public class GeneratorFunctions {

    private GeneratorFunctions() {
    }

    public static GeneratorFunction<?> of(Schema schema) {
        switch (schema.getType()) {
        case RECORD:
            return ofRecord(schema);
        case ENUM:
            return new EnumGenerator(schema);
        case ARRAY:
            return new ArrayGenerator(schema);
        case MAP:
            return new MapGenerator(schema);
        case UNION:
            return new UnionGenerator(schema);
        case FIXED:
            return new FixedBytesGenerator(schema);
        case STRING:
            return ofString(schema);
        case BYTES:
            return new BytesGenerator(schema, 100);
        case INT:
            return new IntGenerator(schema);
        case LONG:
            return new LongGenerator(schema);
        case FLOAT:
            return new FloatGenerator(schema);
        case DOUBLE:
            return new DoubleGenerator(schema);
        case BOOLEAN:
            return new BooleanGenerator(schema);
        default:
            // NULL and all unknown types.
            return new NullGenerator();
        }
    }

    public static GeneratorFunction<IndexedRecord> ofRecord(Schema schema) {
        return new RecordGenerator(schema);
    }

    private static GeneratorFunction<String> ofString(Schema schema) {
        return new StringGenerator(schema);
    }

    public static class RandomPickGenerator<T> extends GeneratorFunction<T> {

        private final T[] constants;

        public RandomPickGenerator(T[] constants) {
            this.constants = constants;
        }

        @Override
        public T apply(GeneratorContext input) {
            return constants[input.getRandom().nextInt(constants.length)];
        }
    }

    public static class RecordGenerator extends GeneratorFunction<IndexedRecord> {

        private final GeneratorFunction<?>[] inner;

        private final String jsonSchema;

        private transient Schema schema;

        public RecordGenerator(Schema recordSchema) {
            jsonSchema = recordSchema.toString();
            this.schema = recordSchema;
            inner = new GeneratorFunction<?>[recordSchema.getFields().size()];
            for (int i = 0; i < inner.length; i++) {
                inner[i] = of(recordSchema.getFields().get(i).schema());
            }
        }

        @Override
        public IndexedRecord apply(GeneratorContext input) {
            if (schema == null) {
                schema = new Schema.Parser().parse(jsonSchema);
            }
            GenericData.Record record = new GenericData.Record(schema);
            for (int i = 0; i < inner.length; i++) {
                record.put(i, inner[i].apply(input));
            }
            return record;
        }
    }

    public static class EnumGenerator extends GeneratorFunction<GenericEnumSymbol> {

        private final String jsonSchema;

        private transient GenericEnumSymbol[] constants;

        private transient Schema schema;

        public EnumGenerator(Schema enumSchema) {
            jsonSchema = enumSchema.toString();
            schema = enumSchema;
        }

        public GenericEnumSymbol apply(GeneratorContext input) {
            if (constants == null) {
                if (schema == null) {
                    schema = new Schema.Parser().parse(jsonSchema);
                }
                constants = new GenericEnumSymbol[schema.getEnumSymbols().size()];
                for (int i = 0; i < constants.length; i++) {
                    constants[i] = new GenericData.EnumSymbol(schema, schema.getEnumSymbols().get(i));
                }
            }
            return constants[input.getRandom().nextInt(constants.length)];
        }
    }

    public static class ArrayGenerator extends GeneratorFunction<List<Object>> {

        private final GeneratorFunction<?> inner;

        public ArrayGenerator(Schema schema) {
            inner = of(schema.getElementType());
        }

        @Override
        public List<Object> apply(GeneratorContext input) {
            Object[] array = new Object[input.getRandom().nextInt(10)];
            for (int i = 0; i < array.length; i++) {
                array[i] = inner.apply(input);
            }
            return Arrays.asList(array);
        }
    }

    public static class MapGenerator extends GeneratorFunction<Map<String, Object>> {

        private final GeneratorFunction<String> keyGenerator;

        private final GeneratorFunction<?> valueGenerator;

        public MapGenerator(Schema schema) {
            keyGenerator = ofString(schema);
            valueGenerator = of(schema.getValueType());
        }

        @Override
        public Map<String, Object> apply(GeneratorContext input) {
            int size = input.getRandom().nextInt(10);
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(keyGenerator.apply(input), valueGenerator.apply(input));
            }
            return map;
        }
    }

    public static class UnionGenerator extends GeneratorFunction<Object> {

        private final GeneratorFunction<?>[] inner;

        public UnionGenerator(Schema schema) {
            inner = new GeneratorFunction<?>[schema.getTypes().size()];
            for (int i = 0; i < inner.length; i++) {
                inner[i] = of(schema.getTypes().get(i));
            }
        }

        @Override
        public Object apply(GeneratorContext input) {
            return inner[input.getRandom().nextInt(inner.length)].apply(input);
        }
    }

    public static class StringGenerator extends GeneratorFunction<String> {

        public static final char[] ALPHANUMERIC = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "123456780")
                .toCharArray();

        private final char[] characters;

        private final int size;

        /** Reuse StringBuilder. */
        private final StringBuilder sb = new StringBuilder();

        public StringGenerator(Schema schema) {
            characters = ALPHANUMERIC;
            size = 10;
        }

        @Override
        public String apply(GeneratorContext input) {
            sb.setLength(0);
            for (int i = 0; i < size; i++) {
                sb.append(characters[input.getRandom().nextInt(characters.length)]);
            }
            return sb.toString();
        }
    }

    public static class FixedBytesGenerator extends GeneratorFunction<GenericFixed> {

        private final int size;

        private final String jsonSchema;

        private transient Schema schema;

        public FixedBytesGenerator(Schema fixedSchema) {
            size = fixedSchema.getFixedSize();
            jsonSchema = fixedSchema.toString();
            this.schema = fixedSchema;
        }

        @Override
        public GenericFixed apply(GeneratorContext input) {
            if (schema == null) {
                schema = new Schema.Parser().parse(jsonSchema);
            }
            byte[] buffer = new byte[size];
            input.getRandom().nextBytes(buffer);
            return new GenericData.Fixed(schema, buffer);
        }
    }

    public static class BytesGenerator extends GeneratorFunction<ByteBuffer> {

        private final int maxSize;

        public BytesGenerator(Schema schema, int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public ByteBuffer apply(GeneratorContext input) {
            byte[] buffer = new byte[input.getRandom().nextInt(maxSize)];
            input.getRandom().nextBytes(buffer);
            return ByteBuffer.wrap(buffer);
        }
    }

    public static class IntGenerator extends GeneratorFunction<Integer> {

        public IntGenerator(Schema schema) {
        }

        @Override
        public Integer apply(GeneratorContext input) {
            return input.getRandom().nextInt();
        }
    }

    public static class LongGenerator extends GeneratorFunction<Long> {

        public LongGenerator(Schema schema) {
        }

        @Override
        public Long apply(GeneratorContext input) {
            return input.getRandom().nextLong();
        }
    }

    public static class FloatGenerator extends GeneratorFunction<Float> {

        public FloatGenerator(Schema schema) {
        }

        @Override
        public Float apply(GeneratorContext input) {
            return input.getRandom().nextFloat();
        }
    }

    public static class DoubleGenerator extends GeneratorFunction<Double> {

        public DoubleGenerator(Schema schema) {
        }

        @Override
        public Double apply(GeneratorContext input) {
            return input.getRandom().nextDouble();
        }
    }

    public static class BooleanGenerator extends GeneratorFunction<Boolean> {

        public BooleanGenerator(Schema schema) {
        }

        @Override
        public Boolean apply(GeneratorContext input) {
            return input.getRandom().nextBoolean();
        }
    }

    public static class NullGenerator extends GeneratorFunction<Void> {

        @Override
        public Void apply(GeneratorContext input) {
            return null;
        }
    }
}
