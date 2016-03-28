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
import org.talend.components.api.component.runtime.AbstractBoundedReader;

import com.sforce.soap.partner.PartnerConnection;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

public abstract class SalesforceReader<T> extends AbstractBoundedReader<T> {

    private transient PartnerConnection connection;

    private transient IndexedRecordAdapterFactory<?, IndexedRecord> factory;

    protected transient Schema querySchema;

    protected SalesforceConnectionModuleProperties properties;

    public SalesforceReader(RuntimeContainer container, SalesforceSource source) {
        super(container, source);
    }

    protected PartnerConnection getConnection() throws IOException {
        if (connection == null) {
            connection = ((SalesforceSource) getCurrentSource()).connect(container).connection;
        }
        return connection;
    }

    protected IndexedRecordAdapterFactory<?, IndexedRecord> getFactory() throws IOException {
        if (null == factory) {
            boolean useBulkFactory = false;
            if(properties instanceof TSalesforceBulkExecProperties){
                useBulkFactory = true;
            }else if(properties instanceof TSalesforceInputProperties){
                if(TSalesforceInputProperties.QUERY_BULK.equals(((TSalesforceInputProperties)properties).queryMode.getStringValue())){
                    useBulkFactory = true;
                }
            }
            if(useBulkFactory){
                factory = new BulkResultAdapterFactory();
            } else {
                factory = new SObjectAdapterFactory();
            }
            factory.setSchema(getSchema());
        }
        return factory;
    }

    protected Schema getSchema() throws IOException {
        if (null == querySchema) {
            querySchema = new Schema.Parser().parse(properties.module.schema.schema.getStringValue());
        }
        return querySchema;
    }

    protected String getQueryString(TSalesforceInputProperties inProperties) throws IOException {
        String queryText = "";
        if (inProperties.manualQuery.getBooleanValue()) {
            queryText = inProperties.query.getStringValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select "); //$NON-NLS-1$
            int count = 0;
            for (Schema.Field se : getSchema().getFields()) {
                if (count++ > 0) {
                    sb.append(", "); //$NON-NLS-1$
                }
                sb.append(se.name());
            }
            sb.append(" from "); //$NON-NLS-1$
            sb.append(inProperties.module.moduleName.getStringValue());
            String condition = inProperties.condition.getStringValue();
            if(condition!=null && condition.trim().length()>0){
                sb.append(" where ");
                sb.append(condition);
            }
            queryText = sb.toString();
        }
        return queryText;
    }
}
