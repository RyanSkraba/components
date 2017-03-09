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
package org.talend.components.azurestorage.table.avro;

import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.table.avro.AzureStorageAvroRegistry.DTEConverter;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.microsoft.azure.storage.table.DynamicTableEntity;

public class AzureStorageTableAdaptorFactory implements IndexedRecordConverter<DynamicTableEntity, IndexedRecord> {

    private Schema schema;

    private Map<String, String> nameMappings;

    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageTableAdaptorFactory.class);

    public AzureStorageTableAdaptorFactory(Map<String, String> nameMappings) {
        this.nameMappings = nameMappings;
    }

    @Override
    public Class<DynamicTableEntity> getDatumClass() {
        return DynamicTableEntity.class;
    }

    @Override
    public DynamicTableEntity convertToDatum(IndexedRecord value) {
        return null;
    }

    @Override
    public AzureStorageTableIndexedRecord convertToAvro(DynamicTableEntity value) {
        return new AzureStorageTableIndexedRecord(value, getSchema());
    }

    @Override
    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
        Boolean useNameMappings = (nameMappings != null);
        String[] names;
        names = new String[getSchema().getFields().size()];
        fieldConverter = new AvroConverter[names.length];
        for (int j = 0; j < names.length; j++) {
            Field f = getSchema().getFields().get(j);
            String mappedName = f.name();
            if (useNameMappings && nameMappings.containsKey(f.name())) {
                mappedName = nameMappings.get(f.name());
                // LOGGER.warn("AdaptorFactory: {} <---> {}.", f.name(), mappedName);
            }
            names[j] = f.name();
            DTEConverter converter = AzureStorageAvroRegistry.get().getConverter(f, mappedName);
            fieldConverter[j] = converter;
        }
    }

    private class AzureStorageTableIndexedRecord implements IndexedRecord {

        private Object[] values;

        @SuppressWarnings("unchecked")
        public AzureStorageTableIndexedRecord(DynamicTableEntity record, Schema schema) {
            try {
                values = new Object[schema.getFields().size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = fieldConverter[i].convertToAvro(record);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
                throw new ComponentException(e);
            }
        }

        @Override
        public Schema getSchema() {
            return AzureStorageTableAdaptorFactory.this.getSchema();
        }

        @Override
        public void put(int paramInt, Object paramObject) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(int idx) {
            return values[idx];
        }
    }

}
