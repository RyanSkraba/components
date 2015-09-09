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
package org.talend.components.api.test;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.internal.SpringApp;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.testcomponent.TestComponentDefinition;
import org.talend.components.api.test.testcomponent.TestComponentProperties;
import org.talend.components.api.test.testcomponent.TestComponentWizardDefinition;

import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class LocalComponentTest extends TestCase {

    @Autowired
    protected ComponentService componentService;

    public LocalComponentTest() {
    }

    @Test
    public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        Form f = props.getForm(TestComponentProperties.TESTCOMPONENT);
        assertTrue(f.getLayout("userId").isVisible());
    }

    @Test
    public void testGetWizardIconOk() {
        InputStream iconStream = componentService.getWizardPngImage(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME);
        assertNotNull(iconStream);
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardIconWrongName() {
        InputStream iconStream = componentService.getWizardPngImage("not an existing wizard name");
        assertNull(iconStream);
    }
}
