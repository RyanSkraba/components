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

package org.talend.components.kinesis.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class KinesisInputPropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final List<String> ALL =
            Arrays.asList("position", "useMaxReadTime", "maxReadTime", "useMaxNumRecords", "maxNumRecords");

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    KinesisInputProperties properties;

    @Before
    public void reset() {
        properties = new KinesisInputProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    /**
     * Checks {@link KinesisInputProperties} sets correctly initial schema
     * property
     */
    @Test
    public void testDefaultProperties() {
        assertEquals(KinesisInputProperties.OffsetType.LATEST, properties.position.getValue());
        assertEquals(false, properties.useMaxReadTime.getValue());
        assertEquals(new Long(600000L), properties.maxReadTime.getValue());
        assertEquals(false, properties.useMaxNumRecords.getValue());
        assertEquals(new Integer(5000), properties.maxNumRecords.getValue());

    }

    /**
     * Checks {@link KinesisInputProperties} sets correctly initial layout
     * properties
     */
    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), Matchers.<Widget> hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }

        assertTrue(main.getWidget("position").isVisible());
        assertTrue(properties.position.isRequired());

        assertTrue(main.getWidget("useMaxReadTime").isVisible());
        assertFalse(properties.useMaxReadTime.isRequired());
        assertFalse(main.getWidget("maxReadTime").isVisible());
        assertFalse(properties.maxReadTime.isRequired());

        assertTrue(main.getWidget("useMaxNumRecords").isVisible());
        assertFalse(properties.useMaxNumRecords.isRequired());
        assertFalse(main.getWidget("maxNumRecords").isVisible());
        assertFalse(properties.maxNumRecords.isRequired());

    }

    /**
     * Checks {@link KinesisInputProperties} sets correctly layout after refresh
     * properties
     */
    @Test
    public void testRefreshLayout() {
        Form main = properties.getForm(Form.MAIN);

        // set true to use max read time
        properties.useMaxReadTime.setValue(true);
        properties.afterUseMaxReadTime();

        assertTrue(main.getWidget("useMaxReadTime").isVisible());
        assertFalse(properties.useMaxReadTime.isRequired());
        assertTrue(main.getWidget("maxReadTime").isVisible());
        assertTrue(properties.maxReadTime.isRequired());

        // set back false to use max read time
        properties.useMaxReadTime.setValue(false);
        properties.afterUseMaxReadTime();
        testSetupLayout();

        // set true to use max record num
        properties.useMaxNumRecords.setValue(true);
        properties.afterUseMaxNumRecords();

        assertTrue(main.getWidget("useMaxNumRecords").isVisible());
        assertFalse(properties.useMaxNumRecords.isRequired());
        assertTrue(main.getWidget("maxNumRecords").isVisible());
        assertTrue(properties.maxNumRecords.isRequired());

        // set back false to use max record num
        properties.useMaxNumRecords.setValue(false);
        properties.afterUseMaxNumRecords();
        testSetupLayout();
    }
}
