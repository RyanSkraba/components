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
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.sforce.soap.partner.PartnerConnection;

public abstract class SalesforceReader<T> extends AbstractBoundedReader<T> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SalesforceReader.class);

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SalesforceReader.class);

    private transient PartnerConnection connection;

    private transient IndexedRecordConverter<?, IndexedRecord> factory;

    protected transient Schema querySchema;

    protected SalesforceConnectionModuleProperties properties;

    protected int dataCount;

    protected RuntimeContainer container;

    public SalesforceReader(RuntimeContainer container, SalesforceSource source) {
        super(source);
        this.container = container;
    }

    protected PartnerConnection getConnection() throws IOException {
        if (connection == null) {
            ConnectionHolder ch = ((SalesforceSource) getCurrentSource()).connect(container);
            connection = ch.connection;
            if (ch.bulkConnection != null) {
                LOGGER.info(MESSAGES.getMessage("info.bulkConnectionUsage"));
            }
        }
        return connection;
    }

    protected IndexedRecordConverter<?, IndexedRecord> getFactory() throws IOException {
        if (null == factory) {
            Schema schema = getSchema();
            boolean useBulkFactory = false;
            if (properties instanceof TSalesforceBulkExecProperties) {
                useBulkFactory = true;
            } else if (properties instanceof TSalesforceInputProperties) {
                if (TSalesforceInputProperties.QueryMode.Bulk
                        .equals(((TSalesforceInputProperties) properties).queryMode.getValue())) {
                    useBulkFactory = true;
                    if (((TSalesforceInputProperties) properties).returnNullValue.getValue()) {
                        schema.addProp(SalesforceSchemaConstants.RETURN_NULL_FOR_EMPTY, true);
                    }

                }
            }
            if (useBulkFactory) {
                factory = new BulkResultAdapterFactory();
            } else {
                factory = new SObjectAdapterFactory();
            }
            factory.setSchema(schema);
        }
        return factory;
    }

    protected Schema getSchema() throws IOException {
        if (querySchema == null) {
            querySchema = properties.module.main.schema.getValue();
            if (AvroUtils.isIncludeAllFields(querySchema)) {
                String moduleName = null;
                if (properties instanceof SalesforceConnectionModuleProperties) {
                    moduleName = properties.module.moduleName.getStringValue();
                }
                querySchema = getCurrentSource().getEndpointSchema(container, moduleName);
            }
        }
        return querySchema;
    }

    protected String getQueryString(SalesforceConnectionModuleProperties properties) throws IOException {
        String condition = null;
        if (properties instanceof TSalesforceInputProperties) {
            TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
            if (inProperties.manualQuery.getValue()) {
                return inProperties.query.getStringValue();
            } else {
                condition = inProperties.condition.getStringValue();
            }
        }
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
        sb.append(properties.module.moduleName.getStringValue());
        if (condition != null && condition.trim().length() > 0) {
            sb.append(" where ");
            sb.append(condition);
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        return result.toMap();
    }

}
