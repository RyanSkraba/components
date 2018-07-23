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
package org.talend.components.marketo.runtime.client;

import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

public interface MarketoClientService {

    String getApi();

    boolean isErrorRecoverable(List<MarketoError> errors);

    // Leads
    MarketoRecordResult getLead(TMarketoInputProperties parameters, String offset);

    MarketoRecordResult getMultipleLeads(TMarketoInputProperties parameters, String offset);

    MarketoRecordResult getLeadActivity(TMarketoInputProperties parameters, String offset);

    MarketoRecordResult getLeadChanges(TMarketoInputProperties parameters, String offset);

    // List Operations
    MarketoSyncResult addToList(ListOperationParameters parameters);

    MarketoSyncResult isMemberOfList(ListOperationParameters parameters);

    MarketoSyncResult removeFromList(ListOperationParameters parameters);

    // Sync Lead
    MarketoSyncResult syncLead(TMarketoOutputProperties parameters, IndexedRecord lead);

    MarketoSyncResult syncMultipleLeads(TMarketoOutputProperties parameters, List<IndexedRecord> leads);

}
