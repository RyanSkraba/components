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

import com.sforce.ws.ConnectionException;
import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.RuntimeHelper;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import java.io.IOException;

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
        Schema schema = RuntimeHelper.resolveSchema(adaptor, properties.module, getCurrentSource(),
                (Schema) properties.module.schema.schema.getValue());
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
}
