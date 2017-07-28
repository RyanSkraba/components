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
package org.talend.components.marketo.runtime.client.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;

public class MarketoRecordResult extends MarketoBaseResult<IndexedRecord> {

    List<IndexedRecord> records;

    public MarketoRecordResult(String streamPosition, int recordCount, int remainCount, List<?> records) {
        super(streamPosition, recordCount, remainCount, records);
    }

    public MarketoRecordResult(Boolean success, String streamPosition, int recordCount, int remainCount, List<?> records) {
        super(streamPosition, recordCount, remainCount, records);
        setSuccess(success);
    }

    public MarketoRecordResult() {
        super();
        records = new ArrayList<>();
    }

    public List<IndexedRecord> getRecords() {
        // ensure that records is never null
        if (records == null) {
            return new ArrayList<>();
        }
        return records;
    }

    public void setRecords(List<IndexedRecord> records) {
        this.records = records;
    }

}
