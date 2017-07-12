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
package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;

public class FileSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = 1L;

    protected ComponentProperties properties;

    private transient Schema schema;

    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer adaptor) {
        if (!(properties instanceof FileDelimitedProperties)) {
            return new ValidationResult(ValidationResult.Result.ERROR,
                    "properties should be of type :" + FileDelimitedProperties.class.getCanonicalName());
        }
        return ValidationResult.OK;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    // "columnsName" is retrieved columns name, it maybe smaller than columnsLength size
    // So we need add some default named columns "Column"+ columnIndex
    public static Schema getSchema(String schemaName, List<String> columnsName, List<Integer> columnsLength) {
        if (columnsLength == null && columnsName != null && columnsName.size() > 0) {
            columnsLength = new ArrayList<>();
            for (String columnName : columnsName) {
                columnsLength.add(255);
            }
        }
        if (columnsLength != null) {
            List<Schema.Field> fields = new ArrayList<>();
            int fieldsSize = columnsLength.size();
            String defaultValue = null;
            for (int columnIndex = 0; columnIndex < fieldsSize; columnIndex++) {
                int columnLength = columnsLength.get(columnIndex);
                String columnName = null;
                if (columnsName != null && columnIndex < columnsName.size()) {
                    columnName = columnsName.get(columnIndex);
                } else {
                    columnName = "Column" + columnIndex;
                }
                // TODO guess data type
                Schema.Field avroField = new Schema.Field(columnName, AvroUtils._string(), null, defaultValue);
                if (columnLength != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(columnLength));
                }
                fields.add(avroField);
            }
            return Schema.createRecord(schemaName, null, null, false, fields);
        } else {
            return SchemaProperties.EMPTY_SCHEMA;
        }
    }

    /**
     * Get dynamic schema base on the file and design schema
     * 
     * @param columnsName All columns name which read from file
     * @param schemaName Specify the schema name
     * @param designSchema The design schema of current component
     * @return The build schema include all fields
     */
    public static Schema getDynamicSchema(String[] columnsName, String schemaName, Schema designSchema) {
        if (columnsName != null && columnsName.length > 0) {
            // FIXME can we use "DiSchemaConstants" here?
            String dynamicPosProp = designSchema.getProp("di.dynamic.column.position");
            List<Schema.Field> fields = new ArrayList<>();
            if (dynamicPosProp != null) {
                int dynPos = Integer.parseInt(dynamicPosProp);
                int dynamicColumnSize = columnsName.length - designSchema.getFields().size();
                String defaultValue = null;
                if (designSchema.getFields().size() > 0) {
                    for (Schema.Field field : designSchema.getFields()) {
                        // Dynamic column is first or middle column in design schema
                        if (dynPos == field.pos()) {
                            for (int i = 0; i < dynamicColumnSize; i++) {
                                // Add dynamic schema fields
                                fields.add(getDefaultField(columnsName[i + dynPos], defaultValue));
                            }
                        }
                        // Add fields of design schema
                        Schema.Field avroField = new Schema.Field(field.name(), field.schema(), null, field.defaultVal());
                        Map<String, Object> fieldProps = field.getObjectProps();
                        for (String propName : fieldProps.keySet()) {
                            Object propValue = fieldProps.get(propName);
                            if (propValue != null) {
                                avroField.addProp(propName, propValue);
                            }
                        }
                        fields.add(avroField);
                        // Dynamic column is last column in design schema
                        if (field.pos() == (designSchema.getFields().size() - 1) && dynPos == (field.pos() + 1)) {
                            for (int i = 0; i < dynamicColumnSize; i++) {
                                // Add dynamic schema fields
                                fields.add(getDefaultField(columnsName[i + dynPos], defaultValue));
                            }
                        }
                    }
                } else {
                    // All fields are included in dynamic schema
                    for (String columnName : columnsName) {
                        fields.add(getDefaultField(columnName, defaultValue));
                    }
                }
            }
            Schema schema = Schema.createRecord(schemaName, null, null, false, fields);
            return schema;
        } else {
            return Schema.createRecord(schemaName, null, null, false);
        }
    }

    /**
     * Created default field for dynamic,default Length and Precision keep same with old behavior
     */
    protected static Schema.Field getDefaultField(String columnName, String defaultValue) {
        Schema.Field avroField = new Schema.Field(columnName, AvroUtils._string(), null, defaultValue);
        avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(100));
        avroField.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(0));
        return avroField;
    }
}
