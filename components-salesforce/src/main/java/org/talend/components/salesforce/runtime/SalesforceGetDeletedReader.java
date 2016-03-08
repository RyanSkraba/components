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

import com.sforce.soap.partner.GetDeletedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetDeletedReader extends SalesforceGetDeletedUpdatedReader<GetDeletedResult, Object> {

    public SalesforceGetDeletedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    @Override
    protected GetDeletedResult getResult() throws IOException, ConnectionException {
        return getConnection().getDeleted(module, props.startDate.getCalendarValue(), props.endDate.getCalendarValue());
    }

    @Override
    public Object getCurrent() throws NoSuchElementException {
        // TODO: Update to use an Avro-izable object.
        return result;
    }

}
