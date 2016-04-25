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
package org.talend.components.jira;

import org.talend.components.api.component.AbstractComponentDefinition;

/**
 * Jira definition common class. It contains some common definition implementation for all Jira components
 * 
 * created by ivan.honchar on Apr 22, 2016
 */
public abstract class JiraDefinition extends AbstractComponentDefinition {

    /**
     * Constructor sets component name
     * 
     * @param componentName component name
     */
    public JiraDefinition(String componentName) {
        super(componentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMavenArtifactId() {
        return "components-jira";
    }
    
    @Override
    public String[] getFamilies() {
        return new String[] { "Business/Jira" };
    }

}
