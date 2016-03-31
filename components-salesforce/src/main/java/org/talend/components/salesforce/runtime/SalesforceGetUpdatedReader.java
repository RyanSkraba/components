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
import java.util.NoSuchElementException;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetUpdatedReader extends SalesforceGetDeletedUpdatedReader<GetUpdatedResult, Object> {

    public SalesforceGetUpdatedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    @Override
    protected GetUpdatedResult getResult() throws IOException, ConnectionException {
        SalesforceGetDeletedUpdatedProperties props = (SalesforceGetDeletedUpdatedProperties)properties;
        return getConnection().getUpdated(module, props.startDate.getCalendarValue(), props.endDate.getCalendarValue());
    }

    @Override
    public Object getCurrent() throws NoSuchElementException {
        // TODO: Update to use an Avro-izable object.
        return result;
    }
}
