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
package org.talend.components.common.avro;

import java.sql.ResultSet;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

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

    private int sizeInResultSet;

    public void setSizeInResultSet(int sizeInResultSet) {
        this.sizeInResultSet = sizeInResultSet;
    }
    
    protected void resetSizeByResultSet(ResultSet resultSet) {
        //do nothing as default
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
            resetSizeByResultSet(resultSet);
            
            values = new Object[names.length];
            for (int i = 0; i < values.length; i++) {
                if ((sizeInResultSet > 0) && (i == sizeInResultSet)) {
                    break;
                }

                values[i] = fieldConverter[i].convertToAvro(resultSet);
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
