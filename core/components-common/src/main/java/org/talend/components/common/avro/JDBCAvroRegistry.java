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

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.codehaus.jackson.JsonNode;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter.UnmodifiableAdapterException;
import org.talend.daikon.java8.SerializableFunction;

public class JDBCAvroRegistry extends AvroRegistry {

    private static final JDBCAvroRegistry sInstance = new JDBCAvroRegistry();

    public static JDBCAvroRegistry get() {
        return sInstance;
    }

    protected JDBCAvroRegistry() {

        registerSchemaInferrer(JDBCTableMetadata.class, new SerializableFunction<JDBCTableMetadata, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(JDBCTableMetadata t) {
                try {
                    return inferSchemaResultSet(t);
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }
            }

        });

        registerSchemaInferrer(ResultSetMetaData.class, new SerializableFunction<ResultSetMetaData, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(ResultSetMetaData t) {
                try {
                    return inferSchemaResultSetMetaData(t);
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }
            }

        });
    }

    protected Schema inferSchemaResultSetMetaData(ResultSetMetaData metadata) throws SQLException {
        List<Field> fields = new ArrayList<>();

        int count = metadata.getColumnCount();
        for (int i = 1; i <= count; i++) {
            int size = metadata.getPrecision(i);
            int scale = metadata.getScale(i);
            boolean nullable = ResultSetMetaData.columnNullable == metadata.isNullable(i);

            int dbtype = metadata.getColumnType(i);
            String fieldName = metadata.getColumnLabel(i);
            String dbColumnName = metadata.getColumnName(i);

            // not necessary for the result schema from the query statement
            boolean isKey = false;

            Field field = sqlType2Avro(size, scale, dbtype, nullable, fieldName, dbColumnName, null, isKey);

            fields.add(field);
        }

        return Schema.createRecord("DYNAMIC", null, null, false, fields);
    }

    protected Schema inferSchemaResultSet(JDBCTableMetadata tableMetadata) throws SQLException {
        DatabaseMetaData databaseMetdata = tableMetadata.getDatabaseMetaData();

        Set<String> keys = getPrimaryKeys(databaseMetdata, tableMetadata.getCatalog(), tableMetadata.getDbSchema(),
                tableMetadata.getTablename());

        try (ResultSet metadata = databaseMetdata.getColumns(tableMetadata.getCatalog(), tableMetadata.getDbSchema(),
                tableMetadata.getTablename(), null)) {
            if (!metadata.next()) {
                return null;
            }

            List<Field> fields = new ArrayList<>();
            String tablename = metadata.getString("TABLE_NAME");

            do {
                int size = metadata.getInt("COLUMN_SIZE");
                int scale = metadata.getInt("DECIMAL_DIGITS");
                int dbtype = metadata.getInt("DATA_TYPE");
                boolean nullable = DatabaseMetaData.columnNullable == metadata.getInt("NULLABLE");

                String columnName = metadata.getString("COLUMN_NAME");
                boolean isKey = keys.contains(columnName);

                String defaultValue = metadata.getString("COLUMN_DEF");

                Field field = sqlType2Avro(size, scale, dbtype, nullable, columnName, columnName, defaultValue, isKey);

                fields.add(field);
            } while (metadata.next());

            return Schema.createRecord(tablename, null, null, false, fields);
        }
    }

    private Set<String> getPrimaryKeys(DatabaseMetaData databaseMetdata, String catalogName, String schemaName, String tableName)
            throws SQLException {
        Set<String> result = new HashSet<>();

        try (ResultSet resultSet = databaseMetdata.getPrimaryKeys(catalogName, schemaName, tableName)) {
            if (resultSet != null) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        }

        return result;
    }

    protected Field sqlType2Avro(int size, int scale, int dbtype, boolean nullable, String name, String dbColumnName,
            Object defaultValue, boolean isKey) {
        Field field = null;
        Schema schema = null;

        switch (dbtype) {
        case java.sql.Types.VARCHAR:
            schema = AvroUtils._string();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, size);
            break;
        case java.sql.Types.INTEGER:
            schema = AvroUtils._int();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.DECIMAL:
            schema = AvroUtils._decimal();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            field.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
            break;
        case java.sql.Types.BIGINT:
            schema = AvroUtils._long();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.NUMERIC:
            schema = AvroUtils._decimal();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            field.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
            break;
        case java.sql.Types.TINYINT:
            schema = AvroUtils._byte();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.DOUBLE:
            schema = AvroUtils._double();
            field = wrap(nullable, schema, name);
            break;
        case java.sql.Types.FLOAT:
            schema = AvroUtils._float();
            field = wrap(nullable, schema, name);
            break;
        case java.sql.Types.DATE:
            schema = AvroUtils._date();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd");
            break;
        case java.sql.Types.TIME:
            schema = AvroUtils._date();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss");
            break;
        case java.sql.Types.TIMESTAMP:
            schema = AvroUtils._date();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd HH:mm:ss.SSS");
            break;
        case java.sql.Types.BOOLEAN:
            schema = AvroUtils._boolean();
            field = wrap(nullable, schema, name);
            break;
        case java.sql.Types.REAL:
            schema = AvroUtils._float();
            field = wrap(nullable, schema, name);
            break;
        case java.sql.Types.SMALLINT:
            schema = AvroUtils._short();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.LONGVARCHAR:
            schema = AvroUtils._string();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, size);
            break;
        case java.sql.Types.CHAR:
            schema = AvroUtils._string();
            field = wrap(nullable, schema, name);
            field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, size);
            break;
        default:
            schema = AvroUtils._string();
            field = wrap(nullable, schema, name);
            break;
        }

        field.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, dbtype);
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, dbColumnName);

        if (defaultValue != null) {
            field.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, defaultValue);
        }

        if (isKey) {
            field.addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");
        }

        return field;
    }

    protected Field wrap(boolean nullable, Schema base, String name) {
        Schema schema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;
        return new Field(name, schema, null, (JsonNode) null);
    }

    public JDBCConverter getConverter(final Field f, final int index) {
        Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());

        if (AvroUtils.isSameType(basicSchema, AvroUtils._string())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    boolean trim = isTrim() || isTrim(index);
                    try {
                        String result = value.getString(index);

                        if (trim && result != null) {
                            return result.trim();
                        }

                        return result;
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._int())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getInt(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._date())) {// no date type in AVRO types, so we replace it by long
            // type
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    java.util.Date date = null;

                    try {
                        date = value.getTimestamp(index);
                    } catch (SQLException e1) {
                        try {
                            date = value.getDate(index);
                        } catch (SQLException e2) {
                            throw new ComponentException(e2);
                        }
                    }

                    if (date == null) {
                        return null;
                    }
                    return date.getTime();
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._decimal()))

        {// TODO why we use big decimal type though AVRO types
         // don't contain it? No need to consider the
         // serialization? But we do it for date type above
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getBigDecimal(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getLong(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._double())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getDouble(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._float())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getFloat(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._boolean())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getBoolean(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._short())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getShort(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._character())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    boolean trim = isTrim() || isTrim(index);
                    try {
                        String result = value.getString(index);

                        if (trim && result != null) {
                            return result.trim();
                        }

                        if (result == null || result.isEmpty()) {
                            return null;
                        }

                        return result.charAt(0);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._byte())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getByte(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (isObject(basicSchema)) {
            return new JDBCConverter() {
  
              @Override
              public Object convertToAvro(ResultSet value) {
                  try {
                      return value.getObject(index);
                  } catch (SQLException e) {
                      throw new ComponentException(e);
                  }
              }
  
            };
        } else {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    boolean trim = isTrim() || isTrim(index);
                    try {
                        String result = value.getString(index);

                        if (trim && result != null) {
                            return result.trim();
                        }

                        return result;
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        }
    }

    public JDBCConverter getConverter(final Field f) {
        return getConverter(f, f.pos() + 1);
    }

    public abstract class JDBCConverter implements AvroConverter<ResultSet, Object> {

        protected JDBCAvroRegistryInfluencer influencer;

        @Override
        public Schema getSchema() {
            // do nothing
            return null;
        }

        @Override
        public Class<ResultSet> getDatumClass() {
            // do nothing
            return null;
        }

        @Override
        public ResultSet convertToDatum(Object value) {
            throw new UnmodifiableAdapterException();
        }

        protected boolean isTrim() {
            if (influencer != null)
                return influencer.trim();
            return false;
        }

        protected boolean isTrim(int index) {
            if (influencer != null)
                return influencer.isTrim(index);
            return false;
        }

        public void setInfluencer(JDBCAvroRegistryInfluencer influencer) {
            this.influencer = influencer;
        }

    }

    public abstract class JDBCSPConverter implements AvroConverter<CallableStatement, Object> {

        protected JDBCAvroRegistryInfluencer influencer;

        @Override
        public Schema getSchema() {
            // do nothing
            return null;
        }

        @Override
        public Class<CallableStatement> getDatumClass() {
            // do nothing
            return null;
        }

        @Override
        public CallableStatement convertToDatum(Object value) {
            throw new UnmodifiableAdapterException();
        }

    }

    public JDBCSPConverter getSPConverter(final Field f, final int index) {
        Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());

        if (AvroUtils.isSameType(basicSchema, AvroUtils._string())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        String result = value.getString(index);

                        return result;
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._int())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getInt(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._date())) {// no date type in AVRO types, so we replace it by long
            // type
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    java.util.Date date = null;

                    try {
                        date = value.getTimestamp(index);
                    } catch (SQLException e1) {
                        try {
                            date = value.getDate(index);
                        } catch (SQLException e2) {
                            throw new ComponentException(e2);
                        }
                    }

                    if (date == null) {
                        return null;
                    }
                    return date.getTime();
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._decimal()))

        {// TODO why we use big decimal type though AVRO types
         // don't contain it? No need to consider the
         // serialization? But we do it for date type above
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        return value.getBigDecimal(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getLong(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._double())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getDouble(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._float())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getFloat(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._boolean())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getBoolean(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._short())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getShort(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._character())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        String result = value.getString(index);

                        if (result == null || result.isEmpty()) {
                            return null;
                        }

                        return result.charAt(0);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._byte())) {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        if (value.getObject(index) == null) {
                            return null;
                        }

                        return value.getByte(index);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (isObject(basicSchema)) {
            return new JDBCSPConverter() {
    
              @Override
              public Object convertToAvro(CallableStatement value) {
                  try {
                      return value.getObject(index);
                  } catch (SQLException e) {
                      throw new ComponentException(e);
                  }
              }
    
            };
        } else {
            return new JDBCSPConverter() {

                @Override
                public Object convertToAvro(CallableStatement value) {
                    try {
                        String result = value.getString(index);

                        return result;
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        }
    }
    
    //please see the method in MetadataToolAvroHelper :
    //org.talend.core.model.metadata.builder.connection.MetadataColumn convertFromAvro(Schema.Field field,
    //org.talend.core.model.metadata.builder.connection.MetadataColumn col)
    public static boolean isObject(Schema schema) {
        if(schema == null) {
            return false;
        }
        
        if(schema.getType() == Schema.Type.STRING) {
            if(schema.getProp(SchemaConstants.JAVA_CLASS_FLAG) != null) {
                return true;
            }
        }
        
        return false;
    }

}
