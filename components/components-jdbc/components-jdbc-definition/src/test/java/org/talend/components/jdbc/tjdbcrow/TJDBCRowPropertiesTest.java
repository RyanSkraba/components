package org.talend.components.jdbc.tjdbcrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

/**
 * The class <code>TJDBCRowPropertiesTest</code> contains tests for the class <code>{@link TJDBCRowProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCRowPropertiesTest {

    /**
     * Run the TJDBCRowProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCRowProperties() throws Exception {
        String name = "row";

        TJDBCRowProperties result = new TJDBCRowProperties(name);

        assertEquals("properties.row.displayName", result.getDisplayName());
        assertEquals(name, result.getName());
        assertEquals(name, result.getTitle());
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
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
     * Run the void afterPropagateQueryResultSet() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testAfterPropagateQueryResultSet() throws Exception {
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();
        fixture.afterPropagateQueryResultSet();
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();
        fixture.afterReferencedComponent();
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();
        fixture.afterUseDataSource();
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();
        fixture.afterUsePreparedStatement();
    }

    /**
     * Run the void beforeUseColumn() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testBeforeUseColumn() throws Exception {
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();
        fixture.beforeUseColumn();
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");

        Set<PropertyPathConnector> result = fixture.getAllSchemaPropertiesConnectors(true);
        assertEquals(2, result.size());

        result = fixture.getAllSchemaPropertiesConnectors(false);
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();
        AllSetting result = fixture.getRuntimeSetting();
        assertNotNull(result);
    }

    /**
     * Run the void refreshLayout(Form) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testRefreshLayout() throws Exception {
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");
        fixture.init();

        Form main = fixture.getForm(Form.MAIN);
        Form advanced = fixture.getForm(Form.ADVANCED);

        fixture.referencedComponent.componentInstanceId.setValue(TJDBCConnectionDefinition.COMPONENT_NAME + "_1");
        fixture.refreshLayout(main);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(
                !main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(!advanced.getWidget(fixture.commitEvery.getName()).isVisible());

        fixture.referencedComponent.componentInstanceId.setValue(null);
        fixture.useDataSource.setValue(true);
        fixture.refreshLayout(main);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(
                main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(advanced.getWidget(fixture.commitEvery.getName()).isVisible());

        fixture.referencedComponent.componentInstanceId.setValue("");
        fixture.useDataSource.setValue(false);
        fixture.refreshLayout(main);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(
                main.getChildForm(fixture.connection.getName()).getWidget(fixture.connection.jdbcUrl.getName()).isVisible());
        Assert.assertTrue(main.getWidget(fixture.useDataSource.getName()).isVisible());
        Assert.assertTrue(!main.getWidget(fixture.dataSource.getName()).isVisible());
        Assert.assertTrue(advanced.getWidget(fixture.commitEvery.getName()).isVisible());

        fixture.propagateQueryResultSet.setValue(true);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(advanced.getWidget(fixture.useColumn.getName()).isVisible());

        fixture.propagateQueryResultSet.setValue(false);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(!advanced.getWidget(fixture.useColumn.getName()).isVisible());

        fixture.usePreparedStatement.setValue(true);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(advanced.getWidget(fixture.preparedStatementTable.getName()).isVisible());

        fixture.usePreparedStatement.setValue(false);
        fixture.refreshLayout(advanced);
        Assert.assertTrue(!advanced.getWidget(fixture.preparedStatementTable.getName()).isVisible());
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
        TJDBCRowProperties fixture = new TJDBCRowProperties("row");

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