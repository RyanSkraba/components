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
package org.talend.components.azurestorage.table;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableDefinition;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableDefinition;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnData;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class AzureStorageTableComponentsTest {

    private AzureStorageTableProperties properties;

    private AzureStorageTableDefinition azureStorageTableDefinition;

    @Before
    public void setup() {
        azureStorageTableDefinition = new TAzureStorageInputTableDefinition();
        properties = new AzureStorageTableProperties("tests");
        properties.setupProperties();
    }

    /**
     *
     * @see org.talend.components.azurestorage.table.AzureStorageTableDefinition#getFamilies()
     */
    @Test
    public void testGetFamilies() {
        assertEquals("Cloud/Azure Storage/Table", azureStorageTableDefinition.getFamilies()[0]);
    }

    /**
     *
     * @see org.talend.components.azurestorage.table.AzureStorageTableDefinition#isSchemaAutoPropagate()
     */
    @Test
    public void isSchemaAutoPropagate() {
        assertTrue(azureStorageTableDefinition.isSchemaAutoPropagate());
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        assertEquals(EnumSet.of(ConnectorTopology.OUTGOING),
                new TAzureStorageInputTableDefinition().getSupportedConnectorTopologies());

        assertEquals(EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING),
                new TAzureStorageOutputTableDefinition().getSupportedConnectorTopologies());
    }

    /**
     *
     * @see org.talend.components.azurestorage.table.AzureStorageTableDefinition#getRuntimeInfo(ExecutionEngine,ComponentProperties,ConnectorTopology)
     */
    @Test
    public void getRuntimeInfo() {
        assertNotNull(azureStorageTableDefinition.getRuntimeInfo(null, null, ConnectorTopology.OUTGOING));
        assertNotNull(azureStorageTableDefinition.getRuntimeInfo(null, null, ConnectorTopology.INCOMING));
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        assertArrayEquals(new Class[] { TAzureStorageConnectionProperties.class, AzureStorageTableProperties.class },
                azureStorageTableDefinition.getNestedCompatibleComponentPropertiesClass());
    }

    @Test
    public void getAllSchemaPropertiesConnectors() {
        TAzureStorageOutputTableProperties p = new TAzureStorageOutputTableProperties("test");

        assertTrue(p.getAllSchemaPropertiesConnectors(true).isEmpty());

        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        connectors.add(p.MAIN_CONNECTOR);
        assertEquals(connectors, p.getAllSchemaPropertiesConnectors(false));
    }

    /**
     * 
     * @see org.talend.components.azurestorage.table.AzureStorageTableProperties#validateNameMapping()
     */
    @Test
    public void testValidateNameMapping() {
        List<String> schemaMappings = new ArrayList<>();
        List<String> propertyMappings = new ArrayList<>();

        schemaMappings.add("daty");
        propertyMappings.add("datyMapped");
        schemaMappings.add("inty");
        propertyMappings.add("intyMapped");

        properties.nameMapping.schemaColumnName.setValue(schemaMappings);
        properties.nameMapping.entityPropertyName.setValue(propertyMappings);
        ValidationResult result = properties.validateNameMapping();
        assertEquals(Result.OK, result.getStatus());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        connectors.add(properties.FLOW_CONNECTOR);
        connectors.add(properties.REJECT_CONNECTOR);
        assertEquals(connectors, properties.getAllSchemaPropertiesConnectors(true));
        connectors.clear();
        connectors.add(properties.MAIN_CONNECTOR);
        assertEquals(connectors, properties.getAllSchemaPropertiesConnectors(false));
        assertEquals(connectors, new TAzureStorageInputTableProperties("test").getAllSchemaPropertiesConnectors(true));
        assertEquals(Collections.emptySet(),
                new TAzureStorageInputTableProperties("test").getAllSchemaPropertiesConnectors(false));

    }

    @Test
    public void testTAzureStorageInputTableProperties() {
        TAzureStorageInputTableProperties p = new TAzureStorageInputTableProperties("test");
        p.connection.setupProperties();
        p.setupProperties();
        p.connection.setupLayout();
        p.schema.setupProperties();
        p.schema.setupLayout();
        p.setupLayout();
        p.afterUseFilterExpression();
    }

    @Test
    public void testEnums() {
        assertEquals(ActionOnData.Delete, ActionOnData.valueOf("Delete"));
        assertEquals(ActionOnData.Insert, ActionOnData.valueOf("Insert"));
        assertEquals(ActionOnData.Insert_Or_Merge, ActionOnData.valueOf("Insert_Or_Merge"));
        assertEquals(ActionOnData.Insert_Or_Replace, ActionOnData.valueOf("Insert_Or_Replace"));
        assertEquals(ActionOnData.Merge, ActionOnData.valueOf("Merge"));
        assertEquals(ActionOnData.Replace, ActionOnData.valueOf("Replace"));

        assertEquals(ActionOnTable.Create_table, ActionOnTable.valueOf("Create_table"));
        assertEquals(ActionOnTable.Create_table_if_does_not_exist, ActionOnTable.valueOf("Create_table_if_does_not_exist"));
        assertEquals(ActionOnTable.Default, ActionOnTable.valueOf("Default"));
        assertEquals(ActionOnTable.Drop_and_create_table, ActionOnTable.valueOf("Drop_and_create_table"));
        assertEquals(ActionOnTable.Drop_table_if_exist_and_create, ActionOnTable.valueOf("Drop_table_if_exist_and_create"));
    }

}
