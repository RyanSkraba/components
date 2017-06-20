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
package org.talend.components.salesforce.tsalesforceoutputbulk;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

public class TSalesforceOutputBulkPropertiesTest extends SalesforceTestBase {

    public static final Schema DEFAULT_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .endRecord();

    private PropertiesService propertiesService;

    private TSalesforceOutputBulkProperties properties;

    @Before
    public void setUp() {
        propertiesService = new PropertiesServiceImpl();

        properties = new TSalesforceOutputBulkProperties("root");
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertNotNull(properties.schemaListener);
        assertTrue(properties.upsertRelationTable.isUsePolymorphic());
        assertFalse(properties.upsertRelationTable.isUseLookupFieldName());
    }

    @Test
    public void testSetupLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(properties.bulkFilePath.getName()));
        assertNotNull(mainForm.getWidget(properties.ignoreNull.getName()));
        assertNotNull(mainForm.getWidget(properties.schema.getName()));
        assertNotNull(mainForm.getChildForm(properties.schema.getName()).getWidget(properties.schema.schema.getName()));

        Form advForm = properties.getForm(Form.ADVANCED);
        assertNotNull(advForm.getWidget(properties.upsertRelationTable.getName()));

        Form refForm = properties.getForm(Form.REFERENCE);
        assertNotNull(refForm.getWidget(properties.append.getName()));
        assertNotNull(refForm.getWidget(properties.ignoreNull.getName()));
    }

    @Test
    public void testAfterSchema() throws Throwable {
        properties.init();

        properties.schema.schema.setValue(DEFAULT_SCHEMA);

        propertiesService.afterProperty("schema", properties.schema);

        assertThat((Iterable<String>) properties.upsertRelationTable.columnName.getPossibleValues(),
                contains("Id", "Name"));
    }

    @Test
    public void testPropertiesConnectors() {

        assertThat(properties.getPossibleConnectors(false), containsInAnyOrder(
                (Connector) new PropertyPathConnector(Connector.MAIN_NAME, "schema")));

        assertThat(properties.getPossibleConnectors(true), empty());
    }

}
