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

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;
import org.talend.daikon.exception.TalendRuntimeException;

import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetUpdatedReader extends SalesforceGetDeletedReader {

    private GetUpdatedResult result;

    public SalesforceGetUpdatedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    protected boolean getResult() throws ConnectionException {
        result = connection.getUpdated(module, props.startDate.getCalendarValue(), props.endDate.getCalendarValue());
        return result != null;
    }

    @Override
    protected Object returnResult() {
        // FIXME - implement me
        TalendRuntimeException.unexpectedException("Implement me");
        return null;
    }

}
