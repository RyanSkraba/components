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
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.Calendar;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public abstract class SalesforceGetDeletedUpdatedReader<ResultT> extends SalesforceReader<IndexedRecord> {

    protected String module;

    protected transient ResultT result;

    protected transient int queryIndex;

    protected transient SObject[] inputRecords;

    protected transient int inputRecordsIndex;

    protected Calendar startDate;

    protected Calendar endDate;

    public SalesforceGetDeletedUpdatedReader(RuntimeContainer container, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(container, source);
        this.properties = props;
        module = props.module.moduleName.getStringValue();
        startDate = SalesforceRuntime.convertDateToCalendar(props.startDate.getValue(), false);
        endDate = SalesforceRuntime.convertDateToCalendar(props.endDate.getValue(), false);
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((SObjectAdapterFactory) getFactory()).convertToAvro(inputRecords[inputRecordsIndex]);
        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }

    abstract protected String[] getRecordIds(ResultT result);

    /**
     * @return the Salesforce object containing the results of the call.
     * @throws ConnectionException
     */
    abstract protected ResultT getResult() throws IOException, ConnectionException;
}
