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
import java.util.HashMap;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import com.sforce.ws.ConnectionException;
import org.talend.daikon.avro.util.AvroUtils;

public class SalesforceInputReader extends SalesforceReader {

    protected TSalesforceInputProperties properties;

    protected int commitLevel;

    public SalesforceInputReader(RuntimeContainer adaptor, SalesforceSource source, TSalesforceInputProperties props) {
        super(adaptor, source);
        properties = props;
        commitLevel = props.batchSize.getIntValue();
    }

    @Override
    public boolean start() throws IOException {
        super.start();
        Schema schema = source.getSchema(adaptor, properties.module.moduleName.getStringValue());
        fieldMap = AvroUtils.makeFieldMap(schema);
        fieldList = schema.getFields();

        for (Schema.Field se : fieldList) {
            if (AvroUtils.isDynamic(se.schema())) {
                dynamicField = se;
                break;
            }
        }

        connection.setQueryOptions(properties.batchSize.getIntValue());

        /*
         * Dynamic columns are requested, find them from Salesforce and only look at the ones that are not explicitly
         * specified in the schema.
         */
        if (dynamicField != null) {
            dynamicFieldMap = new HashMap<>();
            List<Schema.Field> filteredDynamicFields = new ArrayList<>();
            Schema dynSchema = schema;

            for (Schema.Field se : dynSchema.getFields()) {
                if (fieldMap.containsKey(se.name())) {
                    continue;
                }
                filteredDynamicFields.add(se);
                dynamicFieldMap.put(se.name(), se);
            }
            dynamicFieldList = filteredDynamicFields;
        }

        inputFieldsToUse = new ArrayList<>();
        for (Schema.Field s : fieldList) {
            if (AvroUtils.isDynamic(s.schema())) {
                continue;
            }
            inputFieldsToUse.add(s);
        }
        if (dynamicFieldList != null) {
            inputFieldsToUse.addAll(dynamicFieldList);
        }

        String queryText;
        if (properties.manualQuery.getBooleanValue()) {
            queryText = properties.query.getStringValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            int count = 0;
            for (Schema.Field se : inputFieldsToUse) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                sb.append(se.name());
            }
            sb.append(" from ");
            sb.append(properties.module.moduleName.getStringValue());
            queryText = sb.toString();
        }

        try {
            inputResult = connection.query(queryText);
        } catch (ConnectionException e) {
            throw new IOException(e);
        }

        inputRecords = inputResult.getRecords();
        inputRecordsIndex = 0;
        return inputResult.getSize() > 0;
    }
}
