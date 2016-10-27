package org.talend.components.common.avro;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.TalendRuntimeException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCResultSetIndexedRecordConverter implements IndexedRecordConverter<ResultSet, IndexedRecord> {

    private Schema schema;

    private String names[];

    /**
     * The cached AvroConverter objects for the fields of this record.
     */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }


    protected JDBCAvroRegistry getRegistry() {
        return JDBCAvroRegistry.get();
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
        names = new String[getSchema().getFields().size()];
        fieldConverter = new AvroConverter[names.length];
        for (int j = 0; j < names.length; j++) {
            Field f = getSchema().getFields().get(j);
            names[j] = f.name();
            JDBCAvroRegistry.JDBCConverter jdbcConverter = getRegistry().getConverter(f);
            if (influencer != null) {
                jdbcConverter.setInfluencer(influencer);
            }
            fieldConverter[j] = jdbcConverter;
        }
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

    private JDBCAvroRegistryInfluencer influencer;

    public void setInfluencer(JDBCAvroRegistryInfluencer influencer) {
        this.influencer = influencer;
    }

    private class ResultSetIndexedRecord implements IndexedRecord {

        private Object[] values;

        public ResultSetIndexedRecord(ResultSet resultSet) {
            try {
                values = new Object[resultSet.getMetaData().getColumnCount()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = fieldConverter[i].convertToAvro(resultSet);
                }
            } catch (SQLException e) {
                TalendRuntimeException.unexpectedException(e);
            }
        }

        @Override
        public Schema getSchema() {
            return JDBCResultSetIndexedRecordConverter.this.getSchema();
        }

        @Override
        public void put(int i, Object v) {
            throw new UnmodifiableAdapterException();
        }

        @Override
        public Object get(int i) {
            return values[i];
        }
    }

}
