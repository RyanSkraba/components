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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.VirtualComponentProperties;
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
        
        if(Form.ADVANCED.equals(form.getName())) {
        	boolean isUpsert = ACTION_UPSERT.equals(outputAction.getValue());
        	form.getWidget(upsertRelationTable.getName()).setHidden(!isUpsert);
        }
    }

    @Override
    public ComponentProperties getInputComponentProperties() {
        outputBulkProperties.schema.schema.setValue(module.main.schema.getValue());
        outputBulkProperties.bulkFilePath.setValue(bulkFilePath.getValue());
        outputBulkProperties.upsertRelationTable.columnName.setValue(upsertRelationTable.columnName.getStoredValue());
        outputBulkProperties.upsertRelationTable.lookupFieldExternalIdName.setValue(upsertRelationTable.lookupFieldExternalIdName.getStoredValue());
        outputBulkProperties.upsertRelationTable.lookupFieldName.setValue(upsertRelationTable.lookupFieldName.getStoredValue());
        outputBulkProperties.upsertRelationTable.lookupFieldModuleName.setValue(upsertRelationTable.lookupFieldModuleName.getStoredValue());
        outputBulkProperties.upsertRelationTable.polymorphic.setValue(upsertRelationTable.polymorphic.getStoredValue());
        return outputBulkProperties;
    }

    private static final String ADD_QUOTES = "ADD_QUOTES";
    
    @Override
    public ComponentProperties getOutputComponentProperties() {
        TSalesforceBulkExecProperties bulkExecProperties = new TSalesforceBulkExecProperties("bulkExecProperties");
        bulkExecProperties.copyValuesFrom(this);
        
        bulkExecProperties.connection.referencedComponent.componentInstanceId.setTaggedValue(ADD_QUOTES, true);
        bulkExecProperties.module.connection.referencedComponent.componentInstanceId.setTaggedValue(ADD_QUOTES, true);
        
        //bulkExecProperties.module.main.schema.setValue(bulkExecProperties.schemaFlow.schema.getValue());
        
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
