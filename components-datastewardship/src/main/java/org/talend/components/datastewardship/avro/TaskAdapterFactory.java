// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class TaskAdapterFactory implements IndexedRecordConverter<JSONObject, IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskAdapterFactory.class);

    private Schema schema;

    @Override
    public Class<JSONObject> getDatumClass() {
        return JSONObject.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject convertToDatum(IndexedRecord value) {
        JSONObject recordObj = new JSONObject();
        for (Schema.Field f : schema.getFields()) {
            Object obj = value.get(f.pos());
            recordObj.put(f.name(), obj);
        }
        return recordObj;
    }

    @Override
    public IndexedRecord convertToAvro(JSONObject value) {
        return new TaskIndexedRecord(value);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    private class TaskIndexedRecord implements IndexedRecord {

        private final JSONObject record;

        public TaskIndexedRecord(JSONObject record) {
            this.record = record;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Record: \n" + record); //$NON-NLS-1$
            }
        }

        @Override
        public Schema getSchema() {
            return TaskAdapterFactory.this.getSchema();
        }

        @Override
        public void put(int i, Object v) {
            throw new UnmodifiableAdapterException();
        }

        @Override
        public Object get(int i) {
            Schema.Field field = this.getSchema().getFields().get(i);
            Schema.Type type = field.schema().getType();
            Object value = record.get(field.name());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Name=" + field.name() + ", Type=" + type + ",Value=" + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            // The value in JSON may not match its type, need to cast explicitly
            if (value != null && (Schema.Type.INT == type || Schema.Type.LONG == type || Schema.Type.DOUBLE == type)) {
                try {
                    if (Schema.Type.INT == type) {
                        return Integer.parseInt(value.toString());
                    } else if (Schema.Type.LONG == type) {
                        return Long.parseLong(value.toString());
                    } else {
                        return Double.parseDouble(value.toString());
                    }
                } catch (NumberFormatException e) {
                    LOG.warn(value + " cannot be casted.", e); //$NON-NLS-1$
                    return null;
                }
            }
            return value;
        }
    }
}
