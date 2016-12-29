// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.jdbc.module.PreparedStatementTable;
import org.talend.daikon.avro.SchemaConstants;

/**
 * provide some tools for JDBC runtime
 *
 */
public class JDBCTemplate {

    /**
     * get key columns from the fields
     * 
     * @param allFields
     * @return
     */
    public static List<Schema.Field> getKeyColumns(List<Schema.Field> allFields) {
        List<Schema.Field> result = new ArrayList<Schema.Field>();

        for (Schema.Field field : allFields) {
            boolean isKey = "true".equalsIgnoreCase(field.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
            if (isKey) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * get value columns from the fields
     * 
     * @param allFields
     * @return
     */
    public static List<Schema.Field> getValueColumns(List<Schema.Field> allFields) {
        List<Schema.Field> result = new ArrayList<Schema.Field>();

        for (Schema.Field field : allFields) {
            boolean isKey = "true".equalsIgnoreCase(field.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));

            if (!isKey) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * fill the prepared statement object
     * 
     * @param pstmt
     * @param indexs
     * @param types
     * @param values
     * @throws SQLException
     */
    public static void setPreparedStatement(final PreparedStatement pstmt, final List<Integer> indexs, final List<String> types,
            final List<Object> values) throws SQLException {
        // TODO : adjust it
        for (int i = 0; i < indexs.size(); i++) {
            Integer index = indexs.get(i);
            PreparedStatementTable.Type type = PreparedStatementTable.Type.valueOf(types.get(i));
            Object value = values.get(i);

            switch (type) {
            case BigDecimal:
                pstmt.setBigDecimal(index, (BigDecimal) value);
                break;
            case Blob:
                pstmt.setBlob(index, (Blob) value);
                break;
            case Boolean:
                pstmt.setBoolean(index, (boolean) value);
                break;
            case Byte:
                pstmt.setByte(index, (byte) value);
                break;
            case Bytes:
                pstmt.setBytes(index, (byte[]) value);
                break;
            case Clob:
                pstmt.setClob(index, (Clob) value);
                break;
            case Date:
                pstmt.setTimestamp(index, new Timestamp(((Date) value).getTime()));
                break;
            case Double:
                pstmt.setDouble(index, (double) value);
                break;
            case Float:
                pstmt.setFloat(index, (float) value);
                break;
            case Int:
                pstmt.setInt(index, (int) value);
                break;
            case Long:
                pstmt.setLong(index, (long) value);
                break;
            case Object:
                pstmt.setObject(index, value);
                break;
            case Short:
                pstmt.setShort(index, (short) value);
                break;
            case String:
                pstmt.setString(index, (String) value);
                break;
            case Time:
                pstmt.setTime(index, (Time) value);
                break;
            case Null:
                pstmt.setNull(index, (int) value);
                break;
            default:
                pstmt.setString(index, (String) value);
                break;
            }
        }
    }

}
