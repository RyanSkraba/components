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

/**
 * Base class of Jira entities like Issue, Project, Search etc.
 * Each {@link Entity} represents separate Jira REST API resource
 */
public interface Entity {

    /**
     * Returns JSON representation of this {@link Entity}
     * 
     * @return JSON representation of this {@link Entity}
     */
    String getJson();
}
