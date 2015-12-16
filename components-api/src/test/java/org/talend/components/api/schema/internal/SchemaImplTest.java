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
package org.talend.components.api.schema.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.api.schema.SchemaElement;

/**
 * created by pbailly on 16 Dec 2015 Detailled comment
 *
 */
public class SchemaImplTest {

    @Test
    public void test() {
        SchemaImpl schema = new SchemaImpl();
        assertNull(schema.getRoot());
        SchemaElement element = new DataSchemaElement();
        schema.setRoot(element);
        assertEquals(element, schema.getRoot());

    }

}
