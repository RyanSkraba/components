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

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.api.properties.PropertyFactory;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.internal.SchemaImpl;

/**
 * created by pbailly on 10 Dec 2015 Detailled comment
 *
 */
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
        assertEquals(SchemaImpl.class, schemaProperties.schema.getValue().getClass());

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
        Schema schema = (Schema) schemaProperties.getValue(schemaProperties.schema);
        assertNull(schema.getRoot());
        SchemaElement element = PropertyFactory.newBoolean("testBoolean", false);
        schemaProperties.addChild(element);
        assertNotNull(schema.getRoot());
        SchemaElement root = schema.getRoot();
        assertEquals(1, root.getChildMap().size());
        assertEquals(root.getChild("testBoolean"), element);
    }

}
