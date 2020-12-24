package org.talend.components.jdbc.runtime.type;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.runtime.setting.JDBCSQLBuilder;
import org.talend.daikon.avro.AvroUtils;

public class RowWriter {

    private TypeWriter[] typeWriters;

    private final boolean debug;

    private DebugUtil debugUtil;

    public RowWriter(List<JDBCSQLBuilder.Column> columnList, Schema inputSchema, Schema currentSchema,
            PreparedStatement statement) {
        this(columnList, inputSchema, currentSchema, statement, false, null);
    }

    public RowWriter(List<JDBCSQLBuilder.Column> columnList, Schema inputSchema, Schema currentSchema,
            PreparedStatement statement, boolean debug, String sql) {
        this.debug = debug;

        if (debug) {
            debugUtil = new DebugUtil(sql);
        }

        List<TypeWriter> writers = new ArrayList<TypeWriter>();

        int statementIndex = 0;

        for (JDBCSQLBuilder.Column column : columnList) {
            Field inputField = CommonUtils.getField(inputSchema, column.columnLabel);

            Field componentField = CommonUtils.getField(currentSchema, column.columnLabel);
            int inputValueLocation = inputField.pos();
            String pattern = componentField.getProp("talend.field.pattern");
            statementIndex++;

            Schema basicSchema = AvroUtils.unwrapIfNullable(componentField.schema());
            // TODO any difference for nullable
            // boolean nullable = AvroUtils.isNullable(componentField.schema());

            TypeWriter writer = null;

            if (AvroUtils.isSameType(basicSchema, AvroUtils._string())) {
                writer = new StringTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._int())) {
                writer = new IntTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._date())) {
                writer = new DateTypeWriter(statement, statementIndex, inputValueLocation, pattern);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._decimal())) {
                writer = new BigDecimalTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {
                writer = new LongTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._double())) {
                writer = new DoubleTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._float())) {
                writer = new FloatTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._boolean())) {
                writer = new BooleanTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._short())) {
                writer = new ShortTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._character())) {
                writer = new CharacterTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._byte())) {
                writer = new ByteTypeWriter(statement, statementIndex, inputValueLocation);
            } else if (AvroUtils.isSameType(basicSchema, AvroUtils._bytes())) {
                writer = new BytesTypeWriter(statement, statementIndex, inputValueLocation);
            } else {
                writer = new ObjectTypeWriter(statement, statementIndex, inputValueLocation);
            }

            writers.add(writer);
        }

        typeWriters = writers.toArray(new TypeWriter[0]);
    }

    public String write(IndexedRecord input) throws SQLException {
        if (debug) {
            debugUtil.writeHead();
        }

        for (TypeWriter writer : typeWriters) {
            writer.write(input);
        }

        if (debug) {
            return debugUtil.getSQL();
        }

        return null;
    }

    private void writeDebugColumnNullContent() {
        if (debug) {
            debugUtil.writeColumn(null, false);
        }
    }

    class TypeWriter {

        protected final PreparedStatement statement;

        protected final int statementIndex;

        protected final int inputValueLocation;

        protected TypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            this.statement = statement;
            this.statementIndex = statementIndex;
            this.inputValueLocation = inputValueLocation;
        }

        void write(IndexedRecord input) throws SQLException {
            // do nothing
        }

    }

    class StringTypeWriter extends TypeWriter {

        StringTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.VARCHAR);
                writeDebugColumnNullContent();
            } else {
                statement.setString(statementIndex, inputValue.toString());
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), true);
                }
            }
        }
    }

    class IntTypeWriter extends TypeWriter {

        IntTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.INTEGER);
                writeDebugColumnNullContent();
            } else {
                statement.setInt(statementIndex, (int) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class DateTypeWriter extends TypeWriter {

        private String pattern;
        DateTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation, String pattern) {
            super(statement, statementIndex, inputValueLocation);
            this.pattern = pattern;
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.TIMESTAMP);
            } else {
                if (inputValue instanceof Timestamp) {
                    //some jdbc implement may not follow jdbc spec, for example :
                    //mysq_preparestatement.setTimestamp(oracle_timestamp) may not work
                    //but that is only a guess, may not right, maybe can set directly, so do the thing below
                    //here only for safe, so do like this
                    Timestamp source = (Timestamp) inputValue;
                    Timestamp target = new Timestamp(source.getTime());
                    target.setNanos(source.getNanos());
                    statement.setTimestamp(statementIndex, target);
                    debug(inputValue);
                } else if (inputValue instanceof Date) {
                    statement.setTimestamp(statementIndex, new Timestamp(((Date) inputValue).getTime()));
                    debug(inputValue);
                } else {
                    statement.setTimestamp(statementIndex, new Timestamp((long) inputValue));
                    if (debug) {
                        debugUtil.writeColumn(new Timestamp((long) inputValue).toString(), false);
                    }
                }
            }
        }

        private void debug(Object inputValue) {
            if (debug) {
                if (pattern.length() == 0 || pattern == null) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    debugUtil.writeColumn(sdf.format((Date) inputValue), false);
                }
            }
        }

    }

    class BigDecimalTypeWriter extends TypeWriter {

        BigDecimalTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.DECIMAL);
                writeDebugColumnNullContent();
            } else {
                // TODO check if it's right
                statement.setBigDecimal(statementIndex, (BigDecimal) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class LongTypeWriter extends TypeWriter {

        LongTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.INTEGER);
                writeDebugColumnNullContent();
            } else {
                statement.setLong(statementIndex, (long) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class DoubleTypeWriter extends TypeWriter {

        DoubleTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.DOUBLE);
                writeDebugColumnNullContent();
            } else {
                statement.setDouble(statementIndex, (double) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class FloatTypeWriter extends TypeWriter {

        FloatTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.FLOAT);
                writeDebugColumnNullContent();
            } else {
                statement.setFloat(statementIndex, (float) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class BooleanTypeWriter extends TypeWriter {

        BooleanTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.BOOLEAN);
                writeDebugColumnNullContent();
            } else {
                statement.setBoolean(statementIndex, (boolean) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class ShortTypeWriter extends TypeWriter {

        ShortTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.INTEGER);
                writeDebugColumnNullContent();
            } else {
                statement.setShort(statementIndex, ((Number) inputValue).shortValue());
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class ByteTypeWriter extends TypeWriter {

        ByteTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.INTEGER);
                writeDebugColumnNullContent();
            } else {
                // please see org.talend.codegen.enforcer.IncomingSchemaEnforcer, it will convert byte(Byte) to int(Integer), not
                // know why, so change here
                // statement.setByte(statementIndex, (byte) inputValue);
                statement.setByte(statementIndex, ((Number) inputValue).byteValue());
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class CharacterTypeWriter extends TypeWriter {

        CharacterTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.CHAR);
                writeDebugColumnNullContent();
            } else {
                statement.setInt(statementIndex, (char) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), true);
                }
            }
        }

    }

    class BytesTypeWriter extends TypeWriter {

        BytesTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.ARRAY);
                writeDebugColumnNullContent();
            } else {
                statement.setBytes(statementIndex, (byte[]) inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

    class ObjectTypeWriter extends TypeWriter {

        ObjectTypeWriter(PreparedStatement statement, int statementIndex, int inputValueLocation) {
            super(statement, statementIndex, inputValueLocation);
        }

        public void write(IndexedRecord input) throws SQLException {
            Object inputValue = input.get(inputValueLocation);
            if (inputValue == null) {
                statement.setNull(statementIndex, java.sql.Types.JAVA_OBJECT);
                writeDebugColumnNullContent();
            } else {
                statement.setObject(statementIndex, inputValue);
                if (debug) {
                    debugUtil.writeColumn(inputValue.toString(), false);
                }
            }
        }

    }

}
