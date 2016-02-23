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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.adaptor.ComponentDynamicHolder;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

public class SalesforceInputReader implements BoundedReader {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceInputReader.class);

    protected TSalesforceInputProperties properties;

    protected boolean exceptionForErrors;

    protected int commitLevel;

    protected QueryResult inputResult;

    protected SObject[] inputRecords;

    protected int inputRecordsIndex;

    protected Map<String, SchemaElement> fieldMap;

    protected List<SchemaElement> fieldList;

    /*
     * Used on input only, this is read from the module schema, it contains all of the fields from the salesforce
     * definition of the module that are not already in the field list.
     */
    protected List<SchemaElement> dynamicFieldList;

    protected Map<String, SchemaElement> dynamicFieldMap;

    /*
     * The actual fields we read on input which is a combination of the fields specified in the schema and the dynamic
     * fields.
     */
    protected List<SchemaElement> inputFieldsToUse;

    /*
     * The dynamic column that is specified on the input schema.
     */
    protected SchemaElement dynamicField;

    protected PartnerConnection connection;

    protected SalesforceSource source;

    protected Adaptor adaptor;

    public SalesforceInputReader(Adaptor adaptor, SalesforceSource source, TSalesforceInputProperties props) {
        this.source = source;
        this.adaptor = adaptor;
        properties = props;
        commitLevel = props.batchSize.getIntValue();
    }

    @Override
    public boolean start() throws IOException {
        connection = source.connect();
        Schema schema = source.getSchema(adaptor, properties.module.moduleName.getStringValue());
        fieldMap = schema.getRoot().getChildMap();
        fieldList = schema.getRoot().getChildren();

        for (SchemaElement se : fieldList) {
            if (se.getType() == SchemaElement.Type.DYNAMIC) {
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
            List<SchemaElement> filteredDynamicFields = new ArrayList<>();
            Schema dynSchema = schema;

            for (SchemaElement se : dynSchema.getRoot().getChildren()) {
                if (fieldMap.containsKey(se.getName())) {
                    continue;
                }
                filteredDynamicFields.add(se);
                dynamicFieldMap.put(se.getName(), se);
            }
            dynamicFieldList = filteredDynamicFields;
        }

        inputFieldsToUse = new ArrayList<>();
        for (SchemaElement s : fieldList) {
            if (s.getType() == SchemaElement.Type.DYNAMIC) {
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
            for (SchemaElement se : inputFieldsToUse) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                sb.append(se.getName());
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
        ComponentDynamicHolder dynamicHolder = null;
        if (dynamicFieldMap != null) {
            dynamicHolder = adaptor.createDynamicHolder();
            dynamicHolder.setSchemaElements(dynamicFieldList);
        }
        Iterator<XmlObject> it = inputRecords[inputRecordsIndex].getChildren();
        Map<String, Object> columns = new HashMap<>();
        while (it.hasNext()) {
            XmlObject obj = it.next();
            String localName = obj.getName().getLocalPart();
            if (dynamicFieldMap != null && dynamicFieldMap.get(localName) != null) {
                dynamicHolder.addFieldValue(localName, obj.getValue());
            } else {
                columns.put(localName, obj.getValue());
            }
        }
        if (dynamicHolder != null) {
            columns.put(dynamicField.getName(), dynamicHolder);
        }
        return columns;
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Double getFractionConsumed() {
        return null;
    }

    @Override
    public BoundedSource getCurrentSource() {
        return source;
    }

    @Override
    public BoundedSource splitAtFraction(double fraction) {
        return null;
    }

}
