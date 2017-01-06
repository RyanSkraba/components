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

package org.talend.components.jms.output;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class JmsOutputPropertiesTest {

    /**
     * Checks {@link JmsOutputProperties} sets correctly initial schema
     * property
     */
    @Test
    public void testDefaultProperties() {
        JmsOutputProperties properties = new JmsOutputProperties("test");
        assertNull(properties.delivery_mode.getValue());
        assertEquals("8", properties.pool_max_total.getValue());
        assertEquals("-1", properties.pool_max_wait.getValue());
        assertEquals("0", properties.pool_min_Idle.getValue());
        assertEquals(false, properties.pool_use_eviction.getValue());
        assertEquals("-1", properties.pool_time_between_eviction.getValue());
        assertEquals("1800000", properties.pool_eviction_min_idle_time.getValue());
        assertEquals("0", properties.pool_eviction_soft_min_idle_time.getValue());
    }

    /**
     * Checks {@link JmsOutputProperties} sets correctly initial layout
     * properties
     */
    @Test
    public void testSetupLayout() {
        JmsOutputProperties properties = new JmsOutputProperties("test");

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();

        assertThat(mainWidgets, hasSize(0));

        Collection<Widget> advancedWidgets = advanced.getWidgets();
        assertThat(advancedWidgets, hasSize(9));

        Widget delevery_mode = advanced.getWidget("delivery_mode");
        assertThat(delevery_mode, notNullValue());
        Widget pool_max_total = advanced.getWidget("pool_max_total");
        assertThat(pool_max_total, notNullValue());
        Widget pool_max_wait = advanced.getWidget("pool_max_wait");
        assertThat(pool_max_wait, notNullValue());
        Widget pool_min_Idle = advanced.getWidget("pool_min_Idle");
        assertThat(pool_min_Idle, notNullValue());
        Widget pool_max_Idle = advanced.getWidget("pool_max_Idle");
        assertThat(pool_max_Idle, notNullValue());
        Widget pool_use_eviction = advanced.getWidget("pool_use_eviction");
        assertThat(pool_use_eviction, notNullValue());
        Widget pool_time_between_eviction = advanced.getWidget("pool_time_between_eviction");
        assertThat(pool_time_between_eviction, notNullValue());
        Widget pool_eviction_min_idle_time = advanced.getWidget("pool_eviction_min_idle_time");
        assertThat(pool_eviction_min_idle_time, notNullValue());
        Widget pool_eviction_soft_min_idle_time = advanced.getWidget("pool_eviction_soft_min_idle_time");
        assertThat(pool_eviction_soft_min_idle_time, notNullValue());
    }

    /**
     * Checks {@link JmsOutputProperties} sets correctly layout after refresh
     * properties
     */
    @Test
    public void testRefreshLayout() {
        JmsOutputProperties properties = new JmsOutputProperties("test");
        properties.init();
        properties.refreshLayout(properties.getForm(Form.MAIN));

        properties.refreshLayout(properties.getForm(Form.ADVANCED));

        assertFalse(properties.getForm(Form.ADVANCED).getWidget("delivery_mode").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_max_total").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_max_wait").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_min_Idle").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_max_Idle").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_use_eviction").isHidden());
        assertTrue(properties.getForm(Form.ADVANCED).getWidget("pool_time_between_eviction").isHidden());
        assertTrue(properties.getForm(Form.ADVANCED).getWidget("pool_eviction_min_idle_time").isHidden());
        assertTrue(properties.getForm(Form.ADVANCED).getWidget("pool_eviction_soft_min_idle_time").isHidden());

        properties.pool_use_eviction.setValue(true);
        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("delivery_mode").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_max_total").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_max_wait").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_min_Idle").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_max_Idle").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_use_eviction").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_time_between_eviction").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_eviction_min_idle_time").isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget("pool_eviction_soft_min_idle_time").isHidden());
    }
}
