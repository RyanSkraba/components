package org.talend.components.jdbc.avro;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * One-way converter, which converts {@link ResultSet} to String {@link IndexedRecord}, i.e. each field in
 * the record will be of String type (also nullable in case appropriate specification schema field is nullable).
 */
public class ResultSetStringRecordConverter implements IndexedRecordConverter<ResultSet, IndexedRecord> {

    /**
     * Schema of outgoing IndexedRecord. 
     * All fields of this schema is of type String (nullable in case approprieate spec schema field is nullable)
     */
    private Schema actualSchema;

    @Override
    public Class<ResultSet> getDatumClass() {
        return ResultSet.class;
    }

    /**
     * Is not supported
     */
    @Override
    public ResultSet convertToDatum(IndexedRecord value) {
        throw new UnmodifiableAdapterException("not supported");
    }

    /**
     * Converts {@link ResultSet} to String {@link IndexedRecord} by retrieving each column value as a String.
     * {@link ResultSet#getString(int)} is used. It could be improved by using appropriate {@link AvroConverter} if necessary
     */
    @Override
    public IndexedRecord convertToAvro(ResultSet resultSet) {
        IndexedRecord record = new GenericData.Record(actualSchema);
        try {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnsNumber = metadata.getColumnCount();
            for (int i=0; i<columnsNumber; i++) {
                String columnValue = resultSet.getString(i+1);
                record.put(i, columnValue);
            }
        } catch (SQLException e) {
            // TODO replace it with something more meaningful then unexpected exception
            throw TalendRuntimeException.createUnexpectedException(e);
        }
        return record;
    }

    /**
     * Returns actual schema
     * Note, it is not equal to schema set by setSchema() method
     */
    @Override
    public Schema getSchema() {
        return actualSchema;
    }

    /**
     * Creates new actual schema from incoming specification <code>schema</code>
     * Actual schema fields has the same names as specification schema, but they have String type
     * Note, getSchema() will return schema, which differs from specification schema passed to this method
     */
    // TODO this one more kind of copySchema() method which should be implemented in Daikon see TDKN-96
    @Override
    public void setSchema(Schema schema) {
        actualSchema = Schema.createRecord(schema.getName(), schema.getDoc(), schema.getNamespace(), schema.isError());
        
        List<Schema.Field> stringFields = new ArrayList<>();
        for (Schema.Field specField : schema.getFields()) {
            boolean nullable = AvroUtils.isNullable(specField.schema());
            Schema stringSchema = AvroUtils._string();
            if (nullable) {
                stringSchema = AvroUtils.wrapAsNullable(stringSchema);
            }
            Schema.Field stringField = new Schema.Field(specField.name(), stringSchema, specField.doc(), specField.defaultVal(), specField.order());
            for (Map.Entry<String, Object> entry : specField.getObjectProps().entrySet()) {
                stringField.addProp(entry.getKey(), entry.getValue());
            }
            stringFields.add(stringField);
        }

        actualSchema.setFields(stringFields);
        for (Map.Entry<String, Object> entry : schema.getObjectProps().entrySet()) {
            actualSchema.addProp(entry.getKey(), entry.getValue());
        }
    }

}
