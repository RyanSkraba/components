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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.hadoop.WritableCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
import org.talend.components.simplefileio.runtime.beamcopy.Write;
import org.talend.components.simplefileio.runtime.sinks.UgiFileSinkBase;
import org.talend.components.simplefileio.runtime.sources.CsvHdfsFileSource;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class SimpleRecordFormatCsvIO extends SimpleRecordFormatBase {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    private final String recordDelimiter;

    private final String fieldDelimiter;

    public SimpleRecordFormatCsvIO(UgiDoAs doAs, String path, boolean overwrite, int limit, String recordDelimiter,
            String fieldDelimiter, boolean mergeOutput) {
        super(doAs, path, overwrite, limit, mergeOutput);
        this.recordDelimiter = recordDelimiter;

        String fd = fieldDelimiter;
        if (fd.length() > 1) {
            fd = fd.trim();
        }
        if (fd.isEmpty())
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow("single character field delimiter",
                    fd);
        this.fieldDelimiter = fd;
    }

    @Override
    public PCollection<IndexedRecord> read(PBegin in) {

        PCollection<?> pc2;
        if (path.startsWith("gs://")) {
            pc2 = in.apply(TextIO.read().from(path));
        } else {
            CsvHdfsFileSource source = CsvHdfsFileSource.of(doAs, path, recordDelimiter);
            source.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());

            source.setLimit(limit);

            PCollection<KV<org.apache.hadoop.io.LongWritable, Text>> pc1 = in.apply(Read.from(source));

            pc2 = pc1.apply(Values.<Text> create());
        }

        PCollection<IndexedRecord> pc3 = pc2.apply(ParDo.of(new ExtractCsvRecord<>(fieldDelimiter.charAt(0))));
        return pc3;
    }

    @Override
    public PDone write(PCollection<IndexedRecord> in) {

        if (path.startsWith("gs://")) {
            TextIO.Write b = TextIO.write().to(path);

            PCollection<String> pc1 = in.apply(ParDo.of(new FormatCsvRecord2(fieldDelimiter.charAt(0))));
            return pc1.apply(b);

        } else {
            ExtraHadoopConfiguration conf = new ExtraHadoopConfiguration();
            conf.set(CsvTextOutputFormat.RECORD_DELIMITER, recordDelimiter);
            conf.set(CsvTextOutputFormat.ENCODING, CsvTextOutputFormat.UTF_8);
            UgiFileSinkBase<NullWritable, Text> sink = new UgiFileSinkBase<>(doAs, path, overwrite, mergeOutput, CsvTextOutputFormat.class,
                    conf);
            sink.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());

            PCollection<KV<NullWritable, Text>> pc1 = in.apply(ParDo.of(new FormatCsvRecord(fieldDelimiter.charAt(0))))
                    .setCoder(KvCoder.of(WritableCoder.of(NullWritable.class), WritableCoder.of(Text.class)));

            return pc1.apply(Write.to(sink));
        }

    }

    public static class ExtractCsvSplit extends DoFn<Text, String[]> {

        static {
            // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
            SimpleFileIOAvroRegistry.get();
        }

        public final String fieldDelimiter;

        ExtractCsvSplit(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            String in = c.element().toString();
            c.output(in.split("\\Q" + fieldDelimiter + "\\E"));
        }
    }

    public static class ExtractCsvRecord<T> extends DoFn<T, IndexedRecord> {

        static {
            // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
            SimpleFileIOAvroRegistry.get();
        }

        public final char fieldDelimiter;

        /** The converter is cached for performance. */
        private transient IndexedRecordConverter<CSVRecord, ? extends IndexedRecord> converter;

        public ExtractCsvRecord(char fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            if (converter == null) {
                converter = new SimpleFileIOAvroRegistry.CsvRecordToIndexedRecordConverter();
            }
            String in = c.element().toString();
            for (CSVRecord r : CSVFormat.RFC4180.withDelimiter(fieldDelimiter).parse(new StringReader(in)))
                c.output(converter.convertToAvro(r));
        }
    }

    public static class CsvTextOutputFormat extends TextOutputFormat<NullWritable, Text> {

        public static final String RECORD_DELIMITER = "textoutputformat.record.delimiter";

        public static final String ENCODING = "csvtextoutputformat.encoding";

        public static final String UTF_8 = "UTF-8";

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

        protected static class CsvRecordWriter extends RecordWriter<NullWritable, Text> {

            public final String encoding;
            private final byte[] recordDelimiter;
            protected DataOutputStream out;

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
    }

    public static class FormatCsvRecord extends DoFn<IndexedRecord, KV<NullWritable, Text>> {

        public final char fieldDelimiter;

        private final CSVFormat format;

        private StringBuilder sb = new StringBuilder();

        public FormatCsvRecord(char fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
            format = CSVFormat.RFC4180.withDelimiter(fieldDelimiter);
        }

        @ProcessElement
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

    public static class FormatCsvRecord2 extends DoFn<IndexedRecord, String> {

        public final char fieldDelimiter;

        private final CSVFormat format;

        private StringBuilder sb = new StringBuilder();

        public FormatCsvRecord2(char fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
            format = CSVFormat.RFC4180.withDelimiter(fieldDelimiter);
        }

        @ProcessElement
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
            c.output(sb.toString());
            sb.setLength(0);
        }
    }
}
