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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.VoidCoder;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.SimpleFileIOAvroRegistry;
import org.talend.components.simplefileio.runtime.hadoop.excel.ExcelFileInputFormat;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Excel implementation of HDFSFileSource.
 *
 */
public class ExcelHdfsFileSource extends FileSourceBase<Void, IndexedRecord, ExcelHdfsFileSource> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }
    
    private final LazyAvroCoder<IndexedRecord> lac;

    private ExcelHdfsFileSource(UgiDoAs doAs, String filepattern, LazyAvroCoder<IndexedRecord> lac, int limit, String encoding, String sheetName, long header, long footer, String excelFormat, ExtraHadoopConfiguration extraConfig,
            SerializableSplit serializableSplit) {
        super(doAs, filepattern, ExcelFileInputFormat.class, Void.class, IndexedRecord.class, extraConfig, serializableSplit);
        
        this.lac = lac;
        setDefaultCoder(VoidCoder.of(), (LazyAvroCoder) lac);
        
        ExtraHadoopConfiguration hadoop_config = getExtraHadoopConfiguration();
        hadoop_config.set(ExcelFileInputFormat.TALEND_ENCODING, encoding);
        hadoop_config.set(ExcelFileInputFormat.TALEND_EXCEL_SHEET_NAME, sheetName);
        hadoop_config.set(ExcelFileInputFormat.TALEND_HEADER, String.valueOf(header));
        hadoop_config.set(ExcelFileInputFormat.TALEND_FOOTER, String.valueOf(footer));
        hadoop_config.set(ExcelFileInputFormat.TALEND_EXCEL_FORMAT, excelFormat);
        
        //set it to the reader for performance
        hadoop_config.set(ExcelFileInputFormat.TALEND_EXCEL_LIMIT, String.valueOf(limit));
    }

    private ExcelHdfsFileSource(UgiDoAs doAs, String filepattern, LazyAvroCoder<IndexedRecord> lac, ExtraHadoopConfiguration extraConfig,
            SerializableSplit serializableSplit) {
        super(doAs, filepattern, ExcelFileInputFormat.class, Void.class, IndexedRecord.class, extraConfig, serializableSplit);
        
        this.lac = lac;
        setDefaultCoder(VoidCoder.of(), (LazyAvroCoder) lac);
    }

    //call by client, used to set the ExtraHadoopConfiguration : extraConfig major
    public static ExcelHdfsFileSource of(UgiDoAs doAs, String filepattern, LazyAvroCoder<IndexedRecord> lac, int limit, String encoding, String sheetName, long header, long footer, String excelFormat) {
        return new ExcelHdfsFileSource(doAs, filepattern, lac, limit, encoding, sheetName, header, footer, excelFormat, new ExtraHadoopConfiguration(), null);
    }

    //call back by framework only, we call construct to set the parameter in ExtraHadoopConfiguration : extraConfig object before it
    @Override
    protected ExcelHdfsFileSource createSourceForSplit(SerializableSplit serializableSplit) {
        ExcelHdfsFileSource source = new ExcelHdfsFileSource(doAs, filepattern, lac, getExtraHadoopConfiguration(), serializableSplit);
        source.setLimit(getLimit());
        return source;
    }

    //call back by framework only
    @Override
    protected UgiFileReader createReaderForSplit(SerializableSplit serializableSplit) throws IOException {
        return new UgiFileReader(this);
    }
}
