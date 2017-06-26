package org.talend.components.jdbc.tjdbcconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.presentation.Form;

/**
 * The class <code>TJDBCConnectionPropertiesTest</code> contains tests for the class
 * <code>{@link TJDBCConnectionProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:12
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCConnectionPropertiesTest {

    /**
     * Run the TJDBCConnectionProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testTJDBCConnectionProperties() throws Exception {
        String name = "connection";
        TJDBCConnectionProperties result = new TJDBCConnectionProperties(name);
        assertEquals("properties.connection.displayName", result.getDisplayName());
        assertEquals(name, result.getName());
        assertEquals(name, result.getTitle());
    }

    /**
     * Run the void afterShareConnection() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testAfterShareConnection() throws Exception {
        TJDBCConnectionProperties fixture = new TJDBCConnectionProperties("connection");
        fixture.init();

        Form main = fixture.getForm(Form.MAIN);

        fixture.shareConnection.setValue(true);
        fixture.afterShareConnection();
        Assert.assertTrue(main.getWidget(fixture.sharedConnectionName.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());

        fixture.shareConnection.setValue(false);
        fixture.useDataSource.setValue(true);
        fixture.afterShareConnection();
        Assert.assertTrue(!main.getWidget(fixture.sharedConnectionName.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.dataSource.getName()).isVisible());

        fixture.useDataSource.setValue(false);
        fixture.afterShareConnection();
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
    }

    /**
     * Run the void afterUseAutoCommit() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testAfterUseAutoCommit() throws Exception {
        TJDBCConnectionProperties fixture = new TJDBCConnectionProperties("connection");
        fixture.init();

        Form advanced = fixture.getForm(Form.ADVANCED);

        fixture.useAutoCommit.setValue(true);
        fixture.afterUseAutoCommit();
        Assert.assertTrue(advanced.getWidget(fixture.autocommit.getName()).isVisible());

        fixture.useAutoCommit.setValue(false);
        fixture.afterUseAutoCommit();
        Assert.assertTrue(!advanced.getWidget(fixture.autocommit.getName()).isVisible());
    }

    /**
     * Run the void afterUseDataSource() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testAfterUseDataSource() throws Exception {
        TJDBCConnectionProperties fixture = new TJDBCConnectionProperties("connection");
        fixture.init();

        Form main = fixture.getForm(Form.MAIN);
        fixture.useDataSource.setValue(true);
        fixture.afterUseDataSource();
        Assert.assertTrue(main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.shareConnection.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.sharedConnectionName.getName()).isVisible());

        fixture.useDataSource.setValue(false);
        fixture.shareConnection.setValue(true);
        fixture.afterUseDataSource();
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.shareConnection.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.sharedConnectionName.getName()).isVisible());

        fixture.shareConnection.setValue(false);
        fixture.afterUseDataSource();
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
    }

    /**
     * Run the AllSetting getRuntimeSetting() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testGetRuntimeSetting() throws Exception {
        TJDBCConnectionProperties fixture = new TJDBCConnectionProperties("connection");
        fixture.init();
        AllSetting result = fixture.getRuntimeSetting();
        assertNotNull(result);
    }

    /**
     * Run the void setupLayout() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testSetupLayout() throws Exception {
        TJDBCConnectionProperties fixture = new TJDBCConnectionProperties("connection");

        fixture.init();

        Form main = fixture.getForm(Form.MAIN);
        Form advanced = fixture.getForm(Form.ADVANCED);

        assertNotNull(main);
        assertNotNull(advanced);
    }

    /**
     * Run the void setupProperties() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:12
     */
    @Test
    public void testSetupProperties() throws Exception {
        TJDBCConnectionProperties fixture = new TJDBCConnectionProperties("connection");
        fixture.setupProperties();
        Assert.assertTrue(fixture.useAutoCommit.getValue());
    }

}