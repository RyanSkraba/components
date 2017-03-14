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

import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;

public class MarketoSyncResult extends MarketoBaseResult<SyncStatus> {

    List<SyncStatus> records;

    public MarketoSyncResult(String streamPosition, int recordCount, int remainCount, List<?> records) {
        super(streamPosition, recordCount, remainCount, records);
    }

    public MarketoSyncResult() {
        super();
        records = new ArrayList<>();
    }

    @Override
    public List<SyncStatus> getRecords() {
        return records;
    }

    @Override
    public void setRecords(List<SyncStatus> records) {
        this.records = records;
    }
}
