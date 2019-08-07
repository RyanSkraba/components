package org.talend.components.jdbc.tjdbcinput;

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
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

/**
 * The class <code>TJDBCInputPropertiesTest</code> contains tests for the class <code>{@link TJDBCInputProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCInputPropertiesTest {

    /**
     * Run the TJDBCInputProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCInputProperties_1() throws Exception {
        String name = "input";

        TJDBCInputProperties result = new TJDBCInputProperties(name);

        assertEquals("properties.input.displayName", result.getDisplayName());
        assertEquals(name, result.getName());
        assertEquals(name, result.getTitle());
    }

    /**
     * Run the ValidationResult afterFetchSchemaFromQuery() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Ignore
    @Test(expected = ComponentException.class)
    public void testAfterFetchSchemaFromQuery() throws Exception {
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
        fixture.init();
        fixture.afterFetchSchemaFromQuery();
    }

    /**
     * Run the ValidationResult afterGuessQueryFromSchema() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterGuessQueryFromSchema() throws Exception {
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
        fixture.init();

        fixture.tableSelection.tablename.setValue(null);
        ValidationResult result = fixture.afterGuessQueryFromSchema();
        Assert.assertEquals(result.getStatus(), ValidationResult.Result.ERROR);

        fixture.tableSelection.tablename.setValue("");
        result = fixture.afterGuessQueryFromSchema();
        Assert.assertEquals(result.getStatus(), ValidationResult.Result.ERROR);

        fixture.tableSelection.tablename.setValue("test");
        fixture.main.schema.setValue(null);
        result = fixture.afterGuessQueryFromSchema();
        Assert.assertEquals(result.getStatus(), ValidationResult.Result.ERROR);

        fixture.tableSelection.tablename.setValue("test");
        Schema emptySchema = SchemaBuilder.builder().record("TEST").fields().endRecord();
        fixture.main.schema.setValue(emptySchema);
        result = fixture.afterGuessQueryFromSchema();
        Assert.assertEquals(result.getStatus(), ValidationResult.Result.ERROR);

        fixture.tableSelection.tablename.setValue("test");
        Schema schema = SchemaBuilder.builder().record("TEST").fields().name("ID")
                .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID")
                .type(SchemaBuilder.builder().nullable().type(AvroUtils._string())).noDefault().name("NAME")
                .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME")
                .type(SchemaBuilder.builder().nullable().type(AvroUtils._string())).noDefault().endRecord();
        fixture.main.schema.setValue(schema);
        result = fixture.afterGuessQueryFromSchema();
        Assert.assertEquals(result, ValidationResult.OK);
    }

    /**
     * Run the void afterUseCursor() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterUseCursor() throws Exception {
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
        fixture.init();

        Form advanced = fixture.getForm(Form.ADVANCED);

        fixture.useCursor.setValue(true);
        fixture.afterUseCursor();
        Assert.assertTrue(advanced.getWidget(fixture.cursor.getName()).isVisible());

        fixture.useCursor.setValue(false);
        fixture.afterUseCursor();
        Assert.assertTrue(!advanced.getWidget(fixture.cursor.getName()).isVisible());
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
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
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
     * Run the void afterUsePreparedStatement() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterUsePreparedStatement() throws Exception {
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
        fixture.init();
        fixture.usePreparedStatement.setValue(true);
        fixture.afterUsePreparedStatement();
        Form advance = fixture.getForm(Form.ADVANCED);
        Assert.assertFalse(!advance.getWidget(fixture.preparedStatementTable.getName()).isVisible());
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
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
        fixture.init();

        Form main = fixture.getForm(Form.MAIN);

        fixture.referencedComponent.componentInstanceId.setValue(TJDBCConnectionDefinition.COMPONENT_NAME + "_1");
        fixture.afterReferencedComponent();
        Assert.assertTrue(
                !main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());

        fixture.referencedComponent.componentInstanceId.setValue(null);
        fixture.useDataSource.setValue(true);
        fixture.afterReferencedComponent();
        Assert.assertTrue(
                main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.dataSource.getName()).isVisible());

        fixture.referencedComponent.componentInstanceId.setValue("");
        fixture.useDataSource.setValue(false);
        fixture.afterReferencedComponent();
        Assert.assertTrue(
                main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
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
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");

        Set<PropertyPathConnector> result = fixture.getAllSchemaPropertiesConnectors(true);
        assertNotNull(result);
        assertEquals(1, result.size());

        result = fixture.getAllSchemaPropertiesConnectors(false);
        assertNotNull(result);
        assertEquals(0, result.size());
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
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
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
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
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
        TJDBCInputProperties fixture = new TJDBCInputProperties("input");
        fixture.setupProperties();
        Assert.assertEquals(Integer.valueOf(1000), fixture.cursor.getValue());
    }

}