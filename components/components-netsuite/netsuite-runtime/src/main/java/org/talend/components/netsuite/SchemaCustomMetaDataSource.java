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

package org.talend.components.netsuite;

import static org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl.getNsFieldByName;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.netsuite.client.CustomMetaDataSource;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.daikon.avro.AvroUtils;

/**
 * Implementation of <code>CustomMetaDataSource</code> which uses Avro <code>schema</code>
 * as <code>source</code> of customization meta data.
 *
 * <p>
 * <code>SchemaCustomMetaDataSource</code> extracts customization meta data from a schema.
 * If meta data is present in schema then it is returned to a requester, otherwise
 * <code>SchemaCustomMetaDataSource</code> redirects a request to <code>default source</code>.
 *
 * <p>
 * If schema is <code>dynamic</code> then <code>SchemaCustomMetaDataSource</code> doesn't use
 * <code>schema</code> as source of meta data and redirects a request to <code>default source</code>.
 */
public class SchemaCustomMetaDataSource implements CustomMetaDataSource {
    protected BasicMetaData basicMetaData;
    protected CustomMetaDataSource defaultSource;
    protected Schema schema;

    /**
     * Create <code>SchemaCustomMetaDataSource</code> using given <code>basic meta data</code>,
     * <code>default source</code> and <code>schema</code>.
     *
     * @param basicMetaData basic meta data object
     * @param defaultSource default source to be used as secondary source meta data
     * @param schema schema to be used as primary source of meta data
     */
    public SchemaCustomMetaDataSource(BasicMetaData basicMetaData,
            CustomMetaDataSource defaultSource, Schema schema) {
        this.basicMetaData = basicMetaData;
        this.defaultSource = defaultSource;
        this.schema = schema;
    }

    @Override
    public Collection<CustomRecordTypeInfo> getCustomRecordTypes() {
        return defaultSource.getCustomRecordTypes();
    }

    @Override
    public Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo) {
        if (!AvroUtils.isIncludeAllFields(schema)) {
            Map<String, CustomFieldDesc> customFieldDescMap = new HashMap<>();
            for (Schema.Field field : schema.getFields()) {
                CustomFieldDesc customFieldDesc = NetSuiteDatasetRuntimeImpl.readCustomField(field);
                if (customFieldDesc != null) {
                    customFieldDescMap.put(customFieldDesc.getName(), customFieldDesc);
                }
            }
            return customFieldDescMap;
        }

        return defaultSource.getCustomFields(recordTypeInfo);
    }

    @Override
    public CustomRecordTypeInfo getCustomRecordType(String typeName) {
        Schema.Field keyField = getNsFieldByName(schema, "internalId");
        if (keyField != null) {
            CustomRecordTypeInfo customRecordTypeInfo =
                    NetSuiteDatasetRuntimeImpl.readCustomRecord(basicMetaData, keyField);
            if (customRecordTypeInfo != null) {
                return customRecordTypeInfo;
            }
        }

        return defaultSource.getCustomRecordType(typeName);
    }
}
