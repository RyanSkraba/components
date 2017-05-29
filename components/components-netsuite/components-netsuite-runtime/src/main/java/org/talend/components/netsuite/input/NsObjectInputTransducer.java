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

package org.talend.components.netsuite.input;

import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.components.netsuite.SchemaCustomMetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.daikon.avro.AvroUtils;

/**
 * Responsible for translating of input NetSuite record to {@code IndexedRecord} according to schema.
 */
public class NsObjectInputTransducer extends NsObjectTransducer {

    /** Design schema for indexed record. */
    private Schema schema;

    /** Actual schema for indexed record. */
    private Schema runtimeSchema;

    /** Name of NetSuite record type. */
    private String typeName;

    /** Descriptor of NetSuite data model object. */
    private TypeDesc typeDesc;

    public NsObjectInputTransducer(NetSuiteClientService<?> clientService, Schema schema, String typeName) {
        super(clientService);

        this.schema = schema;
        this.typeName = typeName;
    }

    public Schema getSchema() {
        return schema;
    }

    /**
     * Translate NetSuite data model object to {@code IndexedRecord}.
     *
     * @param data NetSuite data object
     * @return indexed record
     */
    public IndexedRecord read(Object data) {
        prepare(data);

        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();
        Map<String, Object> mapView = getMapView(data, runtimeSchema, typeDesc);

        GenericRecord indexedRecord = new GenericData.Record(runtimeSchema);

        for (Schema.Field field : runtimeSchema.getFields()) {
            String nsFieldName = NetSuiteDatasetRuntimeImpl.getNsFieldName(field);

            FieldDesc fieldDesc = fieldMap.get(nsFieldName);
            if (fieldDesc == null) {
                continue;
            }

            Object value = readField(mapView, fieldDesc);

            indexedRecord.put(field.name(), value);
        }

        return indexedRecord;
    }

    /**
     * Prepare processing of data object.
     *
     * @param nsObject data object to be processed
     */
    private void prepare(Object nsObject) {
        if (runtimeSchema != null) {
            return;
        }

        if (AvroUtils.isIncludeAllFields(schema)) {
            // It's dynamic schema, we should use dynamic schema as runtime schema.

            TypeDesc typeDescByClass = metaDataSource.getTypeInfo(nsObject.getClass());
            typeDesc = metaDataSource.getTypeInfo(typeDescByClass.getTypeName());
            runtimeSchema = getDynamicSchema(typeDesc, schema, typeDesc.getTypeName());

            // Replace custom meta data source with SchemaCustomMetaDataSource
            // which will be using new runtime schema
            SchemaCustomMetaDataSource schemaCustomMetaDataSource = new SchemaCustomMetaDataSource(
                    clientService.getBasicMetaData(),
                    clientService.getMetaDataSource().getCustomMetaDataSource(),
                    runtimeSchema);
            metaDataSource.setCustomMetaDataSource(schemaCustomMetaDataSource);
        } else {
            typeDesc = metaDataSource.getTypeInfo(typeName);
            // Use design schema as runtime schema
            runtimeSchema = schema;
        }
    }

}
