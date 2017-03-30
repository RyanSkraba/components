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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

public class NameMappingTableTest {

    NameMappingTable nmt;

    @Before
    public void init() {
        List<String> schemaCols = new ArrayList<>();
        List<String> entityCols = new ArrayList<>();
        nmt = new NameMappingTable("test");
        nmt.schemaColumnName.setValue(schemaCols);
        nmt.entityPropertyName.setValue(entityCols);

    }

    @Test
    public void testValidateNameMappingsEmptyTable() {
        // empty table is ok
        assertEquals(ValidationResult.OK, nmt.validateNameMappings());
    }

    @Test
    public void testValidateNameMappingsInvalide() {
        nmt.schemaColumnName.getValue().add("notentitymapping");
        nmt.entityPropertyName.getValue().add("");
        assertEquals(ValidationResult.Result.ERROR, nmt.validateNameMappings().getStatus());
    }

    @Test
    public void testValidateNameMappingsValide() {
        nmt.schemaColumnName.getValue().add("system");
        nmt.entityPropertyName.getValue().add("SytstemStatus");
        assertEquals(ValidationResult.OK, nmt.validateNameMappings());
    }

    @Ignore
    public void testGetNameMappings() {
        nmt.schemaColumnName.getValue().add("system");
        nmt.entityPropertyName.getValue().add("SytstemStatus");
        Map<String, String> result = nmt.getNameMappings();
        assertNotNull(result);
        assertEquals(1, result.values().size());
        assertTrue(result.containsKey("system"));
        assertEquals("SytstemStatus", result.get("system"));
    }

}
