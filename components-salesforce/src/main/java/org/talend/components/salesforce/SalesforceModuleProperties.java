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

import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class SalesforceModuleProperties extends ComponentProperties {

    private SalesforceConnectionProperties connection;

    //
    // Properties
    //
    public Property moduleName = newEnum("moduleName"); //$NON-NLS-1$

    public SchemaProperties schema = new SchemaProperties("schema");

    public SalesforceModuleProperties(String name) {
        super(name);
    }

    // FIXME - OK what about if we are using a connection from a separate component
    // that defines the connection, how do we get that separate component?
    public SalesforceModuleProperties setConnection(SalesforceConnectionProperties conn) {
        connection = conn;
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = new Form(this, Form.MAIN);
        moduleForm.addRow(widget(moduleName).setWidgetType(Widget.WidgetType.NAME_SELECTION_AREA));
        refreshLayout(moduleForm);

        Form moduleRefForm = new Form(this, Form.REFERENCE);
        moduleRefForm.addRow(widget(moduleName).setWidgetType(Widget.WidgetType.NAME_SELECTION_REFERENCE));

        moduleRefForm.addRow(schema.getForm(Form.REFERENCE));
        refreshLayout(moduleRefForm);
    }

    // consider beforeActivate and beforeRender (change after to afterActivate)l

    public ValidationResult beforeModuleName() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        ValidationResult vr = conn.connectWithResult(connection);
        if (vr.getStatus() == ValidationResult.Result.OK) {
            List<NamedThing> moduleNames = conn.getSchemaNames();
            moduleName.setPossibleValues(moduleNames);
        }
        return vr;
    }

    public ValidationResult afterModuleName() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        ValidationResult vr = conn.connectWithResult(connection);
        if (vr.getStatus() == ValidationResult.Result.OK) {
            schema.schema.setValue(conn.getSchema(moduleName.getStringValue()));
        }
        return vr;
    }

}
