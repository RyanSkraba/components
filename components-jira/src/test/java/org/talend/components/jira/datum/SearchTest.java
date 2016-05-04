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

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.jira.testutils.Utils;

/**
 * Unit-tests for {@link Search} class
 */
public class SearchTest {
    
    private static String json;
    
    /**
     * Reads Search entity json from test file
     * 
     * @throws IOException in case of I/O exception
     */
    @BeforeClass
    public static void setUpClass() throws IOException {
        json = Utils.readFile("src/test/resources/org/talend/components/jira/datum/entities.json");
    }

    /**
     * Check {@link Search#getJson()} returns JSON representation without changes
     */
    @Test
    public void getJsonTest() {
        Search search = new Search(json);
        
        String actual = search.getJson();
        assertEquals(json, actual);
    }
    
    /**
     * Check {@link Search#getTotal()} returns correct value of total field from JSON representation
     */
    @Test
    public void getTotalTest() {
        Search search = new Search(json);
        
        int actual = search.getTotal();
        assertEquals(37, actual);
    }
    
    /**
     * Check {@link Search#getEntities()} returns a list with correct number of entities inside it
     */
    @Test
    public void getEntitiesTest() {
        Search search = new Search(json);
        
        List<Entity> entities = search.getEntities();
        assertEquals(3, entities.size());
    }
}
