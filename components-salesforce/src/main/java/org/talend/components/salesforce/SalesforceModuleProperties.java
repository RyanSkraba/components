// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.List;

import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.StringProperty;

public class SalesforceModuleProperties extends ComponentPropertiesImpl implements SalesforceProvideConnectionProperties {

    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties("connection");

    //
    // Properties
    //
    public StringProperty moduleName = newString("moduleName"); //$NON-NLS-1$

    public ISchemaListener schemaListener;

    public SchemaProperties main = new SchemaProperties("main") {

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }
    };

    public SalesforceModuleProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = Form.create(this, Form.MAIN);
        moduleForm.addRow(widget(moduleName).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(moduleForm);

        Form moduleRefForm = Form.create(this, Form.REFERENCE);
        moduleRefForm.addRow(widget(moduleName).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE).setLongRunning(true));

        moduleRefForm.addRow(main.getForm(Form.REFERENCE));
        refreshLayout(moduleRefForm);
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    // consider beforeActivate and beforeRender (change after to afterActivate)l

    public ValidationResult beforeModuleName() throws Exception {
        try {
            List<NamedThing> moduleNames = SalesforceSourceOrSink.getSchemaNames(null, connection);
            moduleName.setPossibleNamedThingValues(moduleNames);
        } catch (ComponentException ex) {
            return ex.getValidationResult();
        }
        return ValidationResult.OK;
    }

    public ValidationResult afterModuleName() throws Exception {
        try {
            main.schema.setValue(SalesforceSourceOrSink.getSchema(null, connection, moduleName.getStringValue()));
        } catch (ComponentException ex) {
            return ex.getValidationResult();
        }
        return ValidationResult.OK;
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return connection;
    }
}
