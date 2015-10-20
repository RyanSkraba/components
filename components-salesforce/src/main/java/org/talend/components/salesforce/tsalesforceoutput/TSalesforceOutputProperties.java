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
import static org.talend.components.api.schema.SchemaFactory.*;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceInputOutputProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceOutputProperties extends SalesforceInputOutputProperties {

    public static final String ACTION_INSERT = "INSERT";

    public static final String ACTION_UPDATE = "UPDATE";

    public static final String ACTION_UPSERT = "UPSERT";

    public static final String ACTION_DELETE = "DELETE";

    public enum OutputAction {
        INSERT,
        UPDATE,
        UPSERT,
        DELETE
    }

    public SchemaElement outputAction = newProperty(SchemaElement.Type.ENUM, "outputAction"); //$NON-NLS-1$

    public SchemaElement upsertKeyColumn = newProperty("upsertKeyColumn"); //$NON-NLS-1$

    //
    // Advanced
    //
    public SchemaElement extendInsert = newProperty(SchemaElement.Type.BOOLEAN, "extendInsert"); //$NON-NLS-1$

    public SchemaElement ceaseForError = newProperty(SchemaElement.Type.BOOLEAN, "ceaseForError"); //$NON-NLS-1$

    public SchemaElement ignoreNull = newProperty(SchemaElement.Type.BOOLEAN, "ignoreNull"); //$NON-NLS-1$

    public SchemaElement retrieveInsertId = newProperty("retrieveInsertId"); //$NON-NLS-1$

    public SchemaElement commitLevel = newProperty("commitLevel"); //$NON-NLS-1$

    // FIXME - should be file
    public SchemaElement logFileName = newProperty("logFileName"); //$NON-NLS-1$

    public SchemaElement upsertRelation = newProperty("upsertRelation").setOccurMaxTimes(-1); //$NON-NLS-1$

    //
    // Collections
    //
    public SchemaProperties schemaFlow = (SchemaProperties) new SchemaProperties().setName("schemaFlow"); //$NON-NLS-1$

    public SchemaProperties schemaReject = (SchemaProperties) new SchemaProperties().setName("schemaReject"); //$NON-NLS-1$

    // Have to use an explicit class to get the override of afterModuleName(), an anonymous
    // class cannot be public and thus cannot be called.
    public class ModuleSubclass extends SalesforceModuleProperties {

        @Override
        public void afterModuleName() throws Exception {
            super.afterModuleName();
            Schema s = (Schema) schema.getValue(schema.schema);
            // FIXME - we probably only want the names, not the SchemaElements
            upsertKeyColumn.setPossibleValues(s.getRoot().getChildren());
            upsertRelation.getChild("columnName").setPossibleValues(s.getRoot().getChildren());
        }
    }

    public static void setupUpsertRelation(SchemaElement ur) {
        ur.addChild(newProperty("columnName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldModuleName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldExternalIdName")); //$NON-NLS-1$
    }

    @Override
    public ComponentProperties init() {
        List<String> outputActions = new ArrayList<>();
        outputActions.add(ACTION_INSERT);
        outputActions.add(ACTION_UPDATE);
        outputActions.add(ACTION_UPSERT);
        outputActions.add(ACTION_DELETE);
        outputAction.setPossibleValues(outputActions);

        returns = setReturnsProperty();
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_LINE"); //$NON-NLS-1$
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_SUCCESS"); //$NON-NLS-1$
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_REJECT"); //$NON-NLS-1$

        schemaReject.addChild(newProperty("errorCode")); //$NON-NLS-1$
        schemaReject.addChild(newProperty("errorFields")); //$NON-NLS-1$
        schemaReject.addChild(newProperty("errorMessage")); //$NON-NLS-1$

        setupUpsertRelation(upsertRelation);
        super.init();
        module = new ModuleSubclass().setConnection(connection);
        module.init();
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(outputAction);
        mainForm.addColumn(upsertKeyColumn);

        Form advancedForm = Form.create(this, Form.ADVANCED, "Advanced");
        advancedForm.addRow(extendInsert);
        advancedForm.addRow(ceaseForError);
        advancedForm.addRow(ignoreNull);
        advancedForm.addRow(commitLevel);
        advancedForm.addRow(logFileName);
        advancedForm.addColumn(retrieveInsertId);
        advancedForm.addRow(widget(upsertRelation).setWidgetType(Widget.WidgetType.TABLE));
        advancedForm.addRow(widget(schemaFlow.getForm(Form.REFERENCE).setName("SchemaFlow").setTitle("Schema Flow")));
        advancedForm.addRow(widget(schemaReject.getForm(Form.REFERENCE).setName("SchemaReject").setTitle("Schema Reject")));
    }

    public void afterOutputAction() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.ADVANCED)) {
            ((Schema) schemaFlow.getValue(schemaFlow.schema)).setRoot(null);
            if (!getBooleanValue(extendInsert) && getStringValue(retrieveInsertId) != null
                    && getValue(outputAction) == OutputAction.INSERT) {
                schemaFlow.addChild(newProperty("salesforce_id"));
            }
        }
        if (form.getName().equals(Form.MAIN)) {
            Form advForm = getForm(Form.ADVANCED);
            if (advForm != null) {
                if (ACTION_UPSERT.equals(getValue(outputAction))) {
                    form.getWidget("upsertKeyColumn").setVisible(true);
                    advForm.getWidget("upsertRelation").setVisible(true);
                } else {
                    form.getWidget("upsertKeyColumn").setVisible(false);
                    advForm.getWidget("upsertRelation").setVisible(false);
                }
            }
        }

    }

}
