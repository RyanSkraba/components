package org.talend.components.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;
import org.talend.daikon.properties.presentation.Form;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class EncodingTypePropertiesTest {

    @Inject
    protected ComponentService componentService;

    public EncodingTypePropertiesTest() {
    }

    @Test
    public void testEncodingTypeProperties() throws Throwable {
        // Test default possible values
        EncodingTypeProperties defaultProperties = (EncodingTypeProperties) new EncodingTypeProperties(
                "defaultEncodingProperties").init();
        Form defaultMainForm = defaultProperties.getForm(Form.MAIN);
        assertEquals(EncodingTypeProperties.ENCODING_TYPE_ISO_8859_15, defaultProperties.encodingType.getValue());
        assertFalse(defaultMainForm.getWidget("encodingType").isHidden());
        assertTrue(defaultMainForm.getWidget("customEncoding").isHidden());

        defaultProperties.encodingType.setValue(EncodingTypeProperties.ENCODING_TYPE_CUSTOM);
        defaultProperties.customEncoding.setValue("UTF-16");
        assertTrue(defaultMainForm.getWidget("encodingType").isCallAfter());
        componentService.afterProperty("encodingType", defaultProperties);
        assertFalse(defaultMainForm.getWidget("encodingType").isHidden());
        assertFalse(defaultMainForm.getWidget("customEncoding").isHidden());
        assertEquals("UTF-16", defaultProperties.customEncoding.getValue());

        // Test new specify possible values
        EncodingTypeProperties specifyProperties = (EncodingTypeProperties) new EncodingTypeProperties(
                "defaultEncodingProperties") {

            public List<String> getDefaultEncodings() {
                return Arrays.asList("UTF-16", "GBK", "ISO-8859-1", EncodingTypeProperties.ENCODING_TYPE_CUSTOM);
            };
        }.init();
        Form specifyMainForm = specifyProperties.getForm(Form.MAIN);
        assertEquals("UTF-16", specifyProperties.encodingType.getValue());
        assertFalse(specifyMainForm.getWidget("encodingType").isHidden());
        assertTrue(specifyMainForm.getWidget("customEncoding").isHidden());

        specifyProperties.encodingType.setValue(EncodingTypeProperties.ENCODING_TYPE_CUSTOM);
        specifyProperties.customEncoding.setValue("GBK");
        assertTrue(specifyMainForm.getWidget("encodingType").isCallAfter());
        componentService.afterProperty("encodingType", specifyProperties);
        assertFalse(specifyMainForm.getWidget("encodingType").isHidden());
        assertFalse(specifyMainForm.getWidget("customEncoding").isHidden());
        assertEquals("GBK", specifyProperties.customEncoding.getValue());
    }
}
