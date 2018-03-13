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
package org.talend.components.kinesis.runtime;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.kinesis.KinesisIO;
import org.apache.beam.sdk.io.kinesis.KinesisRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.KinesisDatastoreProperties;
import org.talend.components.kinesis.input.KinesisInputProperties;
import org.talend.daikon.avro.converter.ComparableIndexedRecordBase;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;

public class KinesisInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<KinesisInputProperties> {

    /**
     * The component instance that this runtime is configured for.
     */
    private KinesisInputProperties properties = null;

    private KinesisDatasetProperties dataset = null;

    private KinesisDatastoreProperties datastore = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, KinesisInputProperties properties) {
        this.properties = properties;
        this.dataset = properties.getDatasetProperties();
        this.datastore = dataset.getDatastoreProperties();
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        KinesisIO.Read kinesisRead = KinesisIO
                .read()
                .from(dataset.streamName.getValue(), convertToPosition(properties.position.getValue()))
                .withClientProvider(KinesisClient.getProvider(dataset));
        if (properties.useMaxReadTime.getValue()) {
            kinesisRead = kinesisRead.withMaxReadTime(new Duration(properties.maxReadTime.getValue()));
        }
        if (properties.useMaxNumRecords.getValue()) {
            kinesisRead = kinesisRead.withMaxNumRecords(properties.maxNumRecords.getValue());
        }
        PCollection<KinesisRecord> kinesisRecordPCollection = in.apply(kinesisRead);

        switch (dataset.valueFormat.getValue()) {
        case AVRO: {
            Schema schema = new Schema.Parser().parse(dataset.avroSchema.getValue());
            return kinesisRecordPCollection.apply(ParDo.of(new AvroConverter(schema.toString()))).setCoder(
                    getDefaultOutputCoder());
        }
        case CSV: {
            return kinesisRecordPCollection
                    .apply(ParDo.of(new CsvConverter(dataset.getFieldDelimiter())))
                    .setCoder(getDefaultOutputCoder());
        }
        default:
            throw new RuntimeException("To be implemented: " + dataset.valueFormat.getValue());
        }
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return LazyAvroCoder.of();
    }

    public static class CsvConverter extends DoFn<KinesisRecord, IndexedRecord> {

        public static final String RECORD_NAME = "StringArrayRecord";

        public static final String FIELD_PREFIX = "field";

        private final String fieldDelimiter;

        private Schema schema;

        CsvConverter(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        public Schema inferStringArray(String[] in) {
            List<Schema.Field> fields = new ArrayList<>();

            SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
            for (int i = 0; i < in.length; i++) {
                fa = fa.name(FIELD_PREFIX + i).type(Schema.create(Schema.Type.STRING)).noDefault();
            }
            return fa.endRecord();
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            String record = new String(c.element().getDataAsBytes(), Charset.forName("UTF-8"));
            String[] data = record.split(fieldDelimiter);
            if (schema == null) {
                schema = inferStringArray(data);
            }
            c.output(new StringArrayIndexedRecord(schema, data));
        }
    }

    public static class StringArrayIndexedRecord extends ComparableIndexedRecordBase {

        private final Schema schema;

        private final String[] data;

        public StringArrayIndexedRecord(Schema schema, String[] data) {
            this.schema = schema;
            this.data = data;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public Object get(int i) {
            return data[i];
        }

        @Override
        public void put(int i, Object v) {
            data[i] = v == null ? null : String.valueOf(v);
        }
    }

    public static class AvroConverter extends DoFn<KinesisRecord, IndexedRecord> {

        private final String schemaStr;

        private transient Schema schema;

        private transient DatumReader<GenericRecord> datumReader;

        private transient BinaryDecoder decoder;

        AvroConverter(String schemaStr) {
            this.schemaStr = schemaStr;
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            if (schema == null) {
                schema = new Schema.Parser().parse(schemaStr);
                datumReader = new GenericDatumReader<GenericRecord>(schema);
            }
            decoder = DecoderFactory.get().binaryDecoder(c.element().getDataAsBytes(), decoder);
            GenericRecord record = datumReader.read(null, decoder);
            c.output(record);
        }
    }

    private InitialPositionInStream convertToPosition(KinesisInputProperties.OffsetType offsetType) {
        switch (offsetType) {
        case LATEST:
            return InitialPositionInStream.LATEST;
        case EARLIEST:
            return InitialPositionInStream.TRIM_HORIZON;
        default:
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow(
                    String.format("Do not support OffsetType %s", offsetType));
            return null;
        }
    }
}
