package org.talend.components.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class HttpsPropertiesTest {

    @Test
    public void testHttpsProperteis() throws Throwable {
        HttpsProperties props = new HttpsProperties("https");
        props.init();
        Form mainForm = props.getForm(Form.MAIN);

        // Check default value
        assertFalse(props.useHttps.getValue());
        assertFalse(props.needClientAuth.getValue());
        assertTrue(props.verifyHost.getValue());

        assertTrue(mainForm.getWidget("useHttps").isVisible());

        List<String> group1 = Arrays.asList("trustStoreType", "trustStorePath", "trustStorePassword", "needClientAuth",
                "verifyHost");
        List<String> group2 = Arrays.asList("keyStoreType", "keyStorePath", "keyStorePassword");
        for (String pName : group1) {
            assertTrue(pName, mainForm.getWidget(pName).isHidden());
        }
        for (String pName : group2) {
            assertTrue(pName, mainForm.getWidget(pName).isHidden());
        }

        props.useHttps.setValue(true);
        assertTrue(mainForm.getWidget("useHttps").isCallAfter());
        PropertiesDynamicMethodHelper.afterProperty(props, "useHttps");
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