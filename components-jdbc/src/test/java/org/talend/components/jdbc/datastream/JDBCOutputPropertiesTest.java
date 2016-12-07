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

package org.talend.components.jdbc.datastream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;

public class JDBCOutputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    JDBCOutputProperties output;

    @Before
    public void reset() {
        output = new JDBCOutputProperties("output");
        output.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(output, errorCollector);
    }

    @Test
    public void testVisible() throws Throwable {
        Form main = output.getForm(Form.MAIN);
        assertTrue(main.getWidget(output.dataAction).isVisible());
    }

    @Test
    public void testDefaultValue() {
        assertEquals(JDBCOutputProperties.DataAction.INSERT, output.dataAction.getValue());
    }

    @Ignore
    @Test
    public void testTrigger() {
        //nothing to check for now
    }
}
