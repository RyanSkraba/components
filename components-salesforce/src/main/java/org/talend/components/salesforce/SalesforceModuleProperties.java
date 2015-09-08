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

import java.util.List;

import org.talend.components.api.ComponentProperties;
import org.talend.components.api.ComponentSchemaElement;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Layout.WidgetType;

import com.fasterxml.jackson.annotation.JsonRootName;

import static org.talend.components.api.properties.presentation.Layout.*;

@JsonRootName("salesforceModuleProperties")
public class SalesforceModuleProperties extends ComponentProperties {

    private SalesforceConnectionProperties  connection;

    //
    // Properties
    //
    public Property<String>                 moduleName = new Property<String>("moduleName", "Module Name");

    public Property<ComponentSchemaElement> schema     = new Property<ComponentSchemaElement>("schema", "Schema");

    public static final String              MODULE     = "Module";

    public static final String              ADVANCED   = "Advanced";

    // FIXME - OK what about if we are using a connection from a separate component
    // that defines the connection, how do we get that separate component?
    public SalesforceModuleProperties(SalesforceConnectionProperties connectionProperties) {
        super();
        connection = connectionProperties;
        setupLayout();
    }

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form moduleForm = Form.create(this, MODULE, "Salesforce Module");
        moduleForm.addChild(moduleName, layout().setRow(1).setWidgetType(WidgetType.LISTBOX));
        moduleForm.addChild(schema, layout().setRow(2).setWidgetType(WidgetType.SCHEMA_ONE_LINE));
    }

    public void beforeModuleName() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        conn.connect(connection);
        List<String> moduleNames = conn.getModuleNames();
        // FIXME - these are labels, need to have a corresponding actual values.
        moduleName.setPossibleValues(moduleNames);
    }

    public void afterModuleName() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        conn.connect(connection);
        schema.setValue(conn.getSchema(moduleName.getValue()));
    }

}
