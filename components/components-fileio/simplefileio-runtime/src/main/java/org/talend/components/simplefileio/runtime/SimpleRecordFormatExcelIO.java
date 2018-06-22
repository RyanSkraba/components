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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.simplefileio.ExcelFormat;
import org.talend.components.simplefileio.runtime.sources.ExcelHdfsFileSource;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

public class SimpleRecordFormatExcelIO extends SimpleRecordFormatBase {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }
    
    private String sheetName;
    private final String encoding;
    private final long header;
    private final long footer;
    private final ExcelFormat excelFormat;
    
    public SimpleRecordFormatExcelIO(UgiDoAs doAs, String path, boolean overwrite, int limit, boolean mergeOutput, String encoding, String sheetName, long header, long footer, ExcelFormat excelFormat) {
        super(doAs, path, overwrite, limit, mergeOutput);
        this.sheetName = sheetName;
        this.encoding = encoding;
        this.header = header;
        this.footer = footer;
        this.excelFormat = excelFormat;
    }

    @Override
    public PCollection<IndexedRecord> read(PBegin in) {
        LazyAvroCoder<IndexedRecord> lac = LazyAvroCoder.of();
        
        ExcelHdfsFileSource source = ExcelHdfsFileSource.of(doAs, path, lac, limit, encoding, sheetName, header, footer, excelFormat.name());
        source.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());
        source.setLimit(limit);
        PCollection<KV<Void, IndexedRecord>> pc1 = in.apply(Read.from(source)).setCoder(source.getDefaultOutputCoder());
        PCollection<IndexedRecord> pc2 = pc1.apply(Values.<IndexedRecord>create());
        return pc2;
    }

    //now no output support, so TODO
    @Override
    public PDone write(PCollection<IndexedRecord> in) {
      // TODO Auto-generated method stub
      return null;
    }

}
