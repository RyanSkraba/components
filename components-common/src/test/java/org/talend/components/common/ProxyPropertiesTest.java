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
package org.talend.components.common;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.components.test.SpringApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class ProxyPropertiesTest {

    @Inject
    protected ComponentService componentService;

    public ProxyPropertiesTest() {
    }

    @Test
    public void testProxyProperties() throws Throwable {
        ProxyProperties props = (ProxyProperties) new ProxyProperties("proxy").init();
        Form mainForm = props.getForm(Form.MAIN);
        assertFalse(mainForm.getWidget("host").isVisible());
        assertFalse(mainForm.getWidget("userPassword").isVisible());

        props.setValue(props.useProxy, true);
        assertTrue(mainForm.getWidget("useProxy").isCallAfter());
        componentService.afterProperty("useProxy", props);
        assertTrue(mainForm.getWidget("host").isVisible());
        assertTrue(mainForm.getWidget("userPassword").isVisible());
    }
}
