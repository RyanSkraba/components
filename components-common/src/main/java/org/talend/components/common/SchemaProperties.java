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
package org.talend.components.common;

import static org.talend.daikon.properties.presentation.Widget.*;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class SchemaProperties extends PropertiesImpl {

    /** An empty schema is used for an uninitialized SchemaProperties. */
    public static final Schema EMPTY_SCHEMA = SchemaBuilder.builder().record("EmptyRecord").fields().endRecord(); //$NON-NLS-1$

    /**
     * The name of this SchemaProperties will be used to identify the connection name to the next component.
     * 
     * @param name
     */
    public SchemaProperties(String name) {
        super(name);
    }

    //
    // Properties
    //
    public Property<Schema> schema = PropertyFactory.newSchema("schema"); //$NON-NLS-1$

    @Override
    public void setupProperties() {
        super.setupProperties();
        schema.setValue(EMPTY_SCHEMA);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form schemaForm = Form.create(this, Form.MAIN);
        schemaForm.addRow(widget(schema).setWidgetType(Widget.SCHEMA_EDITOR_WIDGET_TYPE));

        Form schemaRefForm = Form.create(this, Form.REFERENCE);
        schemaRefForm.addRow(widget(schema).setWidgetType(Widget.SCHEMA_REFERENCE_WIDGET_TYPE));
    }

}
