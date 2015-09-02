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
package org.talend.components.base.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.components.base.ComponentProperties;
import org.talend.components.base.ComponentService;
import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.test.testcomponent.TestComponentDefinition;
import org.talend.components.base.test.testcomponent.TestComponentProperties;

@RunWith(SpringJUnit4ClassRunner.class) @ContextConfiguration(classes = { ComponentService.class,
        TestComponentDefinition.class }, loader = AnnotationConfigContextLoader.class) public class LocalComponentTest
        extends TestCase {

    @Autowired protected ComponentService componentService;

    public LocalComponentTest() {
    }

    @Test public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        Form f = props.getForm(TestComponentProperties.TESTCOMPONENT);
        assertTrue(f.getLayout("userId").isVisible());
    }

}
