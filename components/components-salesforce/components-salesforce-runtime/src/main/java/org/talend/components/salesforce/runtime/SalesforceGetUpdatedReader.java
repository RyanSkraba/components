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

import org.apache.avro.Schema;
import org.apache.commons.lang3.ArrayUtils;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;

import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.ws.ConnectionException;

public class SalesforceGetUpdatedReader extends SalesforceGetDeletedUpdatedReader<GetUpdatedResult> {

    protected List<String[]> idsList;

    private String fieldNamesStr;

    private static int MAXIMUM_RETRIEVE = 2000;

    public SalesforceGetUpdatedReader(RuntimeContainer adaptor, SalesforceSource source,
            SalesforceGetDeletedUpdatedProperties props) {
        super(adaptor, source, props);
    }

    @Override
    protected String[] getRecordIds(GetUpdatedResult result) {
        if (result != null) {
            return result.getIds();
        }
        return new String[0];
    }

    @Override
    protected GetUpdatedResult getResult() throws IOException, ConnectionException {
        return getConnection().getUpdated(module, startDate, endDate);
    }

    @Override
    public boolean start() throws IOException {
        try {
            result = getResult();
            idsList = splitIds(result);
            if (idsList.size() == 0) {
                return false;
            }
            inputRecords = getConnection().retrieve(getFieldNamesStr(), properties.module.moduleName.getValue(),
                    idsList.get(queryIndex++));
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
        if (queryIndex < idsList.size()) {
            try {
                inputRecords = getConnection().retrieve(getFieldNamesStr(), properties.module.moduleName.getValue(),
                        idsList.get(queryIndex++));
                inputRecordsIndex = 0;
                boolean isAdvanced = inputRecords.length > 0;
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
     * It can be passed a maximum of 2000 object IDs to the retrieve() call.
     * Need to split it into different arrays when objects are more than 2000
     */
    protected List<String[]> splitIds(GetUpdatedResult result) throws IOException {
        List<String[]> idsList = new ArrayList<>();
        String[] ids = getRecordIds(result);
        if (ids.length == 0) {
            return idsList;
        } else if (ids.length <= MAXIMUM_RETRIEVE) {
            idsList.add(ids);
        } else {
            int size = ids.length / MAXIMUM_RETRIEVE;
            if (ids.length % MAXIMUM_RETRIEVE != 0) {
                size += 1;
            }
            for (int i = 0; i < size; i++) {
                idsList.add(ArrayUtils.subarray(ids, i * MAXIMUM_RETRIEVE, (i + 1) * MAXIMUM_RETRIEVE));
            }
        }
        return idsList;
    }

    /**
     * Field list string of one or more fields in the specified object, separated by commas. We must specify valid field
     * names and must have read-level permissions to each specified field.
     */
    protected String getFieldNamesStr() throws IOException {
        if (fieldNamesStr == null) {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Schema.Field se : getSchema().getFields()) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                sb.append(se.name());
            }
            fieldNamesStr = sb.toString();
        }
        return fieldNamesStr;
    }

}
