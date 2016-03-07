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

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.container.RuntimeContainer;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

public class SalesforceReader extends AbstractBoundedReader implements BoundedReader {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceReader.class);

    protected boolean exceptionForErrors;

    protected QueryResult inputResult;

    protected SObject[] inputRecords;

    protected int inputRecordsIndex;

    protected List<Schema.Field> fieldList;

    protected PartnerConnection connection;

    protected RuntimeContainer adaptor;

    public SalesforceReader(RuntimeContainer adaptor, SalesforceSource source) {
        super(source);
        this.adaptor = adaptor;
    }

    @Override
    public boolean start() throws IOException {
        connection = ((SalesforceSource) getCurrentSource()).connect();
        return false;
    }

    @Override
    public boolean advance() throws IOException {
        if (++inputRecordsIndex >= inputRecords.length) {
            if (inputResult.isDone()) {
                return false;
            }
            try {
                inputResult = connection.queryMore(inputResult.getQueryLocator());
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
            inputRecordsIndex = 0;
            return inputResult.getSize() > 0;
        }
        return true;
    }

    @Override
    public Object getCurrent() throws NoSuchElementException {
        return inputRecords[inputRecordsIndex];
    }

    @Override
    public void close() throws IOException {
    }

}
