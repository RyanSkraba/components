package org.talend.components.hadoopcluster.configuration.input;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;

public class HadoopClusterConfigurationBlackListTablePropertiesTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    HadoopClusterConfigurationBlackListTableProperties table;

    @Before
    public void reset() {
        table = new HadoopClusterConfigurationBlackListTableProperties("table");
        table.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(table, errorCollector);
    }

    @Test
    public void testVisible() {
        Form main = table.getForm(Form.MAIN);
        assertTrue(main.getWidget(table.keyCol).isVisible());
    }

    @Test
    public void testDefaultValue() {
        assertNull(table.keyCol.getValue());
    }

    @Test
    public void testTrigger() {
    }
}
