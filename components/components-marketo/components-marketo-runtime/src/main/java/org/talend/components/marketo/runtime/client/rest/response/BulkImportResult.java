// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.runtime.client.rest.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.marketo.runtime.client.rest.type.BulkImport;

public class BulkImportResult extends PaginateResult {

    List<BulkImport> result;

    public void setResult(List<BulkImport> result) {
        this.result = result;
    }

    @Override
    public List<BulkImport> getResult() {
        // ensure that result is never null
        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }

    public List<IndexedRecord> getRecords() {
        List<IndexedRecord> records = new ArrayList<>();
        for (BulkImport bi : result) {
            records.add(bi.toIndexedRecord());
        }
        return records;
    }

}
