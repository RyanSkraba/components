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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * created by pbailly on 5 Nov 2015 Detailled comment
 *
 */
public class SchemaElementTest {

    /**
     * Simple test to be sure that no one will touch this list without thinking about consequences. If you remove/add a
     * type here, be sure that it does not break everything.
     */
    @Test
    public void testEnum() {
        List<String> ref = Arrays.asList("STRING", "BOOLEAN", "INT", "DATE", "DATETIME", "DECIMAL", "FLOAT", "DOUBLE",
                "BYTE_ARRAY", "ENUM", "DYNAMIC", "GROUP", "SCHEMA");
        List<SchemaElement.Type> types = Arrays.asList(SchemaElement.Type.values());
        assertEquals(ref.size(), types.size());
        assertEquals(ref.toString(), types.toString());
    }

}
