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

package org.talend.components.pubsub.input;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class PubSubInputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    PubSubInputProperties properties;

    @Before
    public void reset() {
        properties = new PubSubInputProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    @Test
    public void testVisible() throws Throwable {
        Form main = properties.getForm(Form.MAIN);
        assertTrue(main.getWidget(properties.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(properties.maxReadTime).isHidden());
        assertTrue(main.getWidget(properties.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(properties.maxNumRecords).isHidden());
        assertTrue(main.getWidget(properties.idLabel).isVisible());
        assertTrue(main.getWidget(properties.timestampLabel).isVisible());

        properties.useMaxReadTime.setValue(true);
        PropertiesDynamicMethodHelper.afterProperty(properties, properties.useMaxReadTime.getName());
        assertTrue(main.getWidget(properties.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(properties.maxReadTime).isVisible());
        assertTrue(main.getWidget(properties.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(properties.maxNumRecords).isHidden());
        assertTrue(main.getWidget(properties.idLabel).isVisible());
        assertTrue(main.getWidget(properties.timestampLabel).isVisible());

        properties.useMaxNumRecords.setValue(true);
        PropertiesDynamicMethodHelper.afterProperty(properties, properties.useMaxNumRecords.getName());
        assertTrue(main.getWidget(properties.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(properties.maxReadTime).isVisible());
        assertTrue(main.getWidget(properties.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(properties.maxNumRecords).isVisible());
        assertTrue(main.getWidget(properties.idLabel).isVisible());
        assertTrue(main.getWidget(properties.timestampLabel).isVisible());

    }

    /**
     * Checks {@link PubSubInputProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        assertEquals(600000, properties.maxReadTime.getValue().intValue());
        assertEquals(5000, properties.maxNumRecords.getValue().intValue());
    }

    /**
     * Checks {@link PubSubInputProperties} sets correctly initial layout properties
     */
    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();

        List<String> ALL = Arrays.asList(properties.useMaxReadTime.getName(), properties.maxReadTime.getName(),
                properties.useMaxNumRecords.getName(), properties.maxNumRecords.getName(), properties.idLabel.getName(),
                properties.timestampLabel.getName(), properties.isStreaming.getName());

        Assert.assertThat(main, notNullValue());
        Assert.assertThat(mainWidgets, hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }
    }
}
