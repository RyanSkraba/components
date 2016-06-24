// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.tsalesforceoutputbulkexec;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.VirtualComponentProperties;
import org.talend.components.salesforce.UpsertRelationTable;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.properties.presentation.Form;

public class TSalesforceOutputBulkExecProperties extends TSalesforceBulkExecProperties implements VirtualComponentProperties {

    public TSalesforceOutputBulkExecProperties(String name) {
        super(name);
    }

    public TSalesforceOutputBulkProperties outputBulkProperties = new TSalesforceOutputBulkProperties("outputBulkProperties");

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(outputBulkProperties.getForm(Form.REFERENCE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.ADVANCED.equals(form.getName())) {
            boolean isUpsert = OutputAction.UPSERT.equals(outputAction.getValue());
            form.getWidget(upsertRelationTable.getName()).setHidden(!isUpsert);
        }
    }

    @Override
    public ComponentProperties getInputComponentProperties() {
        outputBulkProperties.init();

        outputBulkProperties.schema.schema.setStoredValue(module.main.schema.getStoredValue());
        outputBulkProperties.schema.schema.setValueEvaluator(module.main.schema.getValueEvaluator());

        outputBulkProperties.bulkFilePath.setStoredValue(bulkFilePath.getStoredValue());
        outputBulkProperties.bulkFilePath.copyTaggedValues(bulkFilePath);
        outputBulkProperties.bulkFilePath.setValueEvaluator(bulkFilePath.getValueEvaluator());

        // we need to pass also the possible values, only way from the studio to know it comes from a combo box (need to
        // add quotes for generation)
        outputBulkProperties.upsertRelationTable.columnName.setPossibleValues(upsertRelationTable.columnName.getPossibleValues());
        outputBulkProperties.upsertRelationTable.columnName.setStoredValue(upsertRelationTable.columnName.getStoredValue());
        outputBulkProperties.upsertRelationTable.columnName.setValueEvaluator(upsertRelationTable.columnName.getValueEvaluator());
        outputBulkProperties.upsertRelationTable.lookupFieldExternalIdName
                .setStoredValue(upsertRelationTable.lookupFieldExternalIdName.getStoredValue());
        outputBulkProperties.upsertRelationTable.lookupFieldExternalIdName
                .setValueEvaluator(upsertRelationTable.lookupFieldExternalIdName.getValueEvaluator());
        outputBulkProperties.upsertRelationTable.lookupRelationshipFieldName
                .setStoredValue(upsertRelationTable.lookupRelationshipFieldName.getStoredValue());
        outputBulkProperties.upsertRelationTable.lookupRelationshipFieldName
                .setValueEvaluator(upsertRelationTable.lookupRelationshipFieldName.getValueEvaluator());
        outputBulkProperties.upsertRelationTable.lookupFieldModuleName
                .setStoredValue(upsertRelationTable.lookupFieldModuleName.getStoredValue());
        outputBulkProperties.upsertRelationTable.lookupFieldModuleName
                .setValueEvaluator(upsertRelationTable.lookupFieldModuleName.getValueEvaluator());
        outputBulkProperties.upsertRelationTable.polymorphic.setStoredValue(upsertRelationTable.polymorphic.getStoredValue());
        outputBulkProperties.upsertRelationTable.polymorphic
                .setValueEvaluator(upsertRelationTable.polymorphic.getValueEvaluator());
        for (Form form : outputBulkProperties.getForms()) {
            outputBulkProperties.refreshLayout(form);
        }
        return outputBulkProperties;
    }

    @Override
    public ComponentProperties getOutputComponentProperties() {
        TSalesforceBulkExecProperties bulkExecProperties = new TSalesforceBulkExecProperties("bulkExecProperties");

        bulkExecProperties.init();
        bulkExecProperties.copyValuesFrom(this, true, true);

        // we need to pass also the possible values, only way from the studio to know it comes from a combo box (need to
        // add quotes for generation)
        bulkExecProperties.upsertRelationTable.columnName.setPossibleValues(upsertRelationTable.columnName.getPossibleValues());

        bulkExecProperties.connection.referencedComponent.componentInstanceId.setTaggedValue(UpsertRelationTable.ADD_QUOTES,
                true);
        bulkExecProperties.module.connection.referencedComponent.componentInstanceId
                .setTaggedValue(UpsertRelationTable.ADD_QUOTES, true);

        for (Form form : bulkExecProperties.getForms()) {
            bulkExecProperties.refreshLayout(form);
        }
        return bulkExecProperties;
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
            connectors.add(REJECT_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }
}
