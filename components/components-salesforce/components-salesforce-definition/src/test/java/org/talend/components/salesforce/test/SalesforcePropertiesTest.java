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
package org.talend.components.salesforce.test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties.FIELD_ERROR_MESSAGE;
import static org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties.FIELD_SALESFORCE_ID;
import static org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties.FIELD_STATUS;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class SalesforcePropertiesTest {

    public static Schema DEFAULT_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .endRecord();

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testSalesforceOutputProps() throws Throwable {
        TSalesforceOutputProperties outputProps = new TSalesforceOutputProperties("foo");
        outputProps = (TSalesforceOutputProperties) outputProps.init();
        ComponentTestUtils.checkSerialize(outputProps, errorCollector);

        // check default
        Form mainForm = outputProps.getForm(Form.MAIN);
        assertEquals(Form.MAIN, mainForm.getName());
        Form advancedForm = outputProps.getForm(Form.ADVANCED);
        assertTrue(mainForm.getWidget(outputProps.outputAction.getName()).isVisible());
        assertEquals(TSalesforceOutputProperties.OutputAction.INSERT, outputProps.outputAction.getValue());
        Schema rejectSchema = outputProps.schemaReject.schema.getValue();
        assertNotNull(rejectSchema);
        assertEquals(0, rejectSchema.getFields().size());
        Schema flowSchema = outputProps.schemaFlow.schema.getValue();
        assertNotNull(flowSchema);
        assertEquals(0, flowSchema.getFields().size());

        assertTrue(advancedForm.getWidget(outputProps.extendInsert.getName()).isVisible());
        assertTrue(outputProps.extendInsert.getValue());
        assertTrue(advancedForm.getWidget(outputProps.ceaseForError.getName()).isVisible());
        assertTrue(outputProps.ceaseForError.getValue());
        assertTrue(advancedForm.getWidget(outputProps.commitLevel.getName()).isVisible());
        assertEquals(200, outputProps.commitLevel.getValue().intValue());
        assertTrue(advancedForm.getWidget(outputProps.logFileName.getName()).isVisible());
        assertNull(outputProps.logFileName.getValue());

        // 1.After schema changed
        outputProps.module.main.schema.setValue(DEFAULT_SCHEMA);
        outputProps.module.schemaListener.afterSchema();
        rejectSchema = outputProps.schemaReject.schema.getValue();
        assertNotNull(rejectSchema);
        assertEquals(5, rejectSchema.getFields().size());
        assertEquals(4, rejectSchema.getField(FIELD_ERROR_MESSAGE).pos());
        flowSchema = outputProps.schemaFlow.schema.getValue();
        assertNotNull(flowSchema);
        assertEquals(2, flowSchema.getFields().size());

        // 2.After extendInsert unchecked
        outputProps.extendInsert.setValue(false);
        assertTrue(advancedForm.getWidget(outputProps.extendInsert.getName()).isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(outputProps, outputProps.extendInsert.getName());
        assertTrue(advancedForm.getWidget(outputProps.retrieveInsertId.getName()).isVisible());
        assertFalse(outputProps.retrieveInsertId.getValue());

        // 3.After retrieveInsertId checked
        outputProps.retrieveInsertId.setValue(true);
        assertTrue(advancedForm.getWidget(outputProps.retrieveInsertId.getName()).isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(outputProps, outputProps.retrieveInsertId.getName());
        // check schema changes
        flowSchema = outputProps.schemaFlow.schema.getValue();
        assertNotNull(flowSchema);
        assertEquals(3, flowSchema.getFields().size());
        // Field "salesforce_id" is added
        assertEquals(2, flowSchema.getField(FIELD_SALESFORCE_ID).pos());

        // 3.After retrieveInsertId checked
        outputProps.outputAction.setValue(SalesforceOutputProperties.OutputAction.UPSERT);
        assertTrue(mainForm.getWidget(outputProps.outputAction.getName()).isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(outputProps, outputProps.outputAction.getName());
        flowSchema = outputProps.schemaFlow.schema.getValue();
        assertNotNull(flowSchema);
        assertEquals(4, flowSchema.getFields().size());
        // Field "salesforce_id" is added
        assertEquals(2, flowSchema.getField(FIELD_SALESFORCE_ID).pos());
        assertEquals(3, flowSchema.getField(FIELD_STATUS).pos());

    }
}
