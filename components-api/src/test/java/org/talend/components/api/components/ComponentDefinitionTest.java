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
package org.talend.components.api.components;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.testcomponent.TestComponentProperties;

/**
 * created by sgandon on 11 janv. 2016
 */
public class ComponentDefinitionTest {

    @Test
    public void test() {
        TestComponentDefinition cd = new TestComponentDefinition();

        TestComponentProperties prop = (TestComponentProperties) cd.createRuntimeProperties();
        assertNotNull(prop.initLater);
        assertNull(prop.mainForm);
    }

    @Test
    public void testi18NForComponentDefintion() {
        TestComponentDefinition tcd = new TestComponentDefinition();
        assertEquals("Test Component", tcd.getDisplayName());
        assertEquals("Ze Test Component Title", tcd.getTitle());
    }

}
