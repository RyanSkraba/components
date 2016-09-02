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

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.datastewardship.common.CampaignDetail.RecordField;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.di.DiSchemaConstants;
import org.talend.daikon.java8.SerializableFunction;

public class CampaignAvroRegistry extends AvroRegistry {

    private static CampaignAvroRegistry campaignInstance;

    private CampaignAvroRegistry() {

        // Ensure that we know how to get Schemas for these DataPrep objects.
        registerSchemaInferrer(RecordField[].class, new SerializableFunction<RecordField[], Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(RecordField[] t) {
                return inferSchemaTdsCampaignResult(t);
            }

        });

        registerSchemaInferrer(RecordField.class, new SerializableFunction<RecordField, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(RecordField t) {
                return inferSchemaField(t);
            }

        });
    }

    public static CampaignAvroRegistry getCampaignInstance() {
        if (campaignInstance == null) {
            campaignInstance = new CampaignAvroRegistry();
        }
        return campaignInstance;
    }

    @SuppressWarnings("deprecation")
    private Schema inferSchemaTdsCampaignResult(RecordField[] in) {
        List<Schema.Field> fields = new ArrayList<>();
        for (RecordField field : in) {
            Schema.Field avroField = new Schema.Field(field.getName(), inferSchemaField(field), null, null);
            if (field.isReadonly()) {
                avroField.addProp(SchemaConstants.TALEND_IS_LOCKED, Boolean.TRUE.toString());
            }
            if (field.isMandatory()) {
                avroField.addProp(DiSchemaConstants.TALEND6_COLUMN_IS_NULLABLE, false);
            }
            fields.add(avroField);
        }
        return Schema.createRecord("main", null, null, false, fields); //$NON-NLS-1$
    }

    // Reference FieldType.java of https://github.com/Talend/data-stewardship
    private Schema inferSchemaField(RecordField field) {
        Schema base;
        switch (field.getType()) {
        case "integer": //$NON-NLS-1$
            base = AvroUtils._int();
            break;
        case "decimal": //$NON-NLS-1$
            base = AvroUtils._double();
            break;
        case "date": //$NON-NLS-1$
            base = AvroUtils._int();
            break;
        case "timestamp": //$NON-NLS-1$
            base = AvroUtils._long();
            break;
        case "boolean": //$NON-NLS-1$
            base = AvroUtils._boolean();
            break;
        case "time": //$NON-NLS-1$
            base = AvroUtils._int();
            break;
        default:
            base = AvroUtils._string();
            break;
        }
        return base;
    }
}
