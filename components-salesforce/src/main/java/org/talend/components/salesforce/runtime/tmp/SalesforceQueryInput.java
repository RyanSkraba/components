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
package org.talend.components.salesforce.runtime.tmp;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.salesforce.runtime.SObjectAdapterFactory;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;

/**
 * 
 */
public class SalesforceQueryInput extends SalesforceUnshardedInputBase {

    private final boolean manualQuery;

    private final String query;

    private final String moduleName;

    private final Schema querySchema;

    private final SObjectAdapterFactory factory;

    public SalesforceQueryInput(TSalesforceInputProperties properties, Schema querySchema) {
        super(properties.getConnectionProperties());
        this.manualQuery = properties.manualQuery.getBooleanValue();
        this.query = properties.query.getStringValue();
        this.moduleName = properties.module.moduleName.getStringValue();
        this.querySchema = querySchema;
        this.factory = new SObjectAdapterFactory();
        this.factory.setSchema(querySchema);
    }

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    protected QueryResult executeSalesforceQuery() throws ConnectionException {
        String queryText;
        if (manualQuery) {
            queryText = query;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select "); //$NON-NLS-1$
            int count = 0;
            for (Field se : querySchema.getFields()) {
                if (count++ > 0) {
                    sb.append(", "); //$NON-NLS-1$
                }
                sb.append(se.name());
            }
            sb.append(" from "); //$NON-NLS-1$
            sb.append(moduleName);
            queryText = sb.toString();
        }

        return connection.query(queryText);
    }

    @Override
    public IndexedRecord next() {
        return this.factory.convertToAvro(nextSObjectToWrap());
    }
}
