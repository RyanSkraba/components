package org.talend.components.snowflake.tsnowflakeinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.ComponentConstants;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit tests for {@link TSnowflakeInputProperties} class
 */
public class TSnowflakeInputPropertiesTest {

    TSnowflakeInputProperties inputProperties;

    @Before
    public void reset() {
        inputProperties = new TSnowflakeInputProperties("input");
        inputProperties.init();
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {

        Set<PropertyPathConnector> allSchemaPropertiesConnectorsForInputConnection;

        allSchemaPropertiesConnectorsForInputConnection = inputProperties.getAllSchemaPropertiesConnectors(false);

        assertEquals(allSchemaPropertiesConnectorsForInputConnection, Collections.EMPTY_SET);

    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsForOutputConnection() {
        Set<PropertyPathConnector> allSchemaPropertiesConnectorsForOutputConnection;

        allSchemaPropertiesConnectorsForOutputConnection = inputProperties.getAllSchemaPropertiesConnectors(true);

        assertEquals(allSchemaPropertiesConnectorsForOutputConnection,
                Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "table.main")));

    }

    @Test
    public void testDefaultProperties() {
        Form main;
        boolean defaultManualQueryValue;
        boolean isQueryPropertyHidden;
        boolean isConditionPropertyHidden;

        main = inputProperties.getForm(Form.MAIN);
        defaultManualQueryValue = inputProperties.manualQuery.getValue();
        isQueryPropertyHidden = main.getWidget(inputProperties.query.getName()).isHidden();
        isConditionPropertyHidden = main.getWidget(inputProperties.condition.getName()).isHidden();

        assertFalse(defaultManualQueryValue);
        assertTrue(isQueryPropertyHidden);
        assertFalse(isConditionPropertyHidden);

    }

    @Test
    public void testFullSQLQueryTextArea() {
        Widget fullSQLQueryWidget = inputProperties.getForm(Form.MAIN).getWidget(inputProperties.query);

        assertEquals(Widget.TEXT_AREA_WIDGET_TYPE, fullSQLQueryWidget.getWidgetType());
        assertEquals(" ", inputProperties.query.getTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO));
    }

    @Test
    public void testConnectionPropertiesAreHidden() {
        TSnowflakeInputProperties inputProperties = new TSnowflakeInputProperties("inputProperties");
        inputProperties.init();

        inputProperties.connection.referencedComponent.componentInstanceId.setValue("SOME_COMPONENT");

        Form mainForm = inputProperties.getForm(Form.MAIN);
        inputProperties.refreshLayout(mainForm);
        Form connectionMainForm = inputProperties.connection.getForm(Form.MAIN);

        Widget accountWidget = connectionMainForm.getWidget(inputProperties.connection.account);
        Widget userAndPasswordWidget = connectionMainForm.getWidget(inputProperties.connection.userPassword);
        Widget warehouseWidget = connectionMainForm.getWidget(inputProperties.connection.warehouse);
        Widget schemaNameWidget = connectionMainForm.getWidget(inputProperties.connection.schemaName);
        Widget dbWidget = connectionMainForm.getWidget(inputProperties.connection.db);

        assertNotNull(accountWidget);
        assertFalse(accountWidget.isVisible());
        assertNotNull(userAndPasswordWidget);
        assertFalse(userAndPasswordWidget.isVisible());
        assertNotNull(warehouseWidget);
        assertFalse(warehouseWidget.isVisible());
        assertNotNull(schemaNameWidget);
        assertFalse(schemaNameWidget.isVisible());
        assertNotNull(dbWidget);
        assertFalse(dbWidget.isVisible());
    }

}
