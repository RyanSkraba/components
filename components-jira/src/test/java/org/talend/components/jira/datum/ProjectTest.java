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
 * Unit-tests for {@link Projects} class
 */
public class ProjectTest {

    /**
     * Check {@link Projects#getEntities()} returns a list with correct number of entities inside it
     */
    @Test
    public void testGetEntities() {
    	String projectJson = "[{\"expand\":\"description,lead,url,projectKeys\",\"self\":\"http://localhost:8080/rest/api/2/project/10100\",\"id\":\"10100\","
    			+ "\"key\":\"AN\",\"name\":\"AnotherProject\",\"projectTypeKey\":\"software\"},"
    			+ "{\"expand\":\"description,lead,url,projectKeys\",\"self\":\"http://localhost:8080/rest/api/2/project/10000\",\"id\":\"10000\","
    			+ "\"key\":\"TP\",\"name\":\"Test Project\",\"projectTypeKey\":\"software\"}]";
        Projects project = new Projects(projectJson);
        List<Entity> entities = project.getEntities();
        assertEquals(2, entities.size());
    }
}
