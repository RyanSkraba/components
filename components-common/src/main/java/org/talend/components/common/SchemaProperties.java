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
package org.talend.components.common;

import static org.talend.components.api.properties.presentation.Widget.*;
import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaFactory;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("schema")
public class SchemaProperties extends ComponentProperties {

    public SchemaProperties() {
        super();
    }

    public SchemaProperties(String name) {
        super();
        setName(name);
    }

    //
    // Properties
    //
    // FIXME - change to Schema
    public SchemaElement schema = newProperty(SchemaElement.Type.SCHEMA, "schema"); //$NON-NLS-1$

    @Override
    public SchemaProperties init() {
        super.init();
        setValue(schema, SchemaFactory.newSchema());
        return this;
    }

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form schemaForm = Form.create(this, Form.MAIN, "Schema"); //$NON-NLS-1$
        schemaForm.addRow(widget(schema).setWidgetType(Widget.WidgetType.SCHEMA_EDITOR));

        Form schemaRefForm = Form.create(this, Form.REFERENCE, "Schema"); //$NON-NLS-1$
        schemaRefForm.addRow(widget(schema).setWidgetType(Widget.WidgetType.SCHEMA_REFERENCE));
    }

    @Override
    public SchemaElement addChild(SchemaElement row) {
        Schema s = (Schema) getValue(schema);
        if (s == null) {
            s = SchemaFactory.newSchema();
        }
        SchemaElement root = s.getRoot();
        if (root == null) {
            root = SchemaFactory.newProperty("Root"); //$NON-NLS-1$
            s.setRoot(root);
        }
        root.addChild(row);
        return row;
    }

}
