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

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

import static org.talend.components.api.schema.SchemaFactory.newSchemaElement;

public class TSalesforceOutputProperties extends ComponentProperties {

    public enum OutputAction {
        INSERT,
        UPDATE,
        UPSERT,
        DELETE
    }

    public SchemaElement outputAction = newSchemaElement(SchemaElement.Type.ENUM, "outputAction", "Output Action");

    public SchemaElement upsertKeyColumn = newSchemaElement("upsertKeyColumn", "Upsert Key Column");

    //
    // Advanced
    //
    public SchemaElement extendInsert = newSchemaElement(SchemaElement.Type.BOOLEAN, "extendInsert", "Extend Insert");

    public SchemaElement ceaseForError = newSchemaElement(SchemaElement.Type.BOOLEAN, "ceaseForError", "Cease on Error");

    public SchemaElement ignoreNull = newSchemaElement(SchemaElement.Type.BOOLEAN, "ignoreNull", "Ignore Null");

    public SchemaElement retrieveInsertId = newSchemaElement("retrieveInsertId", "Retrieve Insert Id");

    public SchemaElement commitLevel = newSchemaElement("commitLevel", "Commit Level");

    // FIXME - should be file
    public SchemaElement logFileName = newSchemaElement("logFileName", "Log File Name");

    // FIXME - need upsertRelation property which is a table

    //
    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties();

    public SalesforceModuleProperties module = new SalesforceModuleProperties(connection);

    public SchemaProperties schemaFlow = new SchemaProperties();

    public SchemaProperties schemaReject = new SchemaProperties();

    public TSalesforceOutputProperties() {
        schemaReject.addRow(newSchemaElement("errorCode"));
        schemaReject.addRow(newSchemaElement("errorFields"));
        schemaReject.addRow(newSchemaElement("errorMessage"));

        setupLayout();
    }

    public static final String MAIN = "Main";

    public static final String ADVANCED = "Advanced";

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, MAIN, "Salesforce Output");
        mainForm.addRow(connection.getForm(SalesforceConnectionProperties.MAIN));
        mainForm.addRow(module.getForm(SalesforceModuleProperties.REFERENCE));
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

        ((Schema) schemaFlow.getValue(schemaFlow.schema)).setRoot(null);
        if (!getBooleanValue(extendInsert) && getStringValue(retrieveInsertId) != null
                && getValue(outputAction) == OutputAction.INSERT) {
            schemaFlow.addRow(newSchemaElement("salesforce_id"));
        }

    }

}
