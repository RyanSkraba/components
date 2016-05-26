// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.DeletedRecord;
import com.sforce.soap.partner.GetDeletedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetDeletedReader extends SalesforceGetDeletedUpdatedReader<GetDeletedResult> {

    public SalesforceGetDeletedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    @Override
    protected List<String> getRecordIds(GetDeletedResult result) {
        List<String> ids = new ArrayList<>();
        if (result != null) {
            DeletedRecord[] records = result.getDeletedRecords();
            for (DeletedRecord record : records) {
                ids.add(record.getId());
            }
        }
        return ids;
    }

    @Override
    protected GetDeletedResult getResult() throws IOException, ConnectionException {
        SalesforceGetDeletedUpdatedProperties props = (SalesforceGetDeletedUpdatedProperties) properties;
        return getConnection().getDeleted(module, props.startDate.getValue(), props.endDate.getValue());
    }

}
