package org.talend.components.jdbc.runtime;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.jdbc.runtime.setting.AllSetting;

public class JdbcRuntimeUtilsTest {

    @Test
    public void getQueryToExecuteNegativeLimitTest() {
        AllSetting setting = buildSetting("mysql");
        String query1 = JdbcRuntimeUtils.getQueryToExecute(setting, -1);
        String query2 = JdbcRuntimeUtils.getQueryToExecute(setting, -10);
        String query3 = JdbcRuntimeUtils.getQueryToExecute(setting, 0);
        String query4 = JdbcRuntimeUtils.getQueryToExecute(setting, 10);
        Assert.assertEquals(query1, setting.getSql());
        Assert.assertEquals(query2, setting.getSql());
        Assert.assertEquals(query3, setting.getSql());
        Assert.assertNotEquals(query4, setting.getSql());
    }

    @Test
    public void getQueryToExecutePositiveLimitMysqlTest() {
        AllSetting setting = buildSetting("mysql");
        String query1 = JdbcRuntimeUtils.getQueryToExecute(setting, 1);
        String query2 = JdbcRuntimeUtils.getQueryToExecute(setting, 2);
        Assert.assertEquals(query1, "SELECT * FROM (originalQuery) AS derived LIMIT 1");
        Assert.assertEquals(query2, "SELECT * FROM (originalQuery) AS derived LIMIT 2");
    }

    @Test
    public void getQueryToExecutePositiveLimitNullTest() {
        AllSetting setting = buildSetting(null);
        String query1 = JdbcRuntimeUtils.getQueryToExecute(setting, 1);
        String query2 = JdbcRuntimeUtils.getQueryToExecute(setting, 2);
        Assert.assertEquals(query1, "SELECT * FROM (originalQuery) AS derived LIMIT 1");
        Assert.assertEquals(query2, "SELECT * FROM (originalQuery) AS derived LIMIT 2");
    }

    @Test
    public void getQueryToExecutePositiveLimitDerbyTest() {
        AllSetting setting = buildSetting("derby");
        String query1 = JdbcRuntimeUtils.getQueryToExecute(setting, 1);
        String query2 = JdbcRuntimeUtils.getQueryToExecute(setting, 2);
        Assert.assertEquals(query1, "SELECT * FROM (originalQuery) AS derived FETCH FIRST ROW ONLY");
        Assert.assertEquals(query2, "SELECT * FROM (originalQuery) AS derived FETCH FIRST 2 ROWS ONLY");
    }

    private AllSetting buildSetting(String db) {
        AllSetting setting = new AllSetting();
        if (db != null) {
            setting.setDriverClass("com." + db + ".driver.Driver");
        }
        setting.setSql("originalQuery");
        return setting;
    }
}