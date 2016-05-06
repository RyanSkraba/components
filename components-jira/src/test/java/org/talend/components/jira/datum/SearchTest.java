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

import java.util.List;

import org.junit.Test;

/**
 * Unit-tests for {@link Search} class
 */
public class SearchTest {

    /**
     * Check {@link Search#getTotal()} returns correct value of total field from JSON representation
     */
    @Test
    public void getTotalTest() {
        Search search = new Search(JsonDataProvider.getPaginationJson());

        int actual = search.getTotal();
        assertEquals(37, actual);
    }

    /**
     * Check {@link Search#getTotal()} returns -1 value, when total property is not defined in JSON representation
     */
    @Test
    public void getTotalUndefinedTest() {
        Search search = new Search(JsonDataProvider.getNoPaginationJson());

        int actual = search.getTotal();
        assertEquals(-1, actual);
    }

    /**
     * Check {@link Search#getEntities()} returns a list with correct number of entities inside it
     */
    @Test
    public void getEntitiesTest() {
        Search search = new Search(JsonDataProvider.getPaginationJson());

        List<Entity> entities = search.getEntities();
        assertEquals(3, entities.size());
    }
}
