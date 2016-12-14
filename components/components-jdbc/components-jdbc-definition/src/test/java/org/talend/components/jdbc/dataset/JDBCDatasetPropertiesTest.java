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

package org.talend.components.jdbc.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.components.jdbc.dataset.JDBCDatasetProperties.SourceType.TABLE_NAME;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class JDBCDatasetPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    JDBCDatasetProperties dataset;

    @Before
    public void reset() {
        dataset = new JDBCDatasetProperties("dataset");
        dataset.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(dataset, errorCollector);
    }

    @Test
    public void testVisible() throws Throwable {
        Form main = dataset.getForm(Form.MAIN);
        assertTrue(main.getWidget(dataset.sourceType).isVisible());
        assertTrue(main.getWidget(dataset.tableName).isHidden());
        assertTrue(main.getWidget(dataset.sql).isVisible());

        dataset.sourceType.setValue(TABLE_NAME);
        PropertiesDynamicMethodHelper.afterProperty(dataset, dataset.sourceType.getName());
        assertTrue(main.getWidget(dataset.sourceType).isVisible());
        assertTrue(main.getWidget(dataset.tableName).isVisible());
        assertTrue(main.getWidget(dataset.sql).isHidden());
    }

    @Test
    public void testDefaultValue() {
        assertEquals(JDBCDatasetProperties.SourceType.QUERY, dataset.sourceType.getValue());
    }

    @Test
    public void testTrigger() {
        Form main = dataset.getForm(Form.MAIN);
        assertTrue(main.getWidget(dataset.sourceType).isCallAfter());
    }

    @Test
    public void testGetSql() {
        dataset.sql.setValue("abc");
        assertEquals("abc", dataset.getSql());
        dataset.sourceType.setValue(TABLE_NAME);
        dataset.tableName.setValue("abc");
        assertEquals("select * from abc", dataset.getSql());
    }

}
