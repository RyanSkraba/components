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

import com.fasterxml.jackson.annotation.JsonRootName;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.ComponentSchemaElement;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Layout;
import static org.talend.components.api.properties.presentation.Layout.*;

import java.util.List;

@JsonRootName("schema") public class SchemaProperties extends ComponentProperties {

    //
    // Properties
    //
    public Property<ComponentSchemaElement> schema = new Property<ComponentSchemaElement>("schema", "Schema");

    public static final String MAIN = "Main";

    public static final String REFERENCE = "Reference";

    public SchemaProperties() {
        super();
        setupLayout();
    }

    @Override protected void setupLayout() {
        super.setupLayout();

        Form schema = Form.create(this, MAIN, "Schema");
        schema.addChild(this.schema, layout().setRow(2).setWidgetType(Layout.WidgetType.SCHEMA_EDITOR));
        refreshLayout(schema);

        Form schemaRef = Form.create(this, REFERENCE, "Schema");
        schemaRef.addChild(this.schema, layout().setRow(2).setWidgetType(Layout.WidgetType.SCHEMA_REFERENCE));
        refreshLayout(schemaRef);
    }

}
