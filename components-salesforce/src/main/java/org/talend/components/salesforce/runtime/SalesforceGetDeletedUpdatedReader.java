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

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.ws.ConnectionException;

public abstract class SalesforceGetDeletedUpdatedReader<ResultT, T> extends SalesforceReader<T> {

    protected SalesforceGetDeletedUpdatedProperties props;

    protected String module;

    protected transient ResultT result;

    public SalesforceGetDeletedUpdatedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(source);
        this.props = props;
        module = props.module.moduleName.getStringValue();
    }

    @Override
    public boolean start() throws IOException {
        try {
            result = getResult();
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
        return result != null;
    }

    @Override
    public boolean advance() throws IOException {
        // only one record is available for this reader.
        result = null;
        return false;
    }

    /**
     * @return the Salesforce object containing the results of the call.
     * @throws ConnectionException
     */
    abstract protected ResultT getResult() throws IOException, ConnectionException;
}
