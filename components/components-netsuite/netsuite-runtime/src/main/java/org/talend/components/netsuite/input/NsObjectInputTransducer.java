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
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.daikon.avro.AvroUtils;

/**
 *
 */
public class NsObjectInputTransducer extends NsObjectTransducer {
    private Schema schema;
    private Schema runtimeSchema;
    private String typeName;
    private TypeDesc typeDesc;

    public NsObjectInputTransducer(NetSuiteClientService<?> clientService, Schema schema, String typeName) {
        super(clientService);

        this.schema = schema;
        this.typeName = typeName;
    }

    public Schema getSchema() {
        return schema;
    }

    public IndexedRecord read(Object data) {
        prepare(data);

        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();
        Map<String, Object> mapView = getMapView(data, runtimeSchema, typeDesc);

        GenericRecord indexedRecord = new GenericData.Record(runtimeSchema);

        for (Schema.Field field : runtimeSchema.getFields()) {
            String fieldName = field.name();
            FieldDesc fieldDesc = fieldMap.get(fieldName);

            if (fieldDesc == null) {
                continue;
            }

            Object value = readField(mapView, fieldDesc);
            indexedRecord.put(fieldName, value);
        }

        return indexedRecord;
    }

    protected void prepare(Object nsObject) {
        if (runtimeSchema != null) {
            return;
        }

        if (AvroUtils.isIncludeAllFields(schema)) {
            TypeDesc typeDescByClass = clientService.getTypeInfo(nsObject.getClass());
            typeDesc = clientService.getTypeInfo(typeDescByClass.getTypeName());
            runtimeSchema = getDynamicSchema(typeDesc, schema, typeDesc.getTypeName());
        } else {
            typeDesc = clientService.getTypeInfo(typeName);
            runtimeSchema = schema;
        }
    }

}
