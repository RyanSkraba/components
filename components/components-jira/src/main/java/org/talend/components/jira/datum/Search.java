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
package org.talend.components.jira.datum;

import java.util.List;

/**
 * Jira entity class, which represents /rest/api/2/search result.
 * It contains pure JSON string and provides methods to get certain values from it
 */
public class Search extends Entity {

    /**
     * Constructor sets Entity JSON representation
     *
     * @param json Entity JSON representation
     */
    public Search(String json) {
        super(json);
    }

    /**
     * Returns a list of Issues included in this Search result
     * 
     * @return a list of Issues included in this Search result
     */
    public List<Entity> getEntities() {
        List<Entity> entities = EntityParser.getEntities(getJson(), "issues");
        return entities;
    }

    /**
     * Returns total property value of this {@link Entity}
     * 
     * @return total property value
     */
    public int getTotal() {
        int total = EntityParser.getTotal(getJson());
        return total;
    }

}
