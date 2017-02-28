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

package org.talend.components.bigquery.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;

import com.google.api.services.bigquery.model.TableRow;

/**
 * Converter for convert {@link TableRow} to {@link IndexedRecord} {@link TableRow} does not contain predefined schema,
 * so need to set schema before calling convertToAvro/convertToDatum
 */
public class BigQueryTableRowIndexedRecordConverter extends BigQueryBaseIndexedRecordConverter<TableRow> {

    @Override
    public Class<TableRow> getDatumClass() {
        return TableRow.class;
    }

    @Override
    public TableRow convertToDatum(IndexedRecord indexedRecord) {
        // When BigQueryOutput do not specify schema, so read it from the incoming data
        if (schema == null) {
            schema = indexedRecord.getSchema();
            initFieldConverters();
        }

        TableRow row = new TableRow();
        for (Schema.Field field : schema.getFields()) {
            Object v = indexedRecord.get(field.pos());
            if (v != null) {
                row.set(field.name(), fieldConverters.get(field.name()).convertToDatum(v));
            }
        }
        return row;
    }

}
