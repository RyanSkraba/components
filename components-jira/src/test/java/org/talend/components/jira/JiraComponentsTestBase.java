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

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.jira.tjirainput.TJiraInputDefinition;
import org.talend.components.jira.tjiraoutput.TJiraOutputDefinition;

/**
 * Integration tests for Jira Input component, which check if nothing was missed
 * during component implementation
 */
public class JiraComponentsTestBase extends AbstractComponentTest {

    @Inject
    private ComponentService componentService;

    @Override
    public ComponentService getComponentService() {
        return componentService;
    }

    @Test
    public void componentHasBeenRegistered() {
        assertComponentIsRegistered(TJiraInputDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TJiraOutputDefinition.COMPONENT_NAME);
    }
}
