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

package ${package}.output;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ${componentNameClass}OutputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    ${componentNameClass}OutputProperties properties;

    @Before
    public void reset() {
        properties = new ${componentNameClass}OutputProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }


    /**
     * Checks {@link ${componentNameClass}OutputProperties} sets correctly initial schema
     * property
     */
    @Test
    public void testDefaultProperties() {
    }

    /**
     * Checks {@link ${componentNameClass}OutputProperties} sets correctly initial layout
     * properties
     */
    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();
    }

    /**
     * Checks {@link ${componentNameClass}DatastoreProperties} sets correctly layout after refresh
     * properties
     */
    @Test
    public void testRefreshLayout() {
        properties.refreshLayout(properties.getForm(Form.MAIN));
    }
}
