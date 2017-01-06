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
package org.talend.components.simplefileio.runtime.sources;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.talend.components.simplefileio.runtime.SimpleFileIoAvroRegistry;

/**
 * CSV implementation of HDFSFileSource.
 *
 * This implementation allows the recordDelimiter to be injected into the TextInputFormat.
 */
public class CsvHdfsFileSource extends FileSourceBase<LongWritable, Text, CsvHdfsFileSource> {

    static {
        // Ensure that the singleton for the SimpleFileIoAvroRegistry is created.
        SimpleFileIoAvroRegistry.get();
    }

    private final String recordDelimiter;

    private CsvHdfsFileSource(String filepattern, String recordDelimiter, SerializableSplit serializableSplit) {
        super(filepattern, TextInputFormat.class, LongWritable.class, Text.class, serializableSplit);
        this.recordDelimiter = recordDelimiter;
    }

    public static CsvHdfsFileSource of(String filepattern, String recordDelimiter) {
        return new CsvHdfsFileSource(filepattern, recordDelimiter, null);
    }

    @Override
    protected CsvHdfsFileSource createSourceForSplit(SerializableSplit serializableSplit) {
        CsvHdfsFileSource source = new CsvHdfsFileSource(filepattern, recordDelimiter, serializableSplit);
        source.setLimit(getLimit());
        return source;
    }

    @Override
    protected TalendCsvHdfsFileReader createReaderForSplit(SerializableSplit serializableSplit) throws IOException {
        return new TalendCsvHdfsFileReader(this, filepattern, serializableSplit);
    }

    private static class TalendCsvHdfsFileReader extends TalendHdfsFileReader<LongWritable, Text, CsvHdfsFileSource> {

        public TalendCsvHdfsFileReader(CsvHdfsFileSource source, String filepattern, SerializableSplit serializableSplit)
                throws IOException {
            super(source);
            job.getConfiguration().set("textinputformat.record.delimiter", source.recordDelimiter);
        }
    }
}