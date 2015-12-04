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
package org.talend.components.api.schema;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.api.schema.SchemaElement.Type;
import org.talend.components.api.schema.internal.SchemaImpl;

/**
 * created by pbailly on 5 Nov 2015 Detailled comment
 *
 */
public class SchemaFactoryTest {

    @Test
    public void testNewSchema() {
        assertEquals(SchemaImpl.class, SchemaFactory.newSchema().getClass());
    }

    @Test
    public void testNewSchemaElement() {
        SchemaElement element = SchemaFactory.newSchemaElement(Type.DECIMAL, "schemaElement");
        assertEquals("schemaElement", element.getName());
        assertNull(element.getDefaultValue());
        assertNull(element.getDefaultValue());
        assertNull(element.getTitle());
        assertEquals(Type.DECIMAL, element.getType());
    }
}
