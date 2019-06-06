// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.CustomObjectDeleteBy;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OperationType;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectSyncAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.serialize.PostDeserializeSetup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.deleteCompanies;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.deleteOpportunities;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.deleteOpportunityRoles;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncCompanies;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncLead;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncMultipleLeads;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncOpportunities;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncOpportunityRoles;

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
        props.mappingInput.setupProperties();
        props.mappingInput.setupLayout();
        props.afterOutputOperation();
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        Set<PropertyPathConnector> connectors = new HashSet<>(Arrays.asList(props.FLOW_CONNECTOR, props.REJECT_CONNECTOR));
        assertEquals(connectors, props.getAllSchemaPropertiesConnectors(true));
        assertEquals(Collections.singleton(props.MAIN_CONNECTOR), props.getAllSchemaPropertiesConnectors(false));
    }

    @Test
    public void testEnums() {
        assertEquals(syncLead, OutputOperation.valueOf("syncLead"));
        assertEquals(syncMultipleLeads, OutputOperation.valueOf("syncMultipleLeads"));

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
        assertEquals(RESTLookupFields.Custom, RESTLookupFields.valueOf("Custom"));

        assertEquals(CustomObjectDeleteBy.idField, CustomObjectDeleteBy.valueOf("idField"));
        assertEquals(CustomObjectDeleteBy.dedupeFields, CustomObjectDeleteBy.valueOf("dedupeFields"));
    }

    @Test
    public void testUpdateSchemaRelated() throws Exception {
        props.outputOperation.setValue(syncLead);
        props.setupProperties();
        props.mappingInput.setupProperties();
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncLead(), props.schemaInput.schema.getValue());
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncLead().getFields(),
                props.schemaFlow.schema.getValue().getFields());
        props.outputOperation.setValue(syncMultipleLeads);
        props.batchSize.setValue(1);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncMultipleLeads(), props.schemaInput.schema.getValue());
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncMultipleLeads().getFields(),
                props.schemaFlow.schema.getValue().getFields());
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncMultipleLeads().getFields().size(),
                props.schemaReject.schema.getValue().getFields().size());

        props.connection.apiMode.setValue(APIMode.SOAP);
        props.updateSchemaRelated();
        props.outputOperation.setValue(syncLead);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getSOAPOutputSchemaForSyncLead(), props.schemaInput.schema.getValue());
        assertEquals(MarketoConstants.getSOAPOutputSchemaForSyncLead().getFields(),
                props.schemaFlow.schema.getValue().getFields());
        assertEquals(MarketoConstants.getSOAPOutputSchemaForSyncLead().getFields().size() + 1,
                props.schemaReject.schema.getValue().getFields().size());
        props.outputOperation.setValue(syncMultipleLeads);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getSOAPOutputSchemaForSyncMultipleLeads(), props.schemaInput.schema.getValue());
        assertEquals(MarketoConstants.getSOAPOutputSchemaForSyncMultipleLeads().getFields(),
                props.schemaFlow.schema.getValue().getFields());
        assertEquals(MarketoConstants.getSOAPOutputSchemaForSyncMultipleLeads().getFields(),
                props.schemaReject.schema.getValue().getFields());
        //
        props.connection.apiMode.setValue(APIMode.REST);
        props.outputOperation.setValue(syncCompanies);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getCompanySyncSchema(), props.schemaInput.schema.getValue());

        props.outputOperation.setValue(syncOpportunities);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getOpportunitySyncSchema(), props.schemaInput.schema.getValue());

        props.outputOperation.setValue(syncOpportunityRoles);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getOpportunityRoleSyncSchema(), props.schemaInput.schema.getValue());

        props.outputOperation.setValue(deleteCompanies);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getCompanySyncSchema(), props.schemaInput.schema.getValue());

        props.outputOperation.setValue(deleteOpportunities);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getOpportunitySyncSchema(), props.schemaInput.schema.getValue());

        props.outputOperation.setValue(deleteOpportunityRoles);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getOpportunityRoleSyncSchema(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testDefaultShema() throws Exception {
        assertEquals(MarketoConstants.getRESTOutputSchemaForSyncLead(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testTDI38543() throws Exception {
        props.outputOperation.setValue(syncMultipleLeads);
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
        props.updateSchemaRelated();
        assertTrue(props.deDupeEnabled.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
        props.deDupeEnabled.setValue(false);
        props.afterDeDupeEnabled();
        assertFalse(props.deDupeEnabled.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
    }

    @Test
    public void testDeleteLeads() throws Exception {
        props.outputOperation.setValue(OutputOperation.deleteLeads);
        props.afterOutputOperation();
        assertFalse(props.getForm(Form.MAIN).getWidget(props.mappingInput.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.operationType.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.lookupField.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.deDupeEnabled.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.customObjectDedupeBy.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.customObjectDeleteBy.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.customObjectName.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.customObjectSyncAction.getName()).isVisible());
        //
        assertTrue(props.getForm(Form.MAIN).getWidget(props.deleteLeadsInBatch.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        //
        props.deleteLeadsInBatch.setValue(true);
        props.afterDeleteLeadsInBatch();
        assertTrue(props.getForm(Form.MAIN).getWidget(props.deleteLeadsInBatch.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
    }

    @Test
    public void testDeleteLeadsSchemas() throws Exception {
        props.outputOperation.setValue(OutputOperation.deleteLeads);
        props.afterOutputOperation();
        assertEquals(MarketoConstants.getDeleteLeadsSchema(), props.schemaInput.schema.getValue());
        Schema flow = props.schemaFlow.schema.getValue();
        assertNotNull(flow.getField("Status"));
        props.deleteLeadsInBatch.setValue(true);
        props.afterDeleteLeadsInBatch();
        assertEquals(MarketoConstants.getDeleteLeadsSchema(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testCustomObjectActions() throws Exception {
        Form f = props.getForm(Form.MAIN);
        props.updateSchemaRelated();
        props.schemaListener.afterSchema();
        props.refreshLayout(f);
        assertFalse(f.getWidget(props.customObjectName.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectSyncAction.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectDeleteBy.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectDedupeBy.getName()).isVisible());
        props.outputOperation.setValue(OutputOperation.syncCustomObjects);
        props.updateSchemaRelated();
        props.schemaListener.afterSchema();
        props.refreshLayout(f);
        assertTrue(f.getWidget(props.customObjectName.getName()).isVisible());
        assertTrue(f.getWidget(props.customObjectSyncAction.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectDedupeBy.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectDeleteBy.getName()).isVisible());
        props.outputOperation.setValue(OutputOperation.deleteCustomObjects);
        props.updateSchemaRelated();
        props.schemaListener.afterSchema();
        props.refreshLayout(f);
        assertTrue(f.getWidget(props.customObjectName.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectSyncAction.getName()).isVisible());
        assertTrue(f.getWidget(props.customObjectDeleteBy.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectDedupeBy.getName()).isVisible());

        props.outputOperation.setValue(OutputOperation.syncCustomObjects);
        props.customObjectSyncAction.setValue(CustomObjectSyncAction.updateOnly);
        props.updateSchemaRelated();
        props.schemaListener.afterSchema();
        props.afterCustomObjectSyncAction();
        assertTrue(f.getWidget(props.customObjectName.getName()).isVisible());
        assertTrue(f.getWidget(props.customObjectSyncAction.getName()).isVisible());
        assertFalse(f.getWidget(props.customObjectDeleteBy.getName()).isVisible());
        assertTrue(f.getWidget(props.customObjectDedupeBy.getName()).isVisible());
    }

    @Test
    public void testValidateOutputOperation() throws Exception {
        assertEquals(Result.OK, props.validateOutputOperation().getStatus());
        props.connection.apiMode.setValue(APIMode.SOAP);
        assertEquals(Result.OK, props.validateOutputOperation().getStatus());
        props.outputOperation.setValue(OutputOperation.deleteCustomObjects);
        assertEquals(Result.ERROR, props.validateOutputOperation().getStatus());
    }

    @Test
    public void testBeforeOutputOperation() throws Exception {
        props.beforeOutputOperation();
        assertEquals(OutputOperation.values().length, props.outputOperation.getPossibleValues().size());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.beforeOutputOperation();
        assertEquals(Arrays.asList(syncLead, syncMultipleLeads), props.outputOperation.getPossibleValues());
        props.outputOperation.setValue(OutputOperation.syncCustomObjects);
        props.beforeOutputOperation();
        assertEquals(syncLead, props.outputOperation.getValue());
    }

    @Test
    public void testGetVersionNumber() throws Exception {
        assertTrue(props.getVersionNumber() > 0);
    }

    @Test
    public void testPostDeserialize() throws Exception {
        MarketoComponentWizardBaseProperties mprops = mock(MarketoComponentWizardBaseProperties.class);
        when(mprops.postDeserialize(eq(0), any(PostDeserializeSetup.class), eq(false))).thenReturn(true);
        assertFalse(props.postDeserialize(0, null, false));
        when(mprops.postDeserialize(eq(0), any(PostDeserializeSetup.class), eq(false))).thenThrow(new ClassCastException());
        assertFalse(props.postDeserialize(0, null, false));
    }

    @Test
    public void testAfterLookupField() throws Exception {
        Form f = props.getForm(Form.MAIN);
        props.refreshLayout(f);
        assertFalse(f.getWidget(props.customLookupField.getName()).isVisible());
        props.lookupField.setValue(RESTLookupFields.sfdcAccountId);
        props.afterLookupField();
        assertFalse(f.getWidget(props.customLookupField.getName()).isVisible());
        props.lookupField.setValue(RESTLookupFields.Custom);
        props.afterLookupField();
        assertTrue(f.getWidget(props.customLookupField.getName()).isVisible());
        props.outputOperation.setValue(OutputOperation.deleteLeads);
        props.afterOutputOperation();
        assertFalse(f.getWidget(props.lookupField.getName()).isVisible());
        assertFalse(f.getWidget(props.customLookupField.getName()).isVisible());
    }
}
