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

package org.talend.components.netsuite.output;

import static org.talend.components.netsuite.util.ComponentExceptions.exceptionToValidationResult;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;

import java.util.List;

import org.talend.components.netsuite.NetSuiteModuleProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 *
 */
public class NetSuiteOutputModuleProperties extends NetSuiteModuleProperties {

    public final Property<OutputAction> action = newEnum("action", OutputAction.class);

    public final Property<Boolean> useNativeUpsert = newBoolean("useNativeUpsert");

    public NetSuiteOutputModuleProperties(String name, NetSuiteConnectionProperties connectionProperties) {
        super(name, connectionProperties);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        action.setValue(OutputAction.ADD);
        useNativeUpsert.setValue(Boolean.FALSE);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE)
                .setLongRunning(true));
        mainForm.addRow(main.getForm(Form.REFERENCE));
        mainForm.addRow(action);

        Form advForm = Form.create(this, Form.ADVANCED);
        advForm.addRow(connection.getForm(Form.ADVANCED));
        advForm.addRow(useNativeUpsert);

        Form refForm = Form.create(this, Form.REFERENCE);
        refForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE)
                .setLongRunning(true));
        refForm.addRow(main.getForm(Form.REFERENCE));
        refForm.addRow(action);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(useNativeUpsert.getName()).setHidden(action.getValue() != OutputAction.UPSERT);
        }
    }

    public ValidationResult beforeModuleName() throws Exception {
        try {
            List<NamedThing> types = getRecordTypes();
            moduleName.setPossibleNamedThingValues(types);

            return ValidationResult.OK;
        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
    }

    public ValidationResult afterModuleName() throws Exception {
        try {
            setupSchema();
            return ValidationResult.OK;
        } catch (Exception e) {
            return exceptionToValidationResult(e);
        }
    }

    public ValidationResult afterAction() {
        try {
            setupSchema();

            refreshLayout(getForm(Form.MAIN));
            refreshLayout(getForm(Form.ADVANCED));

            return ValidationResult.OK;
        } catch (Exception e) {
            return exceptionToValidationResult(e);
        }
    }

    protected void setupSchema() {
        switch (action.getValue()) {
        case ADD:
        case UPDATE:
        case UPSERT:
            setupSchemaForUpdate();
            break;
        case DELETE:
            setupSchemaForDelete();
            break;
        }
    }
}
