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
package org.talend.components.processing.definition.filterrow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class FilterRowCriteriaPropertiesTest {

    /**
     * Checks {@link FilterRowCriteriaProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        FilterRowCriteriaProperties properties = new FilterRowCriteriaProperties("test");
        assertEquals("", properties.columnName.getValue());
        assertEquals("EMPTY", properties.function.getValue());
        assertEquals("==", properties.operator.getValue());
        assertEquals("", properties.value.getValue());
    }

    @Test
    public void testSetupLayout() {
        FilterRowCriteriaProperties properties = new FilterRowCriteriaProperties("test");

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(4));
        Widget columnNameWidget = main.getWidget("columnName");
        assertThat(columnNameWidget, notNullValue());
        Widget function = main.getWidget("function");
        assertThat(function, notNullValue());
        Widget operator = main.getWidget("operator");
        assertThat(operator, notNullValue());
        Widget value = main.getWidget("value");
        assertThat(value, notNullValue());
    }



}
