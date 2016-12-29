// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.io.Write;
import org.apache.beam.sdk.io.hdfs.ConfigureOnWriteHdfsFileSink;
import org.apache.beam.sdk.io.hdfs.HDFSFileSink;
import org.apache.beam.sdk.io.hdfs.WritableCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
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
import org.apache.parquet.avro.AvroParquetOutputFormat;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.output.SimpleFileIoOutputProperties;
import org.talend.components.simplefileio.runtime.coders.LazyAvroKeyWrapper;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIoOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone> implements
        RuntimableRuntime<SimpleFileIoOutputProperties> {

    static {
        // Ensure that the singleton for the SimpleFileIoAvroRegistry is created.
        SimpleFileIoAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private SimpleFileIoOutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIoOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        switch (properties.getDatasetProperties().format.getValue()) {

        case AVRO: {
            LazyAvroKeyWrapper lakw = LazyAvroKeyWrapper.of();

            Configuration conf = new Configuration();
            ConfigureOnWriteHdfsFileSink<AvroKey<IndexedRecord>, NullWritable> sink = new ConfigureOnWriteHdfsFileSink(
                    properties.getDatasetProperties().path.getValue(), AvroKeyOutputFormat.class, conf);

            PCollection<KV<AvroKey<IndexedRecord>, NullWritable>> pc1 = in.apply(ParDo.of(new FormatAvro()));
            pc1 = pc1.setCoder(KvCoder.of(lakw, WritableCoder.of(NullWritable.class)));

            return pc1.apply(Write.to(sink));
        }

        case CSV: {
            Configuration conf = new Configuration();
            conf.set(CsvTextOutputFormat.RECORD_DELIMITER, properties.getDatasetProperties().recordDelimiter.getValue());
            conf.set(CsvTextOutputFormat.ENCODING, CsvTextOutputFormat.UTF_8);
            HDFSFileSink<NullWritable, Text> sink = new HDFSFileSink(properties.getDatasetProperties().path.getValue(),
                    CsvTextOutputFormat.class, conf);
            return in.apply(ParDo.of(new FormatCsv(properties.datasetRef.getReference().fieldDelimiter.getValue())))
                    .setCoder(KvCoder.of(WritableCoder.of(NullWritable.class), WritableCoder.of(Text.class))) //
                    .apply(Write.to(sink));
        }

        case PARQUET: {
            ConfigureOnWriteHdfsFileSink<Void, IndexedRecord> sink = new ConfigureOnWriteHdfsFileSink<Void, IndexedRecord>(
                    properties.getDatasetProperties().path.getValue(), (Class) AvroParquetOutputFormat.class);

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

    public static class FormatCsv extends DoFn<IndexedRecord, KV<NullWritable, Text>> {

        public final String fieldDelimiter;

        private StringBuilder sb = new StringBuilder();

        public FormatCsv(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            // Join the strings with the delimiter.
            IndexedRecord in = c.element();
            int size = in.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                if (sb.length() != 0)
                    sb.append(fieldDelimiter);
                sb.append(in.get(i));
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
