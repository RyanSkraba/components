package org.talend.components.jdbc;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;

public class JdbcRuntimeInfoTest {

    @Test
    public void testJdbcRuntimeInfoRuntimeSettingProviderString() {
        TJDBCConnectionProperties properties = new TJDBCConnectionProperties("connection");
        new JdbcRuntimeInfo(properties, null);
    }

    @Test
    public void testRemoveQuote() {
        TJDBCConnectionProperties properties = new TJDBCConnectionProperties("connection");
        Assert.assertEquals("url", new JdbcRuntimeInfo(properties, null).removeQuote("\"url\""));
    }

}
