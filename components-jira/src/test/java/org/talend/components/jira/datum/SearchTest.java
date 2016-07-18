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
    public void testGetTotal() {
    	String paginationJson = "{\"expand\":\"schema,names\",\"startAt\":0,\"maxResults\":50,\"total\":37,\"issues\":["
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10413\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10413\",\"key\":\"TP-41\",\"fields\":{\"description\":\"task upd\",\"summary\":\"Updated 37\",}},"
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10412\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10412\",\"key\":\"TP-40\",\"fields\":{\"description\":\"Task updated\",\"summary\":\"TP 38 updated\",}},"
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10411\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10411\",\"key\":\"TP-39\",\"fields\":{\"description\":null,\"summary\":\"115\",}}]}";
        Search search = new Search(paginationJson);

        int actual = search.getTotal();
        assertEquals(37, actual);
    }

    /**
     * Check {@link Search#getTotal()} returns -1 value, when total property is not defined in JSON representation
     */
    @Test
    public void testGetTotalUndefined() {
    	String noPaginationJson = "{\"issues\":[{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10413\","
    			+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10413\",\"key\":\"TP-41\",\"fields\":{\"description\":\"task upd\",\"summary\":\"Updated 37\"}},"
    			+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10412\","
    			+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10412\",\"key\":\"TP-40\",\"fields\":{\"description\":\"Task updated\",\"summary\":\"TP 38 updated\"}},"
    			+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10411\","
    			+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10411\",\"key\":\"TP-39\",\"fields\":{\"description\":null,\"summary\":\"115\"}}]}";
        Search search = new Search(noPaginationJson);

        int actual = search.getTotal();
        assertEquals(-1, actual);
    }

    /**
     * Check {@link Search#getEntities()} returns a list with correct number of entities inside it
     */
    @Test
    public void testGetEntities() {
    	String paginationJson = "{\"expand\":\"schema,names\",\"startAt\":0,\"maxResults\":50,\"total\":37,\"issues\":["
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10413\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10413\",\"key\":\"TP-41\",\"fields\":{\"description\":\"task upd\",\"summary\":\"Updated 37\",}},"
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10412\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10412\",\"key\":\"TP-40\",\"fields\":{\"description\":\"Task updated\",\"summary\":\"TP 38 updated\",}},"
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10411\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10411\",\"key\":\"TP-39\",\"fields\":{\"description\":null,\"summary\":\"115\",}}]}";
        Search search = new Search(paginationJson);

        List<Entity> entities = search.getEntities();
        assertEquals(3, entities.size());
    }

    /**
     * Check {@link Search#getEntities()} handles json without errors, when there is no entities inside it
     * See https://jira.talendforge.org/browse/TDI-36301 for bug details
     */
    @Test
    public void testGetEntitiesNoIssues() {
    	String noIssuesJson = "{\"startAt\":0,\"maxResults\":2,\"total\":1,\"issues\":[]}";
        Search search = new Search(noIssuesJson);

        List<Entity> entities = search.getEntities();
        assertEquals(0, entities.size());
    }

    /**
     * Check {@link Search#getEntities()} correctly handle JSON with braces in strings
     * See https://jira.talendforge.org/browse/TDI-36415 for bug details
     */
    @Test
    public void testGetEntitiesBracesInString() {
    	String hasBraceJson = "{\"issues\":[{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10413\",\"self\":\"http://localhost:8080/rest/api/2/issue/10413\",\"key\":\"TP-41\",\"fields\":{\"description\":\"task upd\",\"summary\":\"Has brace \\\"}} in string \\\\\"{\"}}]}";
        Search search = new Search(hasBraceJson);

        List<Entity> entities = search.getEntities();
        assertEquals(1, entities.size());
    }
}
