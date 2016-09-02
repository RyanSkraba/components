package org.talend.components.jdbc.runtime.type;

import java.sql.ResultSet;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.jdbc.runtime.type.JDBCAvroRegistry.JDBCConverter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class JDBCResultSetIndexedRecordConverter implements IndexedRecordConverter<ResultSet, IndexedRecord> {

    private Schema schema;

    private String names[];

    /** The cached AvroConverter objects for the fields of this record. */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Class<ResultSet> getDatumClass() {
        return ResultSet.class;
    }

    @Override
    public ResultSet convertToDatum(IndexedRecord value) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(ResultSet value) {
        return new ResultSetIndexedRecord(value);
    }

    private TJDBCInputProperties properties;

    public void setProperties(TJDBCInputProperties properties) {
        this.properties = properties;
    }

    private class ResultSetIndexedRecord implements IndexedRecord {

        private final ResultSet value;

        public ResultSetIndexedRecord(ResultSet value) {
            this.value = value;
        }

        @Override
        public Schema getSchema() {
            return JDBCResultSetIndexedRecordConverter.this.getSchema();
        }

        @Override
        public void put(int i, Object v) {
            throw new UnmodifiableAdapterException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object get(int i) {
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    JDBCConverter jdbcConverter = JDBCAvroRegistry.get().getConverter(f);

                    if (properties != null) {
                        jdbcConverter.setProperties(properties);
                    }

                    fieldConverter[j] = jdbcConverter;
                }
            }

            return fieldConverter[i].convertToAvro(value);
        }
    }

}
