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

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.SimpleFileIOAvroRegistry;
import org.talend.components.simplefileio.runtime.hadoop.csv.CSVFileInputFormat;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * CSV implementation of HDFSFileSource.
 *
 */
public class CsvHdfsFileSource extends FileSourceBase<LongWritable, BytesWritable, CsvHdfsFileSource> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    private CsvHdfsFileSource(UgiDoAs doAs, String filepattern, String recordDelimiter, String encoding, long header, String textEnclosure, String escapeChar, ExtraHadoopConfiguration extraConfig,
            SerializableSplit serializableSplit) {
        super(doAs, filepattern, CSVFileInputFormat.class, LongWritable.class, BytesWritable.class, extraConfig, serializableSplit);
        ExtraHadoopConfiguration hadoop_config = getExtraHadoopConfiguration();
        hadoop_config.set(CSVFileInputFormat.TALEND_ENCODING, encoding);
        hadoop_config.set(CSVFileInputFormat.TALEND_HEADER, String.valueOf(header));
        hadoop_config.set(CSVFileInputFormat.TALEND_ROW_DELIMITED, recordDelimiter);
        hadoop_config.set(CSVFileInputFormat.TALEND_TEXT_ENCLOSURE, textEnclosure);
        hadoop_config.set(CSVFileInputFormat.TALEND_ESCAPE, escapeChar);
    }

    private CsvHdfsFileSource(UgiDoAs doAs, String filepattern, ExtraHadoopConfiguration extraConfig,
            SerializableSplit serializableSplit) {
        super(doAs, filepattern, CSVFileInputFormat.class, LongWritable.class, BytesWritable.class, extraConfig, serializableSplit);
    }

    public static CsvHdfsFileSource of(UgiDoAs doAs, String filepattern, String recordDelimiter, String encoding, long header, String textEnclosure, String escapeChar) {
        return new CsvHdfsFileSource(doAs, filepattern, recordDelimiter, encoding, header, textEnclosure, escapeChar, new ExtraHadoopConfiguration(), null);
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
