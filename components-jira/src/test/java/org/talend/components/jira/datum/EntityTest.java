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
package org.talend.components.jira.datum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit-tests for {@link Entity} class
 */
public class EntityTest {

    /**
     * Check {@link Entity#getJson()} returns JSON representation without changes
     */
    @Test
    public void testGetJson() {
        String json = JsonDataProvider.getPaginationJson();
        Entity entity = new Entity(json);

        String actual = entity.getJson();
        assertEquals(json, actual);
    }
}
