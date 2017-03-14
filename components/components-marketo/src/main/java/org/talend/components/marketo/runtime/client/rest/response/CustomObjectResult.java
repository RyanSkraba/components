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
package org.talend.components.marketo.runtime.client.rest.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.marketo.runtime.client.rest.type.CustomObject;

public class CustomObjectResult extends PaginateResult {

    List<CustomObject> result;

    @Override
    public List<CustomObject> getResult() {
        return result;
    }

    public void setResult(List<CustomObject> result) {
        this.result = result;
    }

    public List<IndexedRecord> getRecords() {
        List<IndexedRecord> records = new ArrayList<>();
        for (CustomObject co : result) {
            records.add(co.toIndexedRecord());
        }
        return records;
    }
}
