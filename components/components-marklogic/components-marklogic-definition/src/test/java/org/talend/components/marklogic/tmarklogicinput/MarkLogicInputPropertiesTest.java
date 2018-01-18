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
package org.talend.components.marklogic.tmarklogicinput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.ArrayList;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.ComponentConstants;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionPropertiesTest;
import org.talend.daikon.properties.presentation.Form;

public class MarkLogicInputPropertiesTest {

    private MarkLogicInputProperties testInputProperties;

    @Before
    public void setUp() {
        testInputProperties = new MarkLogicInputProperties("testInputProperties");
    }

    /**
     * Checks forms are filled with required widgets
     */
    @Test
    public void testSetupLayout() {
        testInputProperties.setupProperties();
        testInputProperties.connection.init();
        testInputProperties.datasetProperties.init();
        testInputProperties.datasetProperties.main.init();
        testInputProperties.inputSchema.init();

        testInputProperties.init();
        Form main = testInputProperties.getForm(Form.MAIN);
        assertNotNull(main.getWidget(testInputProperties.connection));
        assertNotNull(((Form)main.getWidget("datasetProperties").getContent()).getWidget(testInputProperties.datasetProperties.criteria));
        //should not be on main form
        assertNull(main.getWidget(testInputProperties.maxRetrieve));
        assertNull(main.getWidget(testInputProperties.datasetProperties.pageSize));
        assertNull(main.getWidget(testInputProperties.datasetProperties.useQueryOption));
        assertNull(main.getWidget(testInputProperties.datasetProperties.queryLiteralType));
        assertNull(main.getWidget(testInputProperties.datasetProperties.queryOptionName));
        assertNull(main.getWidget(testInputProperties.datasetProperties.queryOptionLiterals));

        Form advanced = testInputProperties.getForm(Form.ADVANCED);
        assertNotNull(advanced.getWidget(testInputProperties.maxRetrieve));
        Form mainDataset = advanced.getChildForm("datasetProperties");
        assertNotNull(mainDataset.getWidget(testInputProperties.datasetProperties.pageSize));
        assertNotNull(mainDataset.getWidget(testInputProperties.datasetProperties.useQueryOption));
        assertNotNull(mainDataset.getWidget(testInputProperties.datasetProperties.queryLiteralType));
        assertNotNull(mainDataset.getWidget(testInputProperties.datasetProperties.queryOptionName));
        assertNotNull(mainDataset.getWidget(testInputProperties.datasetProperties.queryOptionLiterals));
    }

    /**
     * Checks default values are set correctly
     */
    @Test
    public void testSetupProperties() {
        Integer expectedDefaultMaxRetrieveNumber = -1;
        Integer expectedDefaultPageSize = 10;
        Boolean expectedDefaultUseQueryOption = false;
        String expectedDefaultQueryLiteralType = "XML";

        testInputProperties.init();
        assertEquals(MarkLogicConnectionPropertiesTest.EXPECTED_DEFAULT_HOST, testInputProperties.connection.host.getValue());
        assertEquals(MarkLogicConnectionPropertiesTest.EXPECTED_DEFAULT_PORT, testInputProperties.connection.port.getValue());
        assertEquals(MarkLogicConnectionPropertiesTest.EXPECTED_DEFAULT_DATABASE, testInputProperties.connection.database.getValue());
        assertNull(testInputProperties.connection.username.getValue());
        assertNull(testInputProperties.connection.password.getValue());
        assertTrue(testInputProperties.datasetProperties.criteria.getValue().isEmpty());
        assertEquals(expectedDefaultMaxRetrieveNumber, testInputProperties.maxRetrieve.getValue());
        assertEquals(expectedDefaultPageSize, testInputProperties.datasetProperties.pageSize.getValue());
        assertEquals(expectedDefaultUseQueryOption, testInputProperties.datasetProperties.useQueryOption.getValue());
        assertEquals(expectedDefaultQueryLiteralType, testInputProperties.datasetProperties.queryLiteralType.getValue());
        assertNull(testInputProperties.datasetProperties.queryOptionName.getValue());
        assertNull(testInputProperties.datasetProperties.queryOptionLiterals.getValue());
        assertEquals(" ", testInputProperties.datasetProperties.queryOptionLiterals.getTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO));
    }

    @Test
    public void testSchemaDocIdFieldIsLocked() {
        testInputProperties.init();
        assertNull(testInputProperties.datasetProperties.main.schema.getValue().getProp(TALEND_IS_LOCKED));
        assertEquals("true", testInputProperties.datasetProperties.main.schema.getValue().getField("docId").getProp(TALEND_IS_LOCKED));
        assertNull(testInputProperties.datasetProperties.main.schema.getValue().getField("docContent").getProp(TALEND_IS_LOCKED));
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsForIOConnection() {
        Set<PropertyPathConnector> actualConnectors = testInputProperties.getAllSchemaPropertiesConnectors(true);

        assertThat(actualConnectors, Matchers.contains(testInputProperties.MAIN_CONNECTOR));
        assertEquals(1,actualConnectors.size());
        assertEquals("datasetProperties.main", new ArrayList<>(actualConnectors).get(0).getPropertyPath());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsForOnlyOutputConnection() {
        Set<PropertyPathConnector> actualConnectors = testInputProperties.getAllSchemaPropertiesConnectors(false);

        assertThat(actualConnectors, Matchers.contains(testInputProperties.INCOMING_CONNECTOR));
        assertEquals(1, actualConnectors.size());
        assertEquals("inputSchema", new ArrayList<>(actualConnectors).get(0).getPropertyPath());
    }

    /**
     * Checks initial layout
     */
    @Test
    public void testRefreshLayout() {
        testInputProperties.setupProperties();
        testInputProperties.connection.init();
        testInputProperties.datasetProperties.init();
        testInputProperties.inputSchema.init();
        testInputProperties.setupLayout();

        testInputProperties.refreshLayout(testInputProperties.getForm(Form.MAIN));
        testInputProperties.refreshLayout(testInputProperties.getForm(Form.ADVANCED));

        boolean schemaHidden = ((Form) testInputProperties.getForm(Form.REFERENCE).getWidget("datasetProperties").getContent()).getWidget("main").isHidden();
        boolean isConnectionPropertiesHidden = testInputProperties.getForm(Form.MAIN).getWidget("connection").isHidden();
        boolean isQueryCriteriaHidden = ((Form) testInputProperties.getForm(Form.MAIN).getWidget("datasetProperties").getContent()).getWidget("criteria").isHidden();
        boolean isMaxRetrieveHidden = testInputProperties.getForm(Form.ADVANCED).getWidget("maxRetrieve").isHidden();
        Form mainDataset = testInputProperties.getForm(Form.ADVANCED).getChildForm("datasetProperties");
        boolean isPageSizeHidden = mainDataset.getWidget("pageSize").isHidden();
        boolean isUseQueryOptionHidden = mainDataset.getWidget("useQueryOption").isHidden();
        boolean isQueryLiteralTypeHidden = mainDataset.getWidget("queryLiteralType").isHidden();
        boolean isQueryOptionNameHidden = mainDataset.getWidget("queryOptionName").isHidden();
        boolean isQueryOptionLiteralsHidden = mainDataset.getWidget("queryOptionLiterals").isHidden();

        assertFalse(schemaHidden);
        assertFalse(isConnectionPropertiesHidden);
        assertFalse(isQueryCriteriaHidden);
        assertFalse(isMaxRetrieveHidden);
        assertFalse(isPageSizeHidden);
        assertFalse(isUseQueryOptionHidden);

        assertTrue(isQueryLiteralTypeHidden);
        assertTrue(isQueryOptionNameHidden);
        assertTrue(isQueryOptionLiteralsHidden);
    }

    @Test
    public void testUseExistedConnectionHideConnectionWidget() {
        MarkLogicConnectionProperties someConnection = new MarkLogicConnectionProperties("connection");

        testInputProperties.setupProperties();
        testInputProperties.connection.init();
        testInputProperties.datasetProperties.init();
        testInputProperties.inputSchema.init();
        testInputProperties.setupLayout();

        someConnection.init();
        testInputProperties.connection.referencedComponent.setReference(someConnection);
        testInputProperties.connection.referencedComponent.componentInstanceId.setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");
        testInputProperties.refreshLayout(testInputProperties.getForm(Form.MAIN));

        boolean isConnectionHostPropertyHidden = testInputProperties.connection.getForm(Form.MAIN).getWidget(testInputProperties.connection.host).isHidden();
        boolean isConnectionPortPropertyHidden = testInputProperties.connection.getForm(Form.MAIN).getWidget(testInputProperties.connection.port).isHidden();
        boolean isUserNameHidden = testInputProperties.connection.getForm(Form.MAIN).getWidget(testInputProperties.connection.username).isHidden();
        boolean isPasswordHidden = testInputProperties.connection.getForm(Form.MAIN).getWidget(testInputProperties.connection.password).isHidden();
        boolean isConnectionDatabasePropertyHidden = testInputProperties.connection.getForm(Form.MAIN).getWidget(testInputProperties.connection.database).isHidden();

        assertTrue(isConnectionHostPropertyHidden);
        assertTrue(isConnectionPortPropertyHidden);
        assertTrue(isUserNameHidden);
        assertTrue(isPasswordHidden);
        assertTrue(isConnectionDatabasePropertyHidden);
    }

    @Test
    public void testAdvancedPropertiesVisibleForCriteriaMode() {
        testInputProperties.init();
        testInputProperties.criteriaSearch.setValue(false);
        testInputProperties.afterCriteriaSearch();

        boolean isMaxRetrieveHidden = testInputProperties.getForm(Form.ADVANCED).getWidget(testInputProperties.maxRetrieve).isHidden();

        Form mainDataset = testInputProperties.getForm(Form.ADVANCED).getChildForm("datasetProperties");
        boolean isPageSizeHidden = mainDataset.getWidget(testInputProperties.datasetProperties.pageSize).isHidden();
        boolean isQueryLiteralTypeHidden = mainDataset.getWidget(testInputProperties.datasetProperties.queryLiteralType).isHidden();
        boolean isQueryOptionNameHidden = mainDataset.getWidget(testInputProperties.datasetProperties.queryOptionName).isHidden();
        boolean isQueryLiteralsHidden = mainDataset.getWidget(testInputProperties.datasetProperties.queryOptionLiterals).isHidden();

        assertTrue(isMaxRetrieveHidden);
        assertTrue(isPageSizeHidden);
        assertTrue(isQueryLiteralTypeHidden);
        assertTrue(isQueryOptionNameHidden);
        assertTrue(isQueryLiteralsHidden);
    }

    @Test
    public void testGetConnectionProperties() {
        MarkLogicConnectionProperties connectionProperties = new MarkLogicConnectionProperties("connectionProperties");
        connectionProperties.init();
        testInputProperties.init();
        testInputProperties.connection.referencedComponent.setReference(connectionProperties);

        assertEquals(connectionProperties, testInputProperties.getConnectionProperties());
    }
}
