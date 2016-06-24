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
import java.util.Calendar;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public abstract class SalesforceGetDeletedUpdatedReader<ResultT> extends SalesforceReader<IndexedRecord> {

    protected String module;

    protected transient ResultT result;

    protected int soqlCharacterLimit = 10000;

    protected List<String> queryStringList;

    private transient int queryIndex;

    private transient QueryResult inputResult;

    private transient SObject[] inputRecords;

    private transient int inputRecordsIndex;

    protected Calendar startDate;

    protected Calendar endDate;

    public SalesforceGetDeletedUpdatedReader(RuntimeContainer container, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(container, source);
        this.properties = props;
        module = props.module.moduleName.getStringValue();
        startDate = SalesforceRuntime.convertDateToCalendar(props.startDate.getValue());
        endDate = SalesforceRuntime.convertDateToCalendar(props.endDate.getValue());
    }

    @Override
    public boolean start() throws IOException {
        try {
            result = getResult();
            queryStringList = getQueryStringList(result);
            if (queryStringList.size() == 0) {
                return false;
            }
            inputResult = getConnection().queryAll(queryStringList.get(queryIndex++));
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
        boolean startable = inputRecords.length > 0;
        if (startable) {
            dataCount++;
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        inputRecordsIndex++;
        // Fast return conditions.
        if (inputRecordsIndex < inputRecords.length) {
            dataCount++;
            return true;
        }
        if ((inputResult == null || inputResult.isDone()) && queryIndex < queryStringList.size()) {
            try {
                inputResult = getConnection().queryAll(queryStringList.get(queryIndex++));
                inputRecords = inputResult.getRecords();
                inputRecordsIndex = 0;
                boolean isAdvanced = inputResult.getSize() > 0;
                if (isAdvanced) {
                    dataCount++;
                }
                return isAdvanced;
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
        }
        return false;
    }

    /**
     * Get the SOQL statements with the SObject Ids which get from the reocord.
     * Because of the SOQL statements have a limitation of the length.
     * So we create new statement when the length reach soqlCharacterLimit
     */
    protected List<String> getQueryStringList(ResultT result) throws IOException {
        List<String> ids = getRecordIds(result);
        StringBuilder sb = new StringBuilder();
        List<String> queryStringList = new ArrayList<>();
        String baseQueryString = getQueryString(properties);
        sb.append(baseQueryString);
        sb.append(" where ");
        int recordSize = ids.size();
        String condition = "";
        for (int i = 0; i < recordSize; i++) {
            condition = "Id='" + ids.get(i) + "'";
            if (sb.length() + condition.length() > soqlCharacterLimit && i < (recordSize - 1)) {
                sb.delete(sb.length() - 4, sb.length());
                queryStringList.add(sb.toString());
                sb.delete(0, sb.length());
                sb.append(baseQueryString);
                sb.append(" where ");
                sb.append(condition);
                sb.append(" or ");
            } else if (i == (recordSize - 1)) {
                sb.append(condition);
                queryStringList.add(sb.toString());
            } else {
                sb.append(condition);
                sb.append(" or ");
            }
        }
        return queryStringList;
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((SObjectAdapterFactory) getFactory()).convertToAvro(inputRecords[inputRecordsIndex]);
        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }

    abstract protected List<String> getRecordIds(ResultT result);

    /**
     * @return the Salesforce object containing the results of the call.
     * @throws ConnectionException
     */
    abstract protected ResultT getResult() throws IOException, ConnectionException;
}
