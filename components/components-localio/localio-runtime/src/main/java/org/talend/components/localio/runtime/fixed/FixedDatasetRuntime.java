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
package org.talend.components.localio.runtime.fixed;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.talend.components.adapter.beam.io.rowgenerator.GeneratorFunction;
import org.talend.components.adapter.beam.io.rowgenerator.GeneratorFunctions;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.localio.LocalIOErrorCode;
import org.talend.components.localio.fixed.FixedDatasetProperties;
import org.talend.daikon.avro.converter.ComparableIndexedRecordBase;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.avro.converter.JsonGenericRecordConverter;
import org.talend.daikon.avro.inferrer.JsonSchemaInferrer;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FixedDatasetRuntime implements DatasetRuntime<FixedDatasetProperties> {

    /**
     * The dataset instance that this runtime is configured for.
     */
    private FixedDatasetProperties properties = null;

    /**
     * @return true if we are generating random data for this runtime.
     */
    public boolean isRandom() {
        return properties.format.getValue() == FixedDatasetProperties.RecordFormat.AVRO
                && properties.values.getValue().trim().length() == 0;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, FixedDatasetProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        switch (properties.format.getValue()) {
        case CSV:
            // Try to get the schema from the specified value.
            String csvSchema = properties.csvSchema.getValue();
            if (!csvSchema.trim().isEmpty()) {
                try {
                    CSVRecord r = CSVFormat.RFC4180 //
                            .withDelimiter(properties.getFieldDelimiter().charAt(0)) //
                            .withRecordSeparator(properties.getRecordDelimiter()) //
                            .parse(new StringReader(csvSchema)).iterator().next();
                    return CsvRecordToIndexedRecordConverter.inferSchema(r);
                } catch (Exception e) {
                    throw LocalIOErrorCode.createCannotParseSchema(e, csvSchema);
                }
            }
            // Fall back to a schema based on the number of columns.
            try {
                int maxSize = 0;
                for (CSVRecord r : CSVFormat.RFC4180 //
                        .withDelimiter(properties.getFieldDelimiter().charAt(0)) //
                        .withRecordSeparator(properties.getRecordDelimiter())
                        .parse(new StringReader(properties.values.getValue())))
                    maxSize = Math.max(maxSize, r.size());
                if (maxSize == 0)
                    throw LocalIOErrorCode.requireAtLeastOneRecord(new RuntimeException());
                return CsvRecordToIndexedRecordConverter.inferSchema(maxSize);
            } catch (IOException e) {
                throw LocalIOErrorCode.createCannotParseSchema(e, properties.values.getValue());
            }
        case JSON:
            if (properties.values.getValue().trim().isEmpty())
                throw LocalIOErrorCode.requireAtLeastOneRecord(new RuntimeException());
            return getValues(1).get(0).getSchema();
        case AVRO:
            try {
                return new Schema.Parser().parse(properties.schema.getValue());
            } catch (Exception e) {
                throw LocalIOErrorCode.createCannotParseSchema(e, properties.schema.getValue());
            }
        }
        throw LocalIOErrorCode.createCannotParseSchema(null, properties.schema.getValue());
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        for (IndexedRecord value : getValues(limit)) {
            consumer.accept(value);
        }
    }

    public List<IndexedRecord> getValues(int limit) {
        List<IndexedRecord> values = new ArrayList<>();
        switch (properties.format.getValue()) {
        case CSV:
            try {
                CsvRecordToIndexedRecordConverter converter = new CsvRecordToIndexedRecordConverter(getSchema());
                for (CSVRecord r : CSVFormat.RFC4180 //
                        .withDelimiter(properties.getFieldDelimiter().charAt(0)) //
                        .withRecordSeparator(properties.getRecordDelimiter())
                        .parse(new StringReader(properties.values.getValue())))
                    values.add(converter.convertToAvro(r));
            } catch (IOException e) {
                throw LocalIOErrorCode.createCannotParseSchema(e, properties.values.getValue());
            }
            break;
        case JSON:
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(mapper);
            JsonGenericRecordConverter converter = null;
            JsonFactory jsonFactory = new JsonFactory();
            try (StringReader r = new StringReader(properties.values.getValue())) {
                Iterator<JsonNode> value = mapper.readValues(jsonFactory.createParser(r), JsonNode.class);
                int count = 0;
                while (value.hasNext() && count++ < limit) {
                    String json = value.next().toString();
                    if (converter == null) {
                        Schema jsonSchema = jsonSchemaInferrer.inferSchema(json);
                        converter = new JsonGenericRecordConverter(jsonSchema);
                    }
                    values.add(converter.convertToAvro(json));
                }
            } catch (IOException e) {
                throw LocalIOErrorCode.createCannotParseJson(e, properties.schema.getValue(), properties.values.getValue());
            }
            break;
        case AVRO:
            Schema schema = getSchema();
            if (isRandom()) {
                GeneratorFunction<IndexedRecord> gf = (GeneratorFunction<IndexedRecord>) GeneratorFunctions.of(getSchema());
                GeneratorFunction.GeneratorContext ctx = GeneratorFunction.GeneratorContext.of(0, 0L);
                for (int i = 0; i < limit; i++) {
                    ctx.setRowId(i);
                    values.add(gf.apply(ctx));
                }
            } else {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(properties.values.getValue().trim().getBytes())) {
                    JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, bais);
                    DatumReader<IndexedRecord> reader = new GenericDatumReader<>(schema);
                    int count = 0;
                    while (count++ < limit) {
                        values.add(reader.read(null, decoder));
                    }
                } catch (EOFException e) {
                    // Indicates the end of the values.
                } catch (IOException e) {
                    throw LocalIOErrorCode.createCannotParseAvroJson(e, properties.schema.getValue(),
                            properties.values.getValue());
                }
            }
            break;
        }
        return values;
    }

    public static class CsvRecordToIndexedRecordConverter implements IndexedRecordConverter<CSVRecord, CsvIndexedRecord> {

        public static final String RECORD_NAME = "CSVRecord";

        public static final String FIELD_PREFIX = "field";

        private transient Schema schema = null;

        public CsvRecordToIndexedRecordConverter(Schema schema) {
            this.schema = schema;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public void setSchema(Schema schema) {
            this.schema = schema;
        }

        @Override
        public Class<CSVRecord> getDatumClass() {
            return CSVRecord.class;
        }

        @Override
        public CSVRecord convertToDatum(CsvIndexedRecord value) {
            return value.value;
        }

        @Override
        public CsvIndexedRecord convertToAvro(CSVRecord value) {
            return new CsvIndexedRecord(getSchema(), value);
        }

        /**
         * Infers an Avro schema for the given String array. This can be an expensive operation so the schema should be
         * cached where possible. This is always an {@link Schema.Type#RECORD}.
         *
         * @param in the DescribeSObjectResult to analyse.
         * @return the schema for data given from the object.
         */
        static Schema inferSchema(CSVRecord in) {
            List<Schema.Field> fields = new ArrayList<>();
            SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
            for (int i = 0; i < in.size(); i++) {
                fa = fa.name(in.get(i)).type(Schema.create(Schema.Type.STRING)).noDefault();
            }
            return fa.endRecord();
        }

        public static Schema inferSchema(int maxSize) {
            List<Schema.Field> fields = new ArrayList<>();
            SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
            for (int i = 0; i < maxSize; i++) {
                fa = fa.name(FIELD_PREFIX + i).type(Schema.create(Schema.Type.STRING)).noDefault();
            }
            return fa.endRecord();
        }
    }

    public static class CsvIndexedRecord extends ComparableIndexedRecordBase {

        private final Schema schema;

        private final CSVRecord value;

        public CsvIndexedRecord(Schema schema, CSVRecord value) {
            this.schema = schema;
            this.value = value;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public Object get(int i) {
            try {
                return value.get(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override
        public void put(int i, Object v) {
            throw new IndexedRecordConverter.UnmodifiableAdapterException();
        }
    }

}
