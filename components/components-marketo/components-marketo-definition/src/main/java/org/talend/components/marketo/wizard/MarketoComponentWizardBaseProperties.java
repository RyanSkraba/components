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
package org.talend.components.marketo.wizard;

import static org.talend.components.marketo.MarketoComponentDefinition.RUNTIME_SOURCEORSINK_CLASS;
import static org.talend.components.marketo.MarketoComponentDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarketoComponentWizardBaseProperties extends MarketoComponentProperties {

    public Property<String> customObjectName = newString("customObjectName").setRequired();

    public Property<InputOperation> inputOperation = newEnum("inputOperation", InputOperation.class).setRequired();

    public Property<CustomObjectAction> customObjectAction = newEnum("customObjectAction", CustomObjectAction.class);

    public Property<OutputOperation> outputOperation = newEnum("outputOperation", OutputOperation.class);

    public Property<CustomObjectSyncAction> customObjectSyncAction = newEnum("customObjectSyncAction",
            CustomObjectSyncAction.class);

    private static final Logger LOG = LoggerFactory.getLogger(MarketoComponentWizardBaseProperties.class);

    public MarketoComponentWizardBaseProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        return null;
    }

    public enum InputOperation {
        getLead, // retrieves basic information of leads and lead activities in Marketo DB. getLead:
        getMultipleLeads, // retrieves lead records in batch.
        getLeadActivity, // retrieves the history of activity records for a single lead identified by the provided key.
        getLeadChanges, // checks the changes on Lead data in Marketo DB.
        CustomObject, // CO Operation
        Company, //
        Opportunity, //
        OpportunityRole //
    }

    /**
     * Custom objects
     */
    public enum CustomObjectAction {
        describe,
        list,
        get
    }

    public enum OutputOperation {
        syncLead, // This operation requests an insert or update operation for a lead record.
        syncMultipleLeads, // This operation requests an insert or update operation for lead records in batch.
        deleteLeads, // REST only
        syncCustomObjects, // REST only
        deleteCustomObjects, // REST only
        syncCompanies,
        deleteCompanies,
        syncOpportunities,
        deleteOpportunities,
        syncOpportunityRoles,
        deleteOpportunityRoles
    }

    public enum CustomObjectSyncAction {
        createOnly,
        updateOnly,
        createOrUpdate
    }

    protected SandboxedInstance getRuntimeSandboxedInstance() {
        return getSandboxedInstance(RUNTIME_SOURCEORSINK_CLASS);
    }

}
