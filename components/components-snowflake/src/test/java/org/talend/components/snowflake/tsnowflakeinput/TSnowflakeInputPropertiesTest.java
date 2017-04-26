package org.talend.components.snowflake.tsnowflakeinput;

import java.util.Collections;
import java.util.Set;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;


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
	
}
