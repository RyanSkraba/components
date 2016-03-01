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

import com.sforce.ws.ConnectionException;

public abstract class SalesforceGetDeletedUpdatedReader extends SalesforceReader {

    protected SalesforceGetDeletedUpdatedProperties props;

    protected String module = props.module.moduleName.getStringValue();

    protected boolean hasResult;

    public SalesforceGetDeletedUpdatedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source);
        this.props = props;
    }

    @Override
    public boolean start() throws IOException {
        super.start();

        module = props.module.moduleName.getStringValue();
        try {
            hasResult = getResult();
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
        return hasResult;
    }

    abstract protected boolean getResult() throws ConnectionException;

    abstract protected Object returnResult();

    @Override
    public boolean advance() throws IOException {
        // only one record is avalable for this reader.
        return false;
    }

    @Override
    public Object getCurrent() throws NoSuchElementException {
        if (!hasResult)
            return null;
        return returnResult();
    }

}
