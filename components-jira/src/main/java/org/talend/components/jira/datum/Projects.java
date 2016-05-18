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

import java.util.List;

/**
 * Jira entity class, which represents /rest/api/2/project result.
 * It contains pure JSON string and provides methods to get certain values from it
 */
public class Projects extends Entity {

    /**
     * Constructor sets Entity JSON representation 
     *
     * @param json Entity JSON representation
     */
    public Projects(String json) {
        super(json);
    }
    
    /**
     * Returns a list of Projects included in this Project result
     * 
     * @return a list of Projects included in this Project result
     */
    public List<Entity> getEntities() {
        List<Entity> entities = EntityParser.getEntities(getJson(), null);
        return entities;
    }

}
