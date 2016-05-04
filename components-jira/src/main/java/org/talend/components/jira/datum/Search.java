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
 * Jira entity class, which represents /rest/api/2/search result.
 * It contains pure JSON string and provides methods to get certain values from it
 */
public class Search implements Entity{
    
    /**
     * JSON representation of this {@link Entity}
     */
    private String json;
    
    /**
     * Constructor sets Entity JSON representation 
     *
     * @param json Entity JSON representation
     */
    public Search(String json) {
        this.json = json;
    }

    public List<Entity> getEntities() {
        return null;
    }
    
    public int getTotal() {
        return 0;
    }
    
    @Override
    public String getJson() {
        return null;
    }
}
