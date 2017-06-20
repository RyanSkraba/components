// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;
import org.talend.components.salesforce.runtime.BulkResult;

/**
 *
 */
public class BulkResultTest {

    @Test
    public void testCopyValues() throws IOException {

        BulkResult result = new BulkResult();
        result.setValue("fieldA", "fieldValueA");
        result.setValue("fieldB", "fieldValueB");
        result.setValue("fieldC", "fieldValueC");
        result.setValue("fieldD", "#N/A");

        BulkResult result2 = new BulkResult();
        result2.copyValues(result);

        assertEquals("fieldValueA", result2.getValue("fieldA"));
        assertEquals("fieldValueB", result2.getValue("fieldB"));
        assertEquals("fieldValueC", result2.getValue("fieldC"));
        assertNull(result2.getValue("fieldD"));

        BulkResult result3 = new BulkResult();
        result3.copyValues(null);
        assertNull(result3.getValue("fieldA"));
        assertNull(result3.getValue("fieldB"));
    }
}
