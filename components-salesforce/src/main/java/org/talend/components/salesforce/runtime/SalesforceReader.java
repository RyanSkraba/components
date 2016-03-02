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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.ComponentDynamicHolder;
import org.talend.components.api.container.RuntimeContainer;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

public class SalesforceReader implements BoundedReader {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceReader.class);

    protected boolean exceptionForErrors;

    protected QueryResult inputResult;

    protected SObject[] inputRecords;

    protected int inputRecordsIndex;

    protected Map<String, Schema.Field> fieldMap;

    protected List<Schema.Field> fieldList;

    /*
     * Used on input only, this is read from the module schema, it contains all of the fields from the salesforce
     * definition of the module that are not already in the field list.
     */
    protected List<Schema.Field> dynamicFieldList;

    protected Map<String, Schema.Field> dynamicFieldMap;

    /*
     * The actual fields we read on input which is a combination of the fields specified in the schema and the dynamic
     * fields.
     */
    protected List<Schema.Field> inputFieldsToUse;

    /*
     * The dynamic column that is specified on the input schema.
     */
    protected Schema.Field dynamicField;

    protected PartnerConnection connection;

    protected SalesforceSource source;

    protected RuntimeContainer adaptor;

    public SalesforceReader(RuntimeContainer adaptor, SalesforceSource source) {
        this.source = source;
        this.adaptor = adaptor;
    }

    @Override
    public boolean start() throws IOException {
        connection = source.connect();
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
            columns.put(dynamicField.name(), dynamicHolder);
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
