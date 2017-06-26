package org.talend.components.jdbc.tjdbcoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.properties.presentation.Form;

/**
 * The class <code>TJDBCOutputPropertiesTest</code> contains tests for the class <code>{@link TJDBCOutputProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCOutputPropertiesTest {

    /**
     * Run the TJDBCOutputProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCOutputProperties() throws Exception {
        String name = "output";

        TJDBCOutputProperties result = new TJDBCOutputProperties(name);

        assertEquals("properties.output.displayName", result.getDisplayName());
        assertEquals(name, result.getName());
        assertEquals(name, result.getTitle());
    }

    /**
     * Run the ValidationResult afterFetchSchemaFromTable() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Ignore
    @Test(expected = ComponentException.class)
    public void testAfterFetchSchemaFromTable() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.init();
        fixture.afterFetchSchemaFromTable();
    }

    /**
     * Run the void afterReferencedComponent() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterReferencedComponent() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.init();

        Form main = fixture.getForm(Form.MAIN);
        Form advanced = fixture.getForm(Form.ADVANCED);

        fixture.referencedComponent.componentInstanceId.setValue(TJDBCConnectionDefinition.COMPONENT_NAME + "_1");
        fixture.afterReferencedComponent();
        Assert.assertTrue(
                !main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(!advanced.getWidget(fixture.commitEvery.getName()).isVisible());

        fixture.referencedComponent.componentInstanceId.setValue(null);
        fixture.useDataSource.setValue(true);
        fixture.afterReferencedComponent();
        Assert.assertTrue(
                main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(advanced.getWidget(fixture.commitEvery.getName()).isVisible());

        fixture.referencedComponent.componentInstanceId.setValue("");
        fixture.useDataSource.setValue(false);
        fixture.afterReferencedComponent();
        Assert.assertTrue(
                main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(advanced.getWidget(fixture.commitEvery.getName()).isVisible());
    }

    /**
     * Run the void afterUseBatch() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterUseBatch() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.init();

        Form advanced = fixture.getForm(Form.ADVANCED);

        fixture.useBatch.setValue(true);
        fixture.afterUseBatch();
        Assert.assertTrue(advanced.getWidget(fixture.batchSize.getName()).isVisible());

        fixture.useBatch.setValue(false);
        fixture.afterUseBatch();
        Assert.assertTrue(!advanced.getWidget(fixture.batchSize.getName()).isVisible());
    }

    /**
     * Run the void afterUseDataSource() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterUseDataSource() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.init();

        Form main = fixture.getForm(Form.MAIN);

        fixture.useDataSource.setValue(true);
        fixture.afterUseDataSource();
        Assert.assertTrue(main.getWidget(fixture.dataSource.getName()).isVisible());

        fixture.useDataSource.setValue(false);
        fixture.afterUseDataSource();
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
    }

    /**
     * Run the Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        Set<PropertyPathConnector> result = fixture.getAllSchemaPropertiesConnectors(true);
        assertNotNull(result);
        assertEquals(2, result.size());

        result = fixture.getAllSchemaPropertiesConnectors(false);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Run the AllSetting getRuntimeSetting() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetRuntimeSetting() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.setupProperties();
        AllSetting result = fixture.getRuntimeSetting();
        assertNotNull(result);
    }

    /**
     * Run the void setupLayout() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testSetupLayout() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
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
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testSetupProperties() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.setupProperties();
        Assert.assertEquals(TJDBCOutputProperties.DataAction.INSERT, fixture.dataAction.getValue());
        Assert.assertEquals(Integer.valueOf(10000), fixture.commitEvery.getValue());
        Assert.assertEquals(true, fixture.useBatch.getValue());
        Assert.assertEquals(Integer.valueOf(10000), fixture.batchSize.getValue());
    }

    /**
     * Run the void updateOutputSchemas() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testUpdateOutputSchemas() throws Exception {
        TJDBCOutputProperties fixture = new TJDBCOutputProperties("output");
        fixture.init();

        Schema main = SchemaBuilder.builder().record("schema").fields().endRecord();
        fixture.main.schema.setValue(main);
        fixture.updateOutputSchemas();

        Schema flow = fixture.schemaFlow.schema.getValue();
        Schema reject = fixture.schemaReject.schema.getValue();
        assertEquals(main, flow);
        assertEquals(2, reject.getFields().size());
    }

}