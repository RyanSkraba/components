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
package org.talend.components.salesforce;

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentPropertyFactory;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.talend6.Talend6SchemaConstants;

public class SalesforceOutputProperties extends SalesforceConnectionModuleProperties {

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
    public Property upsertRelation = newProperty("upsertRelation").setOccurMaxTimes(Property.INFINITE); //$NON-NLS-1$

    //
    // Collections
    //
    protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject"); //$NON-NLS-1$

    public SalesforceOutputProperties(String name) {
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
            List<String> fieldNames = getFieldNames(main.schema);
            upsertKeyColumn.setPossibleValues(fieldNames);
            upsertRelation.getChild("columnName").setPossibleValues(fieldNames);
            return validationResult;
        }
    }

    public static final boolean POLY = true;

    public void beforeUpsertKeyColumn() {
        upsertKeyColumn.setPossibleValues(getFieldNames(module.main.schema));
    }

    public void beforeUpsertRelation() {
        upsertRelation.getChild("columnName").setPossibleValues(getFieldNames(module.main.schema));
    }

    protected void setupUpsertRelation(Property ur) {
        // They might have been set previously in some inheritance cases
        ur.setChildren(new ArrayList<Property>());
        ur.addChild(newProperty("columnName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldModuleName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldExternalIdName")); //$NON-NLS-1$
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        outputAction.setValue(ACTION_INSERT);

        returns = ComponentPropertyFactory.newReturnsProperty();
        ComponentPropertyFactory.newReturnProperty(returns, Property.Type.INT, "NB_LINE"); //$NON-NLS-1$
        ComponentPropertyFactory.newReturnProperty(returns, Property.Type.INT, "NB_SUCCESS"); //$NON-NLS-1$
        ComponentPropertyFactory.newReturnProperty(returns, Property.Type.INT, "NB_REJECT"); //$NON-NLS-1$

        setupRejectSchema();

        setupUpsertRelation(upsertRelation);

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

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(widget(upsertRelation).setWidgetType(Widget.WidgetType.TABLE));
        advancedForm.addRow(widget(schemaReject.getForm(Form.REFERENCE).setName("SchemaReject").setTitle("Schema Reject")));// TODO
        // check
        // I18N
    }

    public void afterOutputAction() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            Form advForm = getForm(Form.ADVANCED);
            if (advForm != null) {
                boolean isUpsert = ACTION_UPSERT.equals(outputAction.getValue());
                form.getWidget(upsertKeyColumn.getName()).setHidden(!isUpsert);
                advForm.getWidget(upsertRelation.getName()).setHidden(!isUpsert);
            }
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            HashSet<PropertyPathConnector> ouputConnectors = new HashSet<>();
            ouputConnectors.add(MAIN_CONNECTOR);
            ouputConnectors.add(REJECT_CONNECTOR);
            return ouputConnectors;
        } else {
            return Collections.EMPTY_SET;
        }
    }

    protected List<String> getFieldNames(Property schema) {
        Schema s = (Schema) schema.getValue();
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : s.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

    protected void setupRejectSchema() {
        // left empty for subclass to override
    }

}
