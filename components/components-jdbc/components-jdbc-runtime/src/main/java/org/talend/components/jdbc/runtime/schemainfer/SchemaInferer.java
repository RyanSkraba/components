package org.talend.components.jdbc.runtime.schemainfer;

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
import org.talend.components.common.avro.JDBCTableMetadata;
import org.talend.components.common.config.jdbc.Dbms;
import org.talend.components.common.config.jdbc.DbmsType;
import org.talend.components.common.config.jdbc.MappingType;
import org.talend.components.common.config.jdbc.TalendType;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.NameUtil;
import org.talend.daikon.avro.SchemaConstants;

public class SchemaInferer {

    public static Schema infer(ResultSetMetaData metadata, Dbms mapping) throws SQLException {
        List<Field> fields = new ArrayList<>();

        Set<String> existNames = new HashSet<String>();
        int index = 0;
        
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

            String columnTypeName = metadata.getColumnTypeName(i).toUpperCase();

            String validName = NameUtil.correct(fieldName, index++, existNames);
            existNames.add(validName);
            
            Field field = sqlType2Avro(size, scale, dbtype, nullable, validName, dbColumnName, null, isKey, mapping,
                    columnTypeName);

            fields.add(field);
        }

        return Schema.createRecord("DYNAMIC", null, null, false, fields);
    }

    public static Schema infer(JDBCTableMetadata tableMetadata, Dbms mapping) throws SQLException {
        DatabaseMetaData databaseMetdata = tableMetadata.getDatabaseMetaData();

        Set<String> keys = getPrimaryKeys(databaseMetdata, tableMetadata.getCatalog(), tableMetadata.getDbSchema(),
                tableMetadata.getTablename());

        Set<String> existNames = new HashSet<String>();
        int index = 0;
        
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
                
                String columnTypeName = metadata.getString("TYPE_NAME");
                
                String validName = NameUtil.correct(columnName, index++, existNames);
                existNames.add(validName);

                Field field = sqlType2Avro(size, scale, dbtype, nullable, validName, columnName, defaultValue, isKey, mapping,
                        columnTypeName);

                fields.add(field);
            } while (metadata.next());

            return Schema.createRecord(NameUtil.correct(tablename, 0, new HashSet<String>()), null, null, false, fields);
        }
    }

    private static Set<String> getPrimaryKeys(DatabaseMetaData databaseMetdata, String catalogName, String schemaName,
            String tableName) throws SQLException {
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

    private static Schema convertToAvro(TalendType talendType) {
        switch (talendType) {
        case STRING:
            return AvroUtils._string();
        case BOOLEAN:
            return AvroUtils._boolean();
        case INTEGER:
            return AvroUtils._int();
        case LONG:
            return AvroUtils._long();
        case DOUBLE:
            return AvroUtils._double();
        case FLOAT:
            return AvroUtils._float();
        case BYTE:
            return AvroUtils._byte();
        case SHORT:
            return AvroUtils._short();
        case CHARACTER:
            return AvroUtils._character();
        case BIG_DECIMAL:
            return AvroUtils._decimal();
        case DATE:
            return AvroUtils._date();
        case OBJECT :
            return SchemaBuilder.builder().stringBuilder().prop(SchemaConstants.JAVA_CLASS_FLAG, "java.lang.Object").endString();
        default:
            throw new UnsupportedOperationException("Unrecognized type " + talendType);
        }
    }
    
    private static Field sqlType2Avro(int size, int scale, int dbtype, boolean nullable, String name, String dbColumnName,
            Object defaultValue, boolean isKey, Dbms mapping, String columnTypeName) {
        MappingType<DbmsType, TalendType> mt = mapping.getDbmsMapping(columnTypeName);
        
        Field field = null;
        boolean isIgnoreLength = false;
        boolean isIgnorePrecision = false;
        
        if(mt!=null) {
            TalendType talendType = mt.getDefaultType();
            DbmsType sourceType = mt.getSourceType();
            Schema schema = convertToAvro(talendType);
            field = wrap(nullable, schema, name);
            
            isIgnoreLength = sourceType.isIgnoreLength();
            isIgnorePrecision = sourceType.isIgnorePrecision();
        } else {
            field = wrap(nullable, AvroUtils._string(), name);
        }

        switch (dbtype) {
        case java.sql.Types.VARCHAR:
            setPrecision(field, isIgnoreLength, size);
            break;
        case java.sql.Types.INTEGER:
            setPrecision(field, isIgnoreLength, size);
            break;
        case java.sql.Types.DECIMAL:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            break;
        case java.sql.Types.BIGINT:
            setPrecision(field, isIgnoreLength, size);
            break;
        case java.sql.Types.NUMERIC:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            break;
        case java.sql.Types.TINYINT:
            setPrecision(field, isIgnoreLength, size);
            break;
        case java.sql.Types.DOUBLE:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            break;
        case java.sql.Types.FLOAT:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            break;
        case java.sql.Types.DATE:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd");
            break;
        case java.sql.Types.TIME:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss");
            break;
        case java.sql.Types.TIMESTAMP:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd HH:mm:ss.SSS");
            break;
        case java.sql.Types.BOOLEAN:
            break;
        case java.sql.Types.REAL:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            break;
        case java.sql.Types.SMALLINT:
            setPrecision(field, isIgnoreLength, size);
            break;
        case java.sql.Types.LONGVARCHAR:
            setPrecision(field, isIgnoreLength, size);
            break;
        case java.sql.Types.CHAR:
            setPrecision(field, isIgnoreLength, size);
            break;
        default:
            setPrecision(field, isIgnoreLength, size);
            setScale(field, isIgnorePrecision, scale);
            break;
        }

        field.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, columnTypeName);
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, dbColumnName);

        if (defaultValue != null) {
            field.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, String.valueOf(defaultValue));
        }

        if (isKey) {
            field.addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");
        }

        return field;
    }

    private static void setPrecision(Field field, boolean ignorePrecision, int precision) {
        if (ignorePrecision) {
            return;
        }

        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(precision));
    }

    private static void setScale(Field field, boolean ignoreScale, int scale) {
        if (ignoreScale) {
            return;
        }

        field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(scale));
    }

    private static Field wrap(boolean nullable, Schema base, String name) {
        Schema schema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;
        return new Field(name, schema, null, (JsonNode) null);
    }
}
