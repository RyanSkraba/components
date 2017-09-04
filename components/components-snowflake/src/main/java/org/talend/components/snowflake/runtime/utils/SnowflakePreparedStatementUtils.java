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
package org.talend.components.snowflake.runtime.utils;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.talend.components.snowflake.SnowflakePreparedStatementTableProperties;

/**
 * Utility class that helps inserting values into prepared statement.
 *
 */
public final class SnowflakePreparedStatementUtils {

    private SnowflakePreparedStatementUtils() {
    };

    /**
     * Fulfills prepared statement with values.
     *
     * @param pstmt - statement created from query.
     * @param preparedStatementTable - properties that store all indexes, types and values for prepared statement.
     * @throws SQLException if failed to insert value into prepared statement.
     */
    public static void fillPreparedStatement(PreparedStatement pstmt, SnowflakePreparedStatementTableProperties preparedStatementTable)
            throws SQLException {
        List<Integer> indexes = preparedStatementTable.indexes.getValue();
        List<String> types = preparedStatementTable.types.getValue();
        List<Object> values = preparedStatementTable.values.getValue();
        for (int i = 0; i < indexes.size(); i++) {
            Integer index = indexes.get(i);
            SnowflakePreparedStatementTableProperties.Type type = SnowflakePreparedStatementTableProperties.Type.valueOf(types.get(i));
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
