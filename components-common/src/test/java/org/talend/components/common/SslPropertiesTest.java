package org.talend.components.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class SslPropertiesTest {

    @Test
    public void testSslProperteis() throws Throwable {
        SslProperties props = new SslProperties("ssl");
        props.init();
        Form mainForm = props.getForm(Form.MAIN);

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

}