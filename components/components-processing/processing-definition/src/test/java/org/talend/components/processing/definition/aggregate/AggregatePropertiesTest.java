package org.talend.components.processing.definition.aggregate;

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
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class AggregatePropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    AggregateProperties properties;

    @Before
    public void reset() {
        properties = new AggregateProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    @Test
    public void testVisible() {
        Form main = properties.getForm(Form.MAIN);
        assertTrue(main.getWidget(properties.groupBy).isVisible());
        assertTrue(main.getWidget(properties.operations).isVisible());
    }

    @Test
    public void testDefaultProperties() {
        assertEquals(1, properties.groupBy.getPropertiesList().size());
    }

    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();

        List<String> ALL = Arrays.asList(properties.groupBy.getName(), properties.operations.getName());

        Assert.assertThat(main, notNullValue());
        Assert.assertThat(mainWidgets, hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }
    }
}
