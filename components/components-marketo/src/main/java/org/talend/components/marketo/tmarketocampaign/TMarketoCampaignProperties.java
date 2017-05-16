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
package org.talend.components.marketo.tmarketocampaign;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.helpers.TokenTable;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class TMarketoCampaignProperties extends MarketoComponentProperties {

    public enum CampaignAction {
        get,
        getById,
        schedule,
        trigger
    }

    public Property<CampaignAction> campaignAction = newEnum("campaignAction", CampaignAction.class);

    public Property<Integer> campaignId = newInteger("campaignId").setRequired();

    public Property<String> campaignIds = newString("campaignIds");

    public Property<String> campaignNames = newString("campaignNames");

    public Property<String> programNames = newString("programNames");

    public Property<String> workspaceNames = newString("workspaceNames");

    public Property<String> cloneToProgramName = newString("cloneToProgramName");

    public Property<Date> runAt = newProperty(DATE_TYPE_LITERAL, "runAt");

    public Property<Boolean> triggerCampaignForLeadsInBatch = newBoolean("triggerCampaignForLeadsInBatch");

    public TokenTable campaignTokens = new TokenTable("campaignTokens");

    private static final TypeLiteral<Date> DATE_TYPE_LITERAL = new TypeLiteral<Date>() {
    };

    public TMarketoCampaignProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        // REST Only
        connection.apiMode.setPossibleValues(APIMode.REST);
        connection.apiMode.setValue(APIMode.REST);
        //
        campaignAction.setPossibleValues((Object[]) CampaignAction.values());
        campaignAction.setValue(CampaignAction.get);
        triggerCampaignForLeadsInBatch.setValue(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(campaignAction);
        mainForm.addRow(campaignId);
        mainForm.addRow(campaignIds);
        mainForm.addRow(campaignNames);
        mainForm.addRow(programNames);
        mainForm.addRow(workspaceNames);
        mainForm.addRow(cloneToProgramName);
        mainForm.addRow(runAt);
        mainForm.addRow(widget(campaignTokens).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(triggerCampaignForLeadsInBatch);
        mainForm.addRow(batchSize);
        mainForm.addRow(dieOnError);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        switch (campaignAction.getValue()) {
        case get:
        case getById:
            schemaInput.schema.setValue(MarketoConstants.getCampaignSchema());
            schemaFlow.schema.setValue(MarketoConstants.getCampaignSchema());
            break;
        case schedule:
            schemaInput.schema.setValue(MarketoConstants.scheduleCampaignSchema());
            schemaFlow.schema.setValue(MarketoConstants.scheduleCampaignSchema());
            break;
        case trigger:
            schemaInput.schema.setValue(MarketoConstants.triggerCampaignSchema());
            if (triggerCampaignForLeadsInBatch.getValue()) {
                schemaFlow.schema.setValue(MarketoConstants.getEmptySchema());
            } else {
                schemaFlow.schema.setValue(MarketoConstants.triggerCampaignSchemaFlow());
            }
            break;
        }

        if (form.getName().equals(Form.MAIN)) {
            // first hide everything
            form.getWidget(campaignId.getName()).setVisible(false);
            form.getWidget(campaignIds.getName()).setVisible(false);
            form.getWidget(campaignNames.getName()).setVisible(false);
            form.getWidget(programNames.getName()).setVisible(false);
            form.getWidget(workspaceNames.getName()).setVisible(false);
            form.getWidget(cloneToProgramName.getName()).setVisible(false);
            form.getWidget(runAt.getName()).setVisible(false);
            form.getWidget(campaignTokens.getName()).setVisible(false);
            form.getWidget(triggerCampaignForLeadsInBatch.getName()).setVisible(false);
            form.getWidget(batchSize.getName()).setVisible(false);
            switch (campaignAction.getValue()) {
            case get:
                form.getWidget(campaignIds.getName()).setVisible(true);
                form.getWidget(campaignNames.getName()).setVisible(true);
                form.getWidget(programNames.getName()).setVisible(true);
                form.getWidget(workspaceNames.getName()).setVisible(true);
                form.getWidget(batchSize.getName()).setVisible(true);
                break;
            case getById:
                form.getWidget(campaignId.getName()).setVisible(true);
                break;
            case schedule:
                form.getWidget(campaignId.getName()).setVisible(true);
                form.getWidget(runAt.getName()).setVisible(true);
                form.getWidget(cloneToProgramName.getName()).setVisible(true);
                form.getWidget(campaignTokens.getName()).setVisible(true);
                break;
            case trigger:
                form.getWidget(campaignId.getName()).setVisible(true);
                form.getWidget(campaignTokens.getName()).setVisible(true);
                form.getWidget(triggerCampaignForLeadsInBatch.getName()).setVisible(true);
                form.getWidget(batchSize.getName()).setVisible(triggerCampaignForLeadsInBatch.getValue());
                break;
            }
        }
    }

    public void afterCampaignAction() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterTriggerCampaignForLeadsInBatch() {
        refreshLayout(getForm(Form.MAIN));
    }

}
