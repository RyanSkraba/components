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
package org.talend.components.marketo.tmarketooutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OperationType;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OutputOperation;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoOutputPropertiesTest {

    TMarketoOutputProperties props;

    @Before
    public void setup() {
        props = new TMarketoOutputProperties("test");
        props.connection.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupProperties();
        props.setupLayout();
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        Set<PropertyPathConnector> connectors = new HashSet<>(Arrays.asList(props.FLOW_CONNECTOR, props.REJECT_CONNECTOR));
        assertEquals(connectors, props.getAllSchemaPropertiesConnectors(true));
        assertEquals(Collections.singleton(props.MAIN_CONNECTOR), props.getAllSchemaPropertiesConnectors(false));
    }

    @Test
    public void testEnums() {
        assertEquals(OutputOperation.syncLead, OutputOperation.valueOf("syncLead"));
        assertEquals(OutputOperation.syncMultipleLeads, OutputOperation.valueOf("syncMultipleLeads"));

        assertEquals(OperationType.createOnly, OperationType.valueOf("createOnly"));
        assertEquals(OperationType.updateOnly, OperationType.valueOf("updateOnly"));
        assertEquals(OperationType.createOrUpdate, OperationType.valueOf("createOrUpdate"));
        assertEquals(OperationType.createDuplicate, OperationType.valueOf("createDuplicate"));

        assertEquals(RESTLookupFields.id, RESTLookupFields.valueOf("id"));
        assertEquals(RESTLookupFields.cookie, RESTLookupFields.valueOf("cookie"));
        assertEquals(RESTLookupFields.email, RESTLookupFields.valueOf("email"));
        assertEquals(RESTLookupFields.twitterId, RESTLookupFields.valueOf("twitterId"));
        assertEquals(RESTLookupFields.facebookId, RESTLookupFields.valueOf("facebookId"));
        assertEquals(RESTLookupFields.linkedInId, RESTLookupFields.valueOf("linkedInId"));
        assertEquals(RESTLookupFields.sfdcAccountId, RESTLookupFields.valueOf("sfdcAccountId"));
        assertEquals(RESTLookupFields.sfdcContactId, RESTLookupFields.valueOf("sfdcContactId"));
        assertEquals(RESTLookupFields.sfdcLeadId, RESTLookupFields.valueOf("sfdcLeadId"));
        assertEquals(RESTLookupFields.sfdcLeadOwnerId, RESTLookupFields.valueOf("sfdcLeadOwnerId"));
        assertEquals(RESTLookupFields.sfdcOpptyId, RESTLookupFields.valueOf("sfdcOpptyId"));
    }

    @Test
    public void testUpdateSchemaRelated() throws Exception {
        props.outputOperation.setValue(OutputOperation.syncLead);
        props.setupProperties();
        props.mappingInput.setupProperties();
        props.afterApiMode();
        props.schemaListener.afterSchema();
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncLead(), props.schemaInput.schema.getValue());
        props.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        props.updateSchemaRelated();
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncLead(), props.schemaInput.schema.getValue());

        props.connection.apiMode.setValue(APIMode.SOAP);
        props.afterApiMode();
        props.outputOperation.setValue(OutputOperation.syncLead);
        props.afterOutputOperation();
        props.updateSchemaRelated();
        assertEquals(MarketoConstants.getSOAPOuputSchemaForSyncLead(), props.schemaInput.schema.getValue());
        props.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        props.afterOutputOperation();
        props.updateSchemaRelated();
        assertEquals(MarketoConstants.getSOAPOuputSchemaForSyncLead(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testDefaultShema() throws Exception {
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncLead(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testTDI38543() throws Exception {
        props.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        props.afterOutputOperation();
        assertFalse(props.deDupeEnabled.getValue());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
        props.deDupeEnabled.setValue(true);
        props.afterOutputOperation();
        assertTrue(props.deDupeEnabled.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.afterApiMode();
        assertTrue(props.deDupeEnabled.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
        props.deDupeEnabled.setValue(false);
        props.afterDeDupeEnabled();
        assertFalse(props.deDupeEnabled.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
    }
}
