// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.wizard;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.ComponentTestUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.testcomponent.TestComponentWizard;
import org.talend.components.api.service.testcomponent.TestComponentWizardDefinition;

public class WizardTest {

    @BeforeClass
    public static void init() {
        ComponentTestUtils.setupGlobalContext();
    }

    @AfterClass
    public static void unset() {
        ComponentTestUtils.unsetGlobalContext();
    }

    @Test
    public void testi18NForWizardDefintion() {
        ComponentWizardDefinition cwd = new TestComponentWizardDefinition();
        assertNotNull(cwd);
        assertTrue(cwd.isTopLevel());
        assertEquals("Test Wizard", cwd.getDisplayName());
        assertEquals("Ze Test Wizard Title", cwd.getTitle());
        assertEquals("Ze Test Wizard menu", cwd.getMenuItemName());
    }

    @Test
    public void testWizardProps() {
        ComponentWizardDefinition cwd = new TestComponentWizardDefinition();
        TestComponentWizard wiz = (TestComponentWizard) cwd.createWizard("testLoc");
        assertEquals("testLoc", wiz.getRepositoryLocation());
        assertTrue(cwd == wiz.getDefinition());
        assertEquals(1, wiz.getForms().size());
        assertTrue(wiz.props == wiz.getForms().get(0).getProperties());
    }

}
