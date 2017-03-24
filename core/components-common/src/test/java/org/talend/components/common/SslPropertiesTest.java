// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class SslPropertiesTest {

    @Test
    public void testSslProperteis() throws Throwable {
        SslProperties props = new SslProperties("ssl");
        props.init();

        List<String> ALL = Arrays.asList(props.useSsl.getName(), props.trustStorePath.getName(),
                props.trustStorePassword.getName(), props.trustStoreType.getName(), props.needClientAuth.getName(),
                props.keyStoreType.getName(), props.keyStorePassword.getName(), props.keyStorePath.getName(),
                props.verifyHost.getName());

        Form mainForm = props.getForm(Form.MAIN);

        assertThat(mainForm.getWidgets(), hasSize(ALL.size()));
        for (String field : ALL) {
            Widget w = mainForm.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }

        // Check default value
        assertFalse(props.useSsl.getValue());
        assertFalse(props.needClientAuth.getValue());
        assertTrue(props.verifyHost.getValue());

        assertTrue(mainForm.getWidget("useSsl").isVisible());

        List<String> group1 = Arrays.asList("trustStoreType", "trustStorePath", "trustStorePassword", "needClientAuth",
                "verifyHost");
        List<String> group2 = Arrays.asList("keyStoreType", "keyStorePath", "keyStorePassword");
        for (String pName : group1) {
            assertTrue(pName, mainForm.getWidget(pName).isHidden());
        }
        for (String pName : group2) {
            assertTrue(pName, mainForm.getWidget(pName).isHidden());
        }

        props.useSsl.setValue(true);
        assertTrue(mainForm.getWidget("useSsl").isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(props, "useSsl");
        for (String pName : group1) {
            assertTrue(pName, mainForm.getWidget(pName).isVisible());
        }
        for (String pName : group2) {
            assertTrue(pName, mainForm.getWidget(pName).isHidden());
        }

        props.needClientAuth.setValue(true);
        assertTrue(mainForm.getWidget("needClientAuth").isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(props, "needClientAuth");
        for (String pName : group1) {
            assertTrue(pName, mainForm.getWidget(pName).isVisible());
        }
        for (String pName : group2) {
            assertTrue(pName, mainForm.getWidget(pName).isVisible());
        }

    }

    @Test
    public void testTrustOnly() throws Throwable {
        SslProperties props = new SslProperties("ssl", SslProperties.FormType.TRUST_ONLY);
        props.init();

        List<String> ALL = Arrays.asList(props.useSsl.getName(), props.trustStorePath.getName(),
                props.trustStorePassword.getName(), props.trustStoreType.getName());

        Form mainForm = props.getForm(Form.MAIN);

        assertThat(mainForm.getWidgets(), hasSize(ALL.size()));
        for (String field : ALL) {
            Widget w = mainForm.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }

        // Check default value
        assertFalse(props.useSsl.getValue());

        assertTrue(mainForm.getWidget("useSsl").isVisible());

        List<String> group1 = Arrays.asList("trustStoreType", "trustStorePath", "trustStorePassword");
        for (String pName : group1) {
            assertTrue(pName, mainForm.getWidget(pName).isHidden());
        }

        props.useSsl.setValue(true);
        assertTrue(mainForm.getWidget("useSsl").isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(props, "useSsl");
        for (String pName : group1) {
            assertTrue(pName, mainForm.getWidget(pName).isVisible());
        }

    }

}
