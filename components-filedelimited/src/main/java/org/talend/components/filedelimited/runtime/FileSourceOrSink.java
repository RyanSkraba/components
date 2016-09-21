package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage("properties should be of type :" + FileDelimitedProperties.class.getCanonicalName());
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

    public static Schema getDynamicSchema(String[] columnsName, String schemaName) {
        if (columnsName != null) {
            String defaultValue = null;
            List<Schema.Field> fields = new ArrayList<>();
            for (String columnName : columnsName) {
                // TODO schema name can't be empty now. Specify a unique name ?
                Schema.Field avroField = new Schema.Field(columnName, AvroUtils._string(), null, defaultValue);
                avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(100));
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(0));
                fields.add(avroField);
            }
            return Schema.createRecord(schemaName, null, null, false, fields);
        } else {
            return Schema.createRecord(schemaName, null, null, false);
        }
    }

}
