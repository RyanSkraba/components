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

import org.junit.Test;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class ProxyPropertiesTest {

    public ProxyPropertiesTest() {
    }

    @Test
    public void testProxyProperties() throws Throwable {
        ProxyProperties props = (ProxyProperties) new ProxyProperties("proxy").init();
        Form mainForm = props.getForm(Form.MAIN);
        assertTrue(mainForm.getWidget("host").isHidden());
        assertTrue(mainForm.getWidget("userPassword").isHidden());

        props.useProxy.setValue(true);
        assertTrue(mainForm.getWidget("useProxy").isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(props, "useProxy");
        assertFalse(mainForm.getWidget("host").isHidden());
        assertFalse(mainForm.getWidget("userPassword").isHidden());
    }
}
