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
package org.talend.components.simplefileio.runtime;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.io.Write;
import org.apache.beam.sdk.io.hdfs.WritableCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.commons.csv.CSVFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.simplefileio.runtime.coders.LazyAvroKeyWrapper;
import org.talend.components.simplefileio.runtime.sinks.AvroHdfsFileSink;
import org.talend.components.simplefileio.runtime.sinks.ParquetHdfsFileSink;
import org.talend.components.simplefileio.runtime.sinks.UgiFileSinkBase;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIOOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone> implements
        RuntimableRuntime<SimpleFileIOOutputProperties> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private SimpleFileIOOutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIOOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        // Controls the access security on the cluster.
        UgiDoAs doAs = SimpleFileIODatastoreRuntime.getUgiDoAs(properties.getDatasetProperties().getDatastoreProperties());

        switch (properties.getDatasetProperties().format.getValue()) {

        case AVRO: {
            LazyAvroKeyWrapper lakw = LazyAvroKeyWrapper.of();
            AvroHdfsFileSink sink = new AvroHdfsFileSink(doAs, properties.getDatasetProperties().path.getValue());
            PCollection<KV<AvroKey<IndexedRecord>, NullWritable>> pc1 = in.apply(ParDo.of(new FormatAvro()));
            pc1 = pc1.setCoder(KvCoder.of(lakw, WritableCoder.of(NullWritable.class)));
            return pc1.apply(Write.to(sink));
        }

        case CSV: {
            Configuration conf = new Configuration();
            conf.set(CsvTextOutputFormat.RECORD_DELIMITER, properties.getDatasetProperties().getRecordDelimiter());
            conf.set(CsvTextOutputFormat.ENCODING, CsvTextOutputFormat.UTF_8);
            UgiFileSinkBase<NullWritable, Text> sink = new UgiFileSinkBase<>(doAs,
                    properties.getDatasetProperties().path.getValue(), CsvTextOutputFormat.class, conf);

            String fieldDelimiter = properties.getDatasetProperties().getFieldDelimiter();
            if (fieldDelimiter.length() > 1) {
                fieldDelimiter = fieldDelimiter.trim();
            }
            if (fieldDelimiter.isEmpty())
                TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow(
                        "single character field delimiter", fieldDelimiter);

            PCollection<KV<NullWritable, Text>> pc1 = in.apply(ParDo.of(new FormatCsvRecord(fieldDelimiter.charAt(0)))).setCoder(
                    KvCoder.of(WritableCoder.of(NullWritable.class), WritableCoder.of(Text.class)));

            return pc1.apply(Write.to(sink));
        }

        case PARQUET: {
            ParquetHdfsFileSink sink = new ParquetHdfsFileSink(doAs, properties.getDatasetProperties().path.getValue());
            PCollection<KV<Void, IndexedRecord>> pc1 = in.apply(ParDo.of(new FormatParquet()));
            pc1 = pc1.setCoder(KvCoder.of(VoidCoder.of(), LazyAvroCoder.of()));
            return pc1.apply(Write.to(sink));
        }

        default:
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().format.getValue());
        }
    }

    public static class CsvTextOutputFormat extends TextOutputFormat<NullWritable, Text> {

        public static final String RECORD_DELIMITER = "textoutputformat.record.delimiter";

        public static final String ENCODING = "csvtextoutputformat.encoding";

        public static final String UTF_8 = "UTF-8";

        protected static class CsvRecordWriter extends RecordWriter<NullWritable, Text> {

            protected DataOutputStream out;

            private final byte[] recordDelimiter;

            public final String encoding;

            public CsvRecordWriter(DataOutputStream out, String encoding, String recordDelimiter) {
                this.out = out;
                this.encoding = encoding;
                try {
                    this.recordDelimiter = recordDelimiter.getBytes(encoding);
                } catch (UnsupportedEncodingException uee) {
                    throw new IllegalArgumentException("Encoding " + encoding + " not found.");
                }
            }

            public synchronized void write(NullWritable key, Text value) throws IOException {
                out.write(value.toString().getBytes(encoding));
                out.write(recordDelimiter);
            }

            public synchronized void close(TaskAttemptContext context) throws IOException {
                out.close();
            }
        }

        public RecordWriter<NullWritable, Text> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
            Configuration conf = job.getConfiguration();
            boolean isCompressed = getCompressOutput(job);
            String recordDelimiter = conf.get(RECORD_DELIMITER, "\n");
            CompressionCodec codec = null;
            String extension = "";
            if (isCompressed) {
                Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(job, GzipCodec.class);
                codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
                extension = codec.getDefaultExtension();
            }
            Path file = getDefaultWorkFile(job, extension);
            FileSystem fs = file.getFileSystem(conf);
            if (!isCompressed) {
                FSDataOutputStream fileOut = fs.create(file, false);
                return new CsvRecordWriter(fileOut, UTF_8, recordDelimiter);
            } else {
                FSDataOutputStream fileOut = fs.create(file, false);
                return new CsvRecordWriter(new DataOutputStream(codec.createOutputStream(fileOut)), UTF_8, recordDelimiter);
            }
        }
    }

    public static class FormatCsvRecord extends DoFn<IndexedRecord, KV<NullWritable, Text>> {

        public final char fieldDelimiter;

        private final CSVFormat format;

        private StringBuilder sb = new StringBuilder();

        public FormatCsvRecord(char fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
            format = CSVFormat.RFC4180.withDelimiter(fieldDelimiter);
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            // Join the strings with the delimiter.
            IndexedRecord in = c.element();
            int size = in.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                Object valueToWrite = in.get(i);
                if (valueToWrite instanceof ByteBuffer)
                    valueToWrite = new String(((ByteBuffer) valueToWrite).array());
                format.print(valueToWrite, sb, sb.length() == 0);
            }
            c.output(KV.of(NullWritable.get(), new Text(sb.toString())));
            sb.setLength(0);
        }
    }

    public static class FormatAvro extends DoFn<IndexedRecord, KV<AvroKey<IndexedRecord>, NullWritable>> {

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            c.output(KV.of(new AvroKey<>(c.element()), NullWritable.get()));
        }
    }

    public static class FormatParquet extends DoFn<IndexedRecord, KV<Void, IndexedRecord>> {

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            c.output(KV.of((Void) null, c.element()));
        }
    }

}
