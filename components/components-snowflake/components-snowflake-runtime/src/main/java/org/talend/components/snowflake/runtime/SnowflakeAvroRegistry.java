// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.daikon.avro.AvroNamesValidationHelper;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.NameUtil;
import org.talend.daikon.avro.SchemaConstants;

public class SnowflakeAvroRegistry extends JDBCAvroRegistry {

    private static final SnowflakeAvroRegistry sInstance = new SnowflakeAvroRegistry();

    public static SnowflakeAvroRegistry get() {
        return sInstance;
    }

    Field sqlType2Avro(int size, int scale, int dbtype, boolean nullable, String name, String dbColumnName, Object defaultValue) {
        return sqlType2Avro(size, scale, dbtype, nullable, name, dbColumnName, defaultValue, false);
    }

    private Object checkNotNullableDefaultValueCorrect(int dbtype, Object defaultValue) {
        if (defaultValue instanceof String && StringUtils.isEmpty((String) defaultValue)) {
            switch (dbtype) {
            case java.sql.Types.INTEGER:
            case java.sql.Types.DECIMAL:
            case java.sql.Types.BIGINT:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
            case java.sql.Types.BOOLEAN:
                return null;
            }
        }
        return defaultValue;
    }

    @Override
    protected Field sqlType2Avro(int size, int scale, int dbtype, boolean nullable, String name, String dbColumnName,
            Object defaultValue, boolean isKey) {
        if (!nullable) {
            //snowflake schema contain empty string as default Values if not specified
            defaultValue = checkNotNullableDefaultValueCorrect(dbtype, defaultValue);
        }
        Field field = null;
        Schema schema = null;
        name = AvroNamesValidationHelper.getAvroCompatibleName(NameUtil.correct(dbColumnName, 0, Collections.<String>emptySet()));
        switch (dbtype) {
        case java.sql.Types.VARCHAR:
        case java.sql.Types.LONGVARCHAR:
        case java.sql.Types.CHAR:
            schema = AvroUtils._string();
            field = wrap(name, schema, nullable, defaultValue);
            field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, size);
            break;
        case java.sql.Types.INTEGER:
        case java.sql.Types.DECIMAL:
        case java.sql.Types.BIGINT:
        case java.sql.Types.NUMERIC:
        case java.sql.Types.TINYINT:
        case java.sql.Types.SMALLINT:
            schema = AvroUtils._decimal();
            field = wrap(name, schema, nullable, defaultValue);
            field.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            field.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
            break;
        case java.sql.Types.DOUBLE:
        case java.sql.Types.FLOAT:
        case java.sql.Types.REAL:
            schema = AvroUtils._double();
            field = wrap(name, schema, nullable, defaultValue);
            break;
        case java.sql.Types.DATE:
            schema = AvroUtils._int();
            LogicalTypes.date().addToSchema(schema);
            field = wrap(name, schema, nullable, defaultValue);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, SnowflakeConstants.TALEND_DEFAULT_DATE_PATTERN);
            break;
        case java.sql.Types.TIME:
            schema = AvroUtils._int();
            LogicalTypes.timeMillis().addToSchema(schema);
            field = wrap(name, schema, nullable, defaultValue);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, SnowflakeConstants.TALEND_DAFEULT_TIME_PATTERN);
            /** tell Avro converter how to process Original Avro logical type time
             * if value is "TALEND_DATE", it mean use Talend Date, if not, will use Talend Integer like before
             * we add this only one purpose : for the old job(before current commit), we keep Talend Integer, for new job, we use Talend Date
             * 
             * this affect the studio and DI model level which will affect the expected assign value type from input component(snowflakeinput)
             * and the expected passed value type to output component(snowflakeoutput),
             * and we adjust snowflake runtime to make it can process the DATE and INTEGER both for input/output
             * */
            field.addProp(SnowflakeConstants.LOGICAL_TIME_TYPE_AS, SnowflakeConstants.AS_TALEND_DATE);
            break;
        case java.sql.Types.TIMESTAMP:
            schema = AvroUtils._long();
            LogicalTypes.timestampMillis().addToSchema(schema);
            field = wrap(name, schema, nullable, defaultValue);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, SnowflakeConstants.TALEND_DAFEULT_TIMESTAMP_PATTERN);
            break;
        case java.sql.Types.BOOLEAN:
            schema = AvroUtils._boolean();
            field = wrap(name, schema, nullable, defaultValue);
            break;
        default:
            schema = AvroUtils._string();
            field = wrap(name, schema, nullable, defaultValue);
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

    protected Field wrap(String name, Schema base, boolean nullable, Object defaultValue) {
        Schema schema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;
        return new Field(name, schema, null, defaultValue);
    }

    @Override
    public JDBCConverter getConverter(final Field f) {
        final Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());

        //TODO after a check, not sure this overwrite is useful here, JDBCConverter should can work well enough, 
        //need a check more for snowflake "DATE" type about time zone
        
        //also if schema is set by customer self instead of the retrieve schema function by button or after some trigger, 
        //will no logical type, will use one in JDBCConverter instead of the one below
        
        //need to retrieve schema to know the database date type here for the case above if the code below is necessary
        return null == basicSchema.getLogicalType() ? super.getConverter(f) : new JDBCConverter() {

            @Override
            public Object convertToAvro(ResultSet value) {
                int index = f.pos() + 1;
                try {
                    if (basicSchema.getLogicalType() == LogicalTypes.date()) {
                        // Snowflake stores the value as the number of days. So it is possible to retrieve that as an
                        // int value instead of converting it to Date first and then to days from milliseconds. If we
                        // convert it to date, Snowflake jdbc shifts the time to 00:00 in current timezone.
                        Object date = value.getObject(index);
                        return (date != null) ? value.getInt(index) : null;
                    } else if (basicSchema.getLogicalType() == LogicalTypes.timeMillis()) {
                        java.sql.Time time = value.getTime(index);
                        //TODO remove the (int) cast here, seems not necessary
                        return (time != null) ? (int)time.getTime() : null;
                    } else {
                        java.sql.Timestamp timestamp = value.getTimestamp(index);
                        return (timestamp != null) ? timestamp.getTime() : null;
                    }
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }
            }
        };
    }
}
