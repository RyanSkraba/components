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

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import java.util.ArrayList;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentPropertyFactory;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.Property.Type;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

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

    public Property outputAction = newEnum("outputAction", ACTION_INSERT, ACTION_UPDATE, ACTION_UPSERT, ACTION_DELETE); // $NON-NLS-1$

    public Property upsertKeyColumn = newString("upsertKeyColumn"); //$NON-NLS-1$

    //
    // Advanced
    //
    public Property extendInsert = newBoolean("extendInsert"); //$NON-NLS-1$

    public Property ceaseForError = newBoolean("ceaseForError"); //$NON-NLS-1$

    public Property ignoreNull = newBoolean("ignoreNull"); //$NON-NLS-1$

    public Property retrieveInsertId = newString("retrieveInsertId"); //$NON-NLS-1$

    public Property commitLevel = newString("commitLevel"); //$NON-NLS-1$

    // FIXME - should be file
    public Property logFileName = newString("logFileName"); //$NON-NLS-1$

    public Property upsertRelation = (Property) newProperty("upsertRelation").setOccurMaxTimes(SchemaElement.INFINITE); //$NON-NLS-1$

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

        @Override
        public ValidationResult afterModuleName() throws Exception {
            ValidationResult validationResult = super.afterModuleName();
            Schema s = (Schema) schema.schema.getValue();
            // FIXME - we probably only want the names, not the SchemaElements
            upsertKeyColumn.setPossibleValues(s.getRoot().getChildren());
            upsertRelation.getChild("columnName").setPossibleValues(s.getRoot().getChildren());
            return validationResult;
        }
    }

    public static final boolean POLY = true;

    public static void setupUpsertRelation(Property ur, boolean poly) {
        // They might have been set previously in some inheritance cases
        ur.setChildren(new ArrayList<SchemaElement>());
        ur.addChild(newProperty("columnName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldModuleName")); //$NON-NLS-1$
        if (poly) {
            ur.addChild(newProperty(Type.BOOLEAN, "polymorphic")); //$NON-NLS-1$
        }
        ur.addChild(newProperty("lookupFieldExternalIdName")); //$NON-NLS-1$
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        returns = ComponentPropertyFactory.newReturnsProperty();
        ComponentPropertyFactory.newReturnProperty(returns, Type.INT, "NB_LINE"); //$NON-NLS-1$
        ComponentPropertyFactory.newReturnProperty(returns, Type.INT, "NB_SUCCESS"); //$NON-NLS-1$
        ComponentPropertyFactory.newReturnProperty(returns, Type.INT, "NB_REJECT"); //$NON-NLS-1$

        schemaReject.addSchemaChild(newProperty("errorCode")); //$NON-NLS-1$
        schemaReject.addSchemaChild(newProperty("errorFields")); //$NON-NLS-1$
        schemaReject.addSchemaChild(newProperty("errorMessage")); //$NON-NLS-1$

        setupUpsertRelation(upsertRelation, !POLY);

        module = new ModuleSubclass("module");
        module.connection = connection;
        module.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(outputAction);
        mainForm.addColumn(upsertKeyColumn);

        Form advancedForm = new Form(this, Form.ADVANCED);
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
            ((Schema) schemaFlow.schema.getValue()).setRoot(null);
            if (!extendInsert.getBooleanValue() && retrieveInsertId.getStringValue() != null
                    && ACTION_INSERT.equals(outputAction.getValue())) {
                schemaFlow.addSchemaChild(newProperty("salesforce_id"));
            }
        }
        if (form.getName().equals(Form.MAIN)) {
            Form advForm = getForm(Form.ADVANCED);
            if (advForm != null) {
                if (ACTION_UPSERT.equals(outputAction.getValue())) {
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
