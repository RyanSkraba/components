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
        Projects project = new Projects(JsonDataProvider.getProjectJson());
        List<Entity> entities = project.getEntities();
        assertEquals(2, entities.size());
    }
}
