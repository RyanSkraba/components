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
package org.talend.components.azurestorage.table;

import java.util.Date;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public final class TableHelper {

    public static Record getRecord(int i) {
        Record record = new GenericData.Record(getWriteSchema());
        record.put("PartitionKey", "partition" + (i % 1000));
        record.put("RowKey", "row" + i);
        record.put("stringCol", "stringCol" + i);
        record.put("booleanColTrue", true);
        record.put("booleanColFalse", false);
        record.put("doubleCol", 2.0d);
        record.put("longCol", 3l);
        record.put("integerCol", i);
        record.put("byteCol", "imBytes".getBytes());
        record.put("dateCol", new Date());

        return record;
    }

    public static String generateRandomTableName() {
        String tableName = "table" + UUID.randomUUID().toString().toLowerCase();
        return tableName.replace("-", "");
    }

    public static Schema getWriteSchema() {

        return SchemaBuilder.record("Main")//
                .fields()//
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .name("stringCol").type(AvroUtils._string()).noDefault()//
                .name("booleanColTrue").type(AvroUtils._boolean()).noDefault()//
                .name("booleanColFalse").type(AvroUtils._boolean()).noDefault()//
                .name("doubleCol").type(AvroUtils._double()).noDefault()//
                .name("longCol").type(AvroUtils._long()).noDefault() //
                .name("integerCol").type(AvroUtils._int()).noDefault()//
                .name("byteCol").type(AvroUtils._bytes()).noDefault()// =
                .name("dateCol").type(AvroUtils._date()).noDefault()//
                .endRecord();
    }

    public static Schema getRejectSchema() {

        return SchemaBuilder.record("Reject")//
                .fields()//
                .name("errorCode").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .name("errorMessage").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .endRecord();
    }

    public static Schema getDynamicWriteSchema() {

        return SchemaBuilder.record("Main")//
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")//
                .fields() //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault() //
                .endRecord();
    }

}
