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
package org.talend.components.azurestorage.table.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

public class NameMappingTableTest {

    List<String> schemaCols = new ArrayList<>();

    List<String> entityCols = new ArrayList<>();

    Map<String, String> result = new HashMap<>();

    NameMappingTable nmt = new NameMappingTable("test");

    public void clearList() {
        schemaCols.clear();
        entityCols.clear();
    }

    public void setTableValues() {
        nmt.schemaColumnName.setValue(schemaCols);
        nmt.entityPropertyName.setValue(entityCols);
    }

    @Test
    public void testValidation() {
        clearList();
        setTableValues();
        // empty table is ok
        assertEquals(ValidationResult.OK, nmt.validateNameMappings());
        schemaCols.add("system");
        entityCols.add("SytstemStatus");
        setTableValues();
        assertEquals(ValidationResult.OK, nmt.validateNameMappings());
        schemaCols.add("notentitymapping");
        entityCols.add("");
        setTableValues();
        assertEquals(ValidationResult.Result.ERROR, nmt.validateNameMappings().getStatus());
    }

    @Test
    public void testNameMappings() {
        clearList();
        setTableValues();
        assertNull(nmt.getNameMappings());
        schemaCols.add("system");
        entityCols.add("SytstemStatus");
        setTableValues();
        result = nmt.getNameMappings();
        assertNotNull(result);
        assertEquals(1, result.values().size());
        assertTrue(result.containsKey("system"));
        assertEquals("SytstemStatus", result.get("system"));
    }

}
