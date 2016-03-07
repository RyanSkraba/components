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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.RuntimeHelper;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import com.sforce.ws.ConnectionException;

public class SalesforceInputReader extends SalesforceReader<IndexedRecord> {

    protected TSalesforceInputProperties properties;

    protected int commitLevel;

    transient private SObjectAdapterFactory factory = new SObjectAdapterFactory();

    public SalesforceInputReader(RuntimeContainer adaptor, SalesforceSource source, TSalesforceInputProperties props) {
        super(adaptor, source);
        properties = props;
        commitLevel = props.batchSize.getIntValue();
    }

    @Override
    public boolean start() throws IOException {
        super.start();
        Schema schema = RuntimeHelper.resolveSchema(adaptor, properties.module, getCurrentSource(),
                new Schema.Parser().parse(properties.module.schema.schema.getStringValue()));
        factory.setSchema(schema);
        fieldList = schema.getFields();

        connection.setQueryOptions(properties.batchSize.getIntValue());

        String queryText;
        if (properties.manualQuery.getBooleanValue()) {
            queryText = properties.query.getStringValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            int count = 0;
            for (Schema.Field se : fieldList) {
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

    @Override
    public IndexedRecord getCurrent() {
        return this.factory.convertToAvro(getCurrentSObject());
    }

}
