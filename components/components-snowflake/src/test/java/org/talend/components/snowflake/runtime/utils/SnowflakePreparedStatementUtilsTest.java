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
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.snowflake.SnowflakePreparedStatementTableProperties;
import org.talend.components.snowflake.SnowflakePreparedStatementTableProperties.Type;

public class SnowflakePreparedStatementUtilsTest {

    private SnowflakePreparedStatementTableProperties prstmtTableProperties;

    private Object[] values;

    @Before
    public void setup() {
        prstmtTableProperties = new SnowflakePreparedStatementTableProperties("preparedStatementProperties");
        List<Integer> indexes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        prstmtTableProperties.indexes.setValue(indexes);
        List<String> types = new ArrayList<>();
        for (Type type : Type.values()) {
            types.add(type.name());
        }
        prstmtTableProperties.types.setValue(types);
        values = new Object[] { new BigDecimal(Integer.MAX_VALUE), Mockito.mock(Blob.class), false, (byte) 1, new byte[] { 1, 3 },
                Mockito.mock(Clob.class), new Date(), 0.1, 0.2f, 10, 15l, new Object(), (short) 655, "default string",
                new java.sql.Time(new Date().getTime()), Types.INTEGER };
        prstmtTableProperties.values.setValue(Arrays.asList(values));
    }

    @Test
    public void testFillPreparedStatement() throws SQLException {
        PreparedStatement prstmt = Mockito.mock(PreparedStatement.class);

        SnowflakePreparedStatementUtils.fillPreparedStatement(prstmt, prstmtTableProperties);

        Mockito.verify(prstmt, Mockito.times(1)).setBigDecimal(1, (BigDecimal) values[0]);
        Mockito.verify(prstmt, Mockito.times(1)).setBlob(2, (Blob) values[1]);
        Mockito.verify(prstmt, Mockito.times(1)).setBoolean(3, (boolean) values[2]);
        Mockito.verify(prstmt, Mockito.times(1)).setByte(4, (byte) values[3]);
        Mockito.verify(prstmt, Mockito.times(1)).setBytes(5, (byte[]) values[4]);
        Mockito.verify(prstmt, Mockito.times(1)).setClob(6, (Clob) values[5]);
        Mockito.verify(prstmt, Mockito.times(1)).setTimestamp(7, new Timestamp(((Date) values[6]).getTime()));
        Mockito.verify(prstmt, Mockito.times(1)).setDouble(8, (double) values[7]);
        Mockito.verify(prstmt, Mockito.times(1)).setFloat(9, (float) values[8]);
        Mockito.verify(prstmt, Mockito.times(1)).setInt(10, (int) values[9]);
        Mockito.verify(prstmt, Mockito.times(1)).setLong(11, (long) values[10]);
        Mockito.verify(prstmt, Mockito.times(1)).setObject(12, values[11]);
        Mockito.verify(prstmt, Mockito.times(1)).setShort(13, (short) values[12]);
        Mockito.verify(prstmt, Mockito.times(1)).setString(14, (String) values[13]);
        Mockito.verify(prstmt, Mockito.times(1)).setTime(15, (java.sql.Time) values[14]);
        Mockito.verify(prstmt, Mockito.times(1)).setNull(16, (int) values[15]);

    }

}
