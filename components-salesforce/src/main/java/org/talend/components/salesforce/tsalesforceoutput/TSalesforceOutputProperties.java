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

import static org.talend.components.api.properties.presentation.Widget.*;
import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceOutputProperties extends SalesforceConnectionModuleProperties {

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

    public SchemaElement outputAction = newEnum("outputAction", ACTION_INSERT, ACTION_UPDATE, ACTION_UPSERT,
            ACTION_DELETE); //$NON-NLS-1$

    public SchemaElement upsertKeyColumn = newString("upsertKeyColumn"); //$NON-NLS-1$

    //
    // Advanced
    //
    public SchemaElement extendInsert = newBoolean("extendInsert"); //$NON-NLS-1$

    public SchemaElement ceaseForError = newBoolean("ceaseForError"); //$NON-NLS-1$

    public SchemaElement ignoreNull = newBoolean("ignoreNull"); //$NON-NLS-1$

    public SchemaElement retrieveInsertId = newString("retrieveInsertId"); //$NON-NLS-1$

    public SchemaElement commitLevel = newString("commitLevel"); //$NON-NLS-1$

    // FIXME - should be file
    public SchemaElement logFileName = newString("logFileName"); //$NON-NLS-1$

    public SchemaElement upsertRelation = newProperty("upsertRelation").setOccurMaxTimes(INFINITE); //$NON-NLS-1$

    //
    // Collections
    //
    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow"); //$NON-NLS-1$

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject"); //$NON-NLS-1$

    public TSalesforceOutputProperties(String name) {
        super(name);
    }

    // Have to use an explicit class to get the override of afterModuleName(), an anonymous
    // class cannot be public and thus cannot be called.
    public class ModuleSubclass extends SalesforceModuleProperties {

        public ModuleSubclass(String name) {
            super(name);
        }

        @Override public void afterModuleName() throws Exception {
            super.afterModuleName();
            Schema s = (Schema) schema.getValue(schema.schema);
            // FIXME - we probably only want the names, not the SchemaElements
            upsertKeyColumn.setPossibleValues(s.getRoot().getChildren());
            upsertRelation.getChild("columnName").setPossibleValues(s.getRoot().getChildren());
        }
    }

    public static final boolean POLY = true;

    public static void setupUpsertRelation(SchemaElement ur, boolean poly) {
        // They might have been set previously in some inheritance cases
        ur.setChildren(null);
        ur.addChild(newProperty("columnName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldModuleName")); //$NON-NLS-1$
        if (poly) {
            ur.addChild(newProperty(Type.BOOLEAN, "polymorphic")); //$NON-NLS-1$
        }
        ur.addChild(newProperty("lookupFieldExternalIdName")); //$NON-NLS-1$
    }

    @Override public ComponentProperties init() {
        returns = setReturnsProperty();
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_LINE"); //$NON-NLS-1$
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_SUCCESS"); //$NON-NLS-1$
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_REJECT"); //$NON-NLS-1$

        schemaReject.addChild(newProperty("errorCode")); //$NON-NLS-1$
        schemaReject.addChild(newProperty("errorFields")); //$NON-NLS-1$
        schemaReject.addChild(newProperty("errorMessage")); //$NON-NLS-1$

        setupUpsertRelation(upsertRelation, !POLY);

        super.init();
        module = new ModuleSubclass("module").setConnection(connection);
        module.init();
        return this;
    }

    @Override public void setupLayout() {
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
