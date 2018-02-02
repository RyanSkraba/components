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
package org.talend.components.adapter.beam.kv;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

public class MergeKVFn extends DoFn<KV<IndexedRecord, IndexedRecord>, IndexedRecord> {

    private transient Schema mergedSchema = null;

    @Setup
    public void setup() throws Exception {
    }

    @ProcessElement
    public void processElement(ProcessContext context) {
        KV<IndexedRecord, IndexedRecord> inputRecord = context.element();
        if (mergedSchema == null) {
            mergedSchema = SchemaGeneratorUtils.mergeKeyValues(inputRecord.getKey().getSchema(),
                    inputRecord.getValue().getSchema());
        }
        context.output(KeyValueUtils.mergeIndexedRecord(inputRecord.getKey(), inputRecord.getValue(), mergedSchema));
    }

}
