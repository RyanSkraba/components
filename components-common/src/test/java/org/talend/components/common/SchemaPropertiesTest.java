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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class SchemaPropertiesTest {

    @Test
    public void testWithInit() {
        // basic element
        SchemaProperties schemaProperties = new SchemaProperties("testSchema");
        assertEquals("testSchema", schemaProperties.getName());
        assertEquals(0, schemaProperties.getForms().size());

        // init
        schemaProperties.init();

        assertEquals("schema", schemaProperties.schema.getName());
        assertNotNull(schemaProperties.schema.getValue());
        assertThat(schemaProperties.schema.getValue(), instanceOf(Schema.class));

        // check the automatic getLayer
        assertEquals(2, schemaProperties.getForms().size());

        assertNotNull(schemaProperties.getForm(Form.MAIN));
        assertNotNull("Schema", schemaProperties.getForm(Form.MAIN).getName());
        assertNotNull(schemaProperties.getForm(Form.MAIN).getWidget(schemaProperties.schema.getName()));
        assertEquals(1, schemaProperties.getForm(Form.MAIN).getWidget(schemaProperties.schema.getName()).getRow());
        assertEquals(Widget.WidgetType.SCHEMA_EDITOR,
                schemaProperties.getForm(Form.MAIN).getWidget(schemaProperties.schema.getName()).getWidgetType());

        assertNotNull(schemaProperties.getForm(Form.REFERENCE));
        assertNotNull("Schema", schemaProperties.getForm(Form.REFERENCE).getName());
        assertNotNull(schemaProperties.getForm(Form.REFERENCE).getWidget(schemaProperties.schema.getName()));
        assertEquals(1, schemaProperties.getForm(Form.REFERENCE).getWidget(schemaProperties.schema.getName()).getRow());
        assertEquals(Widget.WidgetType.SCHEMA_REFERENCE,
                schemaProperties.getForm(Form.REFERENCE).getWidget(schemaProperties.schema.getName()).getWidgetType());

        // add element
        Schema schema = new Schema.Parser().parse(schemaProperties.schema.getStringValue());
        assertThat(schema, not(nullValue()));
    }

}
