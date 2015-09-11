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

import static org.talend.components.api.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.ComponentSchemaElement;
import org.talend.components.api.schema.ComponentSchemaFactory;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("schema")
public class SchemaProperties extends ComponentProperties {

    //
    // Properties
    //
    public Property<ComponentSchemaElement> schema    = new Property<ComponentSchemaElement>("schema", "Schema");

    public static final String              MAIN      = "Main";

    public static final String              REFERENCE = "Reference";

    public SchemaProperties() {
        super();
        setupLayout();
    }

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form schemaForm = Form.create(this, MAIN, "Schema");
        schemaForm.addChild(widget(schema).setRow(2).setWidgetType(Widget.WidgetType.SCHEMA_EDITOR));
        refreshLayout(schemaForm);

        Form schemaRefForm = Form.create(this, REFERENCE, "Schema");
        schemaRefForm.addChild(widget(schema).setRow(2).setWidgetType(Widget.WidgetType.SCHEMA_REFERENCE));
        refreshLayout(schemaRefForm);
    }

    public ComponentSchemaElement addRow(ComponentSchemaElement row) {
        ComponentSchemaElement root = schema.getValue();
        if (root == null) {
            root = ComponentSchemaFactory.getComponentSchemaElement("Root");
            schema.setValue(root);
        }
        root.addChild(row);
        return row;
    }

}
