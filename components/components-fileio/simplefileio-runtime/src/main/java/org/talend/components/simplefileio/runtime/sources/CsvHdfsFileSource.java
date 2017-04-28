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
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.SimpleFileIOAvroRegistry;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * CSV implementation of HDFSFileSource.
 *
 * This implementation allows the recordDelimiter to be injected into the TextInputFormat.
 */
public class CsvHdfsFileSource extends FileSourceBase<LongWritable, Text, CsvHdfsFileSource> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    private CsvHdfsFileSource(UgiDoAs doAs, String filepattern, String recordDelimiter, ExtraHadoopConfiguration extraConfig,
            SerializableSplit serializableSplit) {
        super(doAs, filepattern, TextInputFormat.class, LongWritable.class, Text.class, extraConfig, serializableSplit);
        getExtraHadoopConfiguration().set("textinputformat.record.delimiter", recordDelimiter);
    }

    private CsvHdfsFileSource(UgiDoAs doAs, String filepattern, ExtraHadoopConfiguration extraConfig,
            SerializableSplit serializableSplit) {
        super(doAs, filepattern, TextInputFormat.class, LongWritable.class, Text.class, extraConfig, serializableSplit);
    }

    public static CsvHdfsFileSource of(UgiDoAs doAs, String filepattern, String recordDelimiter) {
        return new CsvHdfsFileSource(doAs, filepattern, recordDelimiter, new ExtraHadoopConfiguration(), null);
    }

    @Override
    protected CsvHdfsFileSource createSourceForSplit(SerializableSplit serializableSplit) {
        CsvHdfsFileSource source = new CsvHdfsFileSource(doAs, filepattern, getExtraHadoopConfiguration(), serializableSplit);
        source.setLimit(getLimit());
        return source;
    }

    @Override
    protected UgiFileReader createReaderForSplit(SerializableSplit serializableSplit) throws IOException {
        return new UgiFileReader(this);
    }
}
