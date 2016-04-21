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
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import com.sforce.soap.partner.DeletedRecord;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetUpdatedReader extends SalesforceGetDeletedUpdatedReader<GetUpdatedResult> {

    public SalesforceGetUpdatedReader(RuntimeContainer adaptor, SalesforceSource source,
                                      SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    @Override
    protected List<String> getRecordIds(GetUpdatedResult result) {
        List<String> ids = new ArrayList<>();
        if (result != null) {
            ids = Arrays.asList(result.getIds());
        }
        return ids;
    }

    @Override
    protected GetUpdatedResult getResult() throws IOException, ConnectionException {
        SalesforceGetDeletedUpdatedProperties props = (SalesforceGetDeletedUpdatedProperties) properties;
        return getConnection().getUpdated(module, props.startDate.getCalendarValue(), props.endDate.getCalendarValue());
    }

}
