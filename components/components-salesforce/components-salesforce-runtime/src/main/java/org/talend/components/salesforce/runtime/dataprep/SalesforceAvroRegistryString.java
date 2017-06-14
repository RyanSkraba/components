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
package org.talend.components.salesforce.runtime.dataprep;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.salesforce.runtime.SalesforceSchemaConstants;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.java8.SerializableFunction;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * avro registry for dataprep platform
 */
public class SalesforceAvroRegistryString extends AvroRegistry {

    private static final SalesforceAvroRegistryString sInstance = new SalesforceAvroRegistryString();

    public static SalesforceAvroRegistryString get() {
        return sInstance;
    }

    private SalesforceAvroRegistryString() {

        registerSchemaInferrer(DescribeSObjectResult.class, new SerializableFunction<DescribeSObjectResult, Schema>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(DescribeSObjectResult t) {
                return inferSchemaDescribeSObjectResult(t);
            }

        });

    }

    private Schema inferSchemaDescribeSObjectResult(DescribeSObjectResult in) {
        List<Schema.Field> fields = new ArrayList<>();
        for (Field field : in.getFields()) {
            // filter the invalud compound columns for salesforce bulk query api
            if (field.getType() == FieldType.address || field.getType() == FieldType.location) {
                continue;
            }

            Schema.Field avroField = new Schema.Field(field.getName(), salesforceField2AvroTypeSchema(field), null,
                    field.getDefaultValueFormula());

            Schema avroFieldSchema = avroField.schema();
            if (avroFieldSchema.getType() == Schema.Type.UNION) {
                for (Schema schema : avroFieldSchema.getTypes()) {
                    if (avroFieldSchema.getType() != Schema.Type.NULL) {
                        avroFieldSchema = schema;
                        break;
                    }
                }
            }
            if (AvroUtils.isSameType(avroFieldSchema, AvroUtils._string())) {
                if (field.getLength() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(field.getLength()));
                }
                if (field.getPrecision() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(field.getPrecision()));
                }
            } else {
                if (field.getPrecision() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(field.getPrecision()));
                }
                if (field.getScale() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(field.getScale()));
                }
            }

            if (field.getReferenceTo() != null && field.getReferenceTo().length > 0 && field.getRelationshipName() != null) {
                avroField.addProp(SalesforceSchemaConstants.REF_MODULE_NAME, field.getReferenceTo()[0]);
                avroField.addProp(SalesforceSchemaConstants.REF_FIELD_NAME, field.getRelationshipName());
            }

            switch (field.getType()) {
            case date:
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd");
                break;
            case datetime:
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
                break;
            case time:
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss.SSS'Z'");
                break;
            default:
                break;
            }
            if (avroField.defaultVal() != null) {
                avroField.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, String.valueOf(avroField.defaultVal()));
            }
            fields.add(avroField);
        }
        return Schema.createRecord(in.getName(), null, null, false, fields);
    }

    private Schema salesforceField2AvroTypeSchema(Field field) {
        Schema base = AvroUtils._string();
        return field.getNillable() ? AvroUtils.wrapAsNullable(base) : base;
    }

    public AvroConverter<String, ?> getConverterFromString(org.apache.avro.Schema.Field f) {
        return super.getConverter(String.class);
    }

}
