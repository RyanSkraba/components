// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.tmarketocampaign;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoCampaignPropertiesTest {

    TMarketoCampaignProperties props;

    @Before
    public void setUp() throws Exception {
        props = new TMarketoCampaignProperties("test");
        props.connection.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.setupProperties();
        props.setupLayout();
    }

    @Test
    public void testCampaign() throws Exception {
        props.campaignAction.setValue(CampaignAction.getById);
        props.afterCampaignAction();
        assertEquals(MarketoConstants.getCampaignSchema(), props.schemaInput.schema.getValue());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignAction.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignId.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.campaignIds.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.campaignNames.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.programNames.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.workspaceNames.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        props.campaignAction.setValue(CampaignAction.get);
        props.afterCampaignAction();
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignAction.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.campaignId.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignIds.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignNames.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.programNames.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.workspaceNames.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        //
        props.campaignAction.setValue(CampaignAction.schedule);
        props.afterCampaignAction();
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignAction.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignId.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.cloneToProgramName.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.runAt.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignTokens.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.triggerCampaignForLeadsInBatch.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        assertEquals(MarketoConstants.scheduleCampaignSchema(), props.schemaInput.schema.getValue());
        assertEquals(2, props.schemaFlow.schema.getValue().getFields().size());
        props.campaignAction.setValue(CampaignAction.trigger);
        props.afterCampaignAction();
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignAction.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignId.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.cloneToProgramName.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.runAt.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.campaignTokens.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.triggerCampaignForLeadsInBatch.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        props.triggerCampaignForLeadsInBatch.setValue(true);
        props.afterTriggerCampaignForLeadsInBatch();
        assertTrue(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.batchSize.getName()).isVisible());
        assertEquals(MarketoConstants.triggerCampaignSchema(), props.schemaInput.schema.getValue());
        assertEquals(MarketoConstants.triggerCampaignSchemaFlow(), props.schemaFlow.schema.getValue());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertThat(props.getAllSchemaPropertiesConnectors(true),
                Matchers.is(Collections.singleton(props.MAIN_CONNECTOR)));
        assertThat(props.getAllSchemaPropertiesConnectors(false),
                Matchers.is(Collections.singleton(props.FLOW_CONNECTOR)));
    }

    @Test
    public void testGetVersionNumber() throws Exception {
        assertThat(props.getVersionNumber(), Matchers.is(1));
    }

    @Test
    public void testCampaignActions() throws Exception {
        assertEquals(CampaignAction.get, CampaignAction.valueOf("get"));
        assertEquals(CampaignAction.getById, CampaignAction.valueOf("getById"));
        assertEquals(CampaignAction.schedule, CampaignAction.valueOf("schedule"));
        assertEquals(CampaignAction.trigger, CampaignAction.valueOf("trigger"));
    }
}
