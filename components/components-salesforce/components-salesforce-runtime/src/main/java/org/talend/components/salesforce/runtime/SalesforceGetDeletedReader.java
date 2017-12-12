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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.DeletedRecord;
import com.sforce.soap.partner.GetDeletedResult;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetDeletedReader extends SalesforceGetDeletedUpdatedReader<GetDeletedResult> {

    protected int soqlCharacterLimit = 10000;

    protected List<String> queryStringList;

    private transient QueryResult inputResult;

    public SalesforceGetDeletedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    @Override
    protected String[] getRecordIds(GetDeletedResult result) {
        List<String> ids = new ArrayList<>();
        if (result != null) {
            DeletedRecord[] records = result.getDeletedRecords();
            for (DeletedRecord record : records) {
                ids.add(record.getId());
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    @Override
    protected GetDeletedResult getResult() throws IOException, ConnectionException {
        SalesforceGetDeletedUpdatedProperties props = (SalesforceGetDeletedUpdatedProperties) properties;
        return getConnection().getDeleted(module, startDate, endDate);
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
            while (!inputResult.isDone()) {
                inputResult = getConnection().queryMore(inputResult.getQueryLocator());
                inputRecords = ArrayUtils.addAll(inputRecords, inputResult.getRecords());
            }
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
                while (!inputResult.isDone()) {
                    inputResult = getConnection().queryMore(inputResult.getQueryLocator());
                    inputRecords = ArrayUtils.addAll(inputRecords, inputResult.getRecords());
                }
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
    protected List<String> getQueryStringList(GetDeletedResult result) throws IOException {
        String[] ids = getRecordIds(result);
        StringBuilder sb = new StringBuilder();
        List<String> queryStringList = new ArrayList<>();
        String baseQueryString = getQueryString(properties);
        sb.append(baseQueryString);
        sb.append(" where ");
        int recordSize = ids.length;
        String condition = "";
        for (int i = 0; i < recordSize; i++) {
            condition = "Id='" + ids[i] + "'";
            sb.append(condition);
            sb.append(" or ");
            if ((sb.length() + condition.length() > soqlCharacterLimit) || (i == (recordSize - 1))) {
                sb.delete(sb.length() - 4, sb.length());
                queryStringList.add(sb.toString());
                sb.delete(0, sb.length());
                if (i != (recordSize - 1)) {
                    sb.append(baseQueryString);
                    sb.append(" where ");
                }
            }
        }
        return queryStringList;
    }

}
