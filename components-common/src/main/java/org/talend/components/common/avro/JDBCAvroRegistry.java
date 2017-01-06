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

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter.UnmodifiableAdapterException;
import org.talend.daikon.java8.SerializableFunction;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCAvroRegistry extends AvroRegistry {

    private static final JDBCAvroRegistry sInstance = new JDBCAvroRegistry();

    public static JDBCAvroRegistry get() {
        return sInstance;
    }

    protected JDBCAvroRegistry() {

        registerSchemaInferrer(ResultSet.class, new SerializableFunction<ResultSet, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(ResultSet t) {
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

            Field field = sqlType2Avro(size, scale, dbtype, nullable, fieldName, dbColumnName, null);

            fields.add(field);
        }

        return Schema.createRecord("DYNAMIC", null, null, false, fields);
    }

    protected Schema inferSchemaResultSet(ResultSet metadata) throws SQLException {
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
            String defaultValue = metadata.getString("COLUMN_DEF");

            Field field = sqlType2Avro(size, scale, dbtype, nullable, columnName, columnName, defaultValue);

            fields.add(field);
        } while (metadata.next());

        return Schema.createRecord(tablename, null, null, false, fields);
    }

    protected Field sqlType2Avro(int size, int scale, int dbtype, boolean nullable, String name, String dbColumnName,
            Object defaultValue) {
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

        return field;
    }

    protected Field wrap(boolean nullable, Schema base, String name) {
        Schema schema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;
        return new Field(name, schema, null, (Object) null);
    }

    public JDBCConverter getConverter(final Field f) {
        Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());

        if (AvroUtils.isSameType(basicSchema, AvroUtils._string())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    boolean trimAll = isTrim();
                    // TODO trim the columns which is selected by user
                    try {
                        String result = value.getString(f.pos() + 1);

                        if (trimAll && result != null) {
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
                    int index = f.pos() + 1;
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

                    int index = f.pos() + 1;
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
                        return value.getBigDecimal(f.pos() + 1);
                    } catch (SQLException e) {
                        throw new ComponentException(e);
                    }
                }

            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    int index = f.pos() + 1;
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
                    int index = f.pos() + 1;
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
                    int index = f.pos() + 1;
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
                    int index = f.pos() + 1;
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
                    int index = f.pos() + 1;
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
                    boolean trimAll = isTrim();
                    // TODO trim the columns which is selected by user
                    try {
                        String result = value.getString(f.pos() + 1);

                        if (trimAll && result != null) {
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
                    int index = f.pos() + 1;
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
        } else {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    // TODO trim the columns which is selected by user
                    try {
                        String result = value.getString(f.pos() + 1);

                        if (isTrim() && result != null) {
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

        public void setInfluencer(JDBCAvroRegistryInfluencer influencer) {
            this.influencer = influencer;
        }

    }

}
