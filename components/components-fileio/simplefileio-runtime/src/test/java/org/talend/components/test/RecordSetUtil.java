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
package org.talend.components.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSTestUtil;
import org.talend.daikon.avro.GenericDataRecordHelper;

/**
 *
 */
public class RecordSetUtil {

    public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz123456780".toCharArray();

    public static RecordSet getSimpleTestData(long seed) {
        return new RecordSet("simple", getRandomRecords(seed, 0, 10, 10, 6));
    }

    public static RecordSet getEmptyTestData() {
        return new RecordSet("empty");
    }

    /**
     *
     * @param seed
     * @param startId
     * @param lines
     * @param columns
     * @param tagLength
     * @return
     */
    public static List<IndexedRecord> getRandomRecords(long seed, int startId, int lines, int columns, int tagLength) {

        List<IndexedRecord> generated = new ArrayList<>();
        Random r = new Random(seed);

        for (int id = 0; id < lines; id++) {
            Object[] row = new Object[columns];
            row[0] = startId + id;
            for (int column = 1; column < columns; column++) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tagLength; i++)
                    sb.append(CHARS[r.nextInt(CHARS.length)]);
                row[column] = sb.toString();
            }
            generated.add(GenericDataRecordHelper.createRecord(row));
        }

        return generated;
    }

    public static String writeRandomCsvFile(FileSystem fs, String path, long seed, int startId, int lines, int columns,
            int tagLength, String fieldDelimiter, String recordDelimiter) throws IOException {
        Random r = new Random(seed);
        try (PrintWriter w = new PrintWriter(fs.create(new Path(path)))) {
            for (int id = 0; id < lines; id++) {
                w.print(startId + id);
                for (int column = 1; column < columns; column++) {
                    w.print(fieldDelimiter);
                    for (int i = 0; i < tagLength; i++)
                        w.print(CHARS[r.nextInt(CHARS.length)]);
                }
                w.print(recordDelimiter);
            }
        }
        return DFSTestUtil.readFile(fs, new Path(path));
    }

    /**
     * Writes all records from the test set into a single Avro file on the file system.
     * 
     * @param fs The filesystem.
     * @param path The path of the file on the filesystem.
     * @param td The test data to write.
     * @throws IOException If there was an exception writing to the filesystem.
     */
    public static void writeRandomAvroFile(FileSystem fs, String path, RecordSet td) throws IOException {
        try (OutputStream out = fs.create(new Path(path))) {
            DatumWriter<IndexedRecord> datumWriter = new GenericDatumWriter<>(td.getSchema());
            DataFileWriter<IndexedRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
            dataFileWriter.create(td.getSchema(), out);
            for (List<IndexedRecord> partition : td.getPartitions()) {
                for (IndexedRecord record : partition) {
                    dataFileWriter.append(record);
                }
            }
            dataFileWriter.close();
        }
    }

    /**
     * Writes all records from the test set into a single CSV file on the file system.
     *
     * @param fs The filesystem.
     * @param path The path of the file on the filesystem.
     * @param td The test data to write.
     * @throws IOException If there was an exception writing to the filesystem.
     */
    public static void writeRandomCsvFile(FileSystem fs, String path, RecordSet td, String encoding) throws IOException {
        int size = td.getSchema().getFields().size();
        try (PrintWriter w = (encoding != null ? new PrintWriter(new OutputStreamWriter(fs.create(new Path(path)), encoding)) : new PrintWriter(fs.create(new Path(path))))) {
            for (List<IndexedRecord> partition : td.getPartitions()) {
                for (IndexedRecord record : partition) {
                    if (size > 0)
                        w.print(record.get(0));
                    for (int i = 1; i < size; i++) {
                        w.print(';');
                        w.print(record.get(i));
                    }
                    w.print(System.getProperty("line.separator"));
                }
            }
        }
    }
    
    public static void writeCsvFile(FileSystem fs, String path, String content, String encoding) throws IOException {
      try (PrintWriter w = (encoding != null ? new PrintWriter(new OutputStreamWriter(fs.create(new Path(path)), encoding)) : new PrintWriter(fs.create(new Path(path))))) {
          w.print(content);
      }
  }

    public static void writeTextFile(FileSystem fs, String path, Supplier<String> contentGetter, String encoding) throws IOException {
        try (PrintWriter w = (encoding != null ? new PrintWriter(new OutputStreamWriter(fs.create(new Path(path)), encoding)) : new PrintWriter(fs.create(new Path(path))))) {
            String content = contentGetter.get();
            long iter = 0;
            while (content != null && content.length() > 0 && iter < 3_000_000L) {
                w.print(content);
                iter++;
                if (iter % 10F == 0F) {
                    w.flush();
                }
            }
        }
    }

}
