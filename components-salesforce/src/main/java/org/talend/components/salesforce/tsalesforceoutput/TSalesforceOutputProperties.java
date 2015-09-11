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
package org.talend.components.salesforce.tsalesforceoutput;

import static org.talend.components.api.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.ComponentSchemaFactory;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceOutputProperties extends ComponentProperties {

    public enum OutputAction {
        INSERT,
        UPDATE,
        UPSERT,
        DELETE
    }

    public Property<OutputAction>         outputAction     = new Property<>("outputAction", "Output Action");

    public Property<String>               upsertKeyColumn  = new Property<String>("upsertKeyColumn", "Upsert Key Column");

    //
    // Advanced
    //
    public Property<Boolean>              extendInsert     = new Property<>("extendInsert", "Extend Insert");

    public Property<Boolean>              ceaseForError    = new Property<>("ceaseForError", "Cease on Error");

    public Property<Boolean>              ignoreNull       = new Property<>("ignoreNull", "Ignore Null");

    public Property<Boolean>              retrieveInsertId = new Property<>("retrieveInsertId", "Retrieve Insert Id");

    public Property<String>               commitLevel      = new Property<>("commitLevel", "Commit Level");

    // FIXME - should be file
    public Property<String>               logFileName      = new Property<>("logFileName", "Log File Name");

    // FIXME - need upsertRelation property which is a table


    //
    // Collections
    //
    public SalesforceConnectionProperties connection       = new SalesforceConnectionProperties();

    public SalesforceModuleProperties     module           = new SalesforceModuleProperties(connection);

    public SchemaProperties               schema           = new SchemaProperties();

    public SchemaProperties               schemaFlow       = new SchemaProperties();

    public SchemaProperties               schemaReject     = new SchemaProperties();

    public TSalesforceOutputProperties() {
        schemaReject.addRow(ComponentSchemaFactory.getComponentSchemaElement("errorCode"));
        schemaReject.addRow(ComponentSchemaFactory.getComponentSchemaElement("errorFields"));
        schemaReject.addRow(ComponentSchemaFactory.getComponentSchemaElement("errorMessage"));

        setupLayout();
    }

    public static final String MAIN     = "Main";

    public static final String ADVANCED = "Advanced";

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, MAIN, "Salesforce Output");
        mainForm.addRow(connection.getForm(SalesforceConnectionProperties.MAIN));
        mainForm.addRow(module.getForm(SalesforceModuleProperties.REFERENCE));
        mainForm.addRow(schema.getForm(SchemaProperties.REFERENCE));
        mainForm.addRow(outputAction);
        refreshLayout(mainForm);

        Form advancedForm = Form.create(this, ADVANCED, "Advanced");
        mainForm.addRow(extendInsert);
        mainForm.addRow(ceaseForError);
        mainForm.addRow(ignoreNull);
        mainForm.addRow(commitLevel);
        mainForm.addRow(logFileName);
        mainForm.addColumn(retrieveInsertId);
        // FIXME - how is this labeled. Should we wrap this in a Property object?
        mainForm.addRow(schemaFlow.getForm(SchemaProperties.REFERENCE));
        mainForm.addRow(schemaReject.getForm(SchemaProperties.REFERENCE));
        refreshLayout(mainForm);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        schemaFlow.schema.getValue().getRoot().setChildren(null);
        if (!extendInsert.getValue() && retrieveInsertId.getValue() != null && outputAction.getValue() == OutputAction.INSERT) {
            schemaFlow.addRow(ComponentSchemaFactory.getComponentSchemaElement("salesforce_id"));
        }

    }

}
