// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class EncodingTypePropertiesTest {

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
        PropertiesDynamicMethodHelper.afterProperty(defaultProperties, "encodingType");
        assertFalse(defaultMainForm.getWidget("encodingType").isHidden());
        assertFalse(defaultMainForm.getWidget("customEncoding").isHidden());
        assertEquals("UTF-16", defaultProperties.customEncoding.getValue());

        // Test new specify possible values
        EncodingTypeProperties specifyProperties = (EncodingTypeProperties) new EncodingTypeProperties(
                "defaultEncodingProperties") {

            @Override
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
        PropertiesDynamicMethodHelper.afterProperty(specifyProperties, "encodingType");
        assertFalse(specifyMainForm.getWidget("encodingType").isHidden());
        assertFalse(specifyMainForm.getWidget("customEncoding").isHidden());
        assertEquals("GBK", specifyProperties.customEncoding.getValue());
    }
}
