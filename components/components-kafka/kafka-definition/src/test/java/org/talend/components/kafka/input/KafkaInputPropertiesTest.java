package org.talend.components.kafka.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class KafkaInputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    KafkaInputProperties input;

    @Before
    public void reset() {
        input = new KafkaInputProperties("input");
        input.init();

    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(input, errorCollector);
    }

    @Test
    public void testVisible() throws Throwable {
        Form main = input.getForm(Form.MAIN);
        assertTrue(main.getWidget(input.groupID).isVisible());
        assertTrue(main.getWidget(input.autoOffsetReset).isVisible());
        assertTrue(main.getWidget(input.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(input.maxReadTime).isHidden());
        assertTrue(main.getWidget(input.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(input.maxNumRecords).isHidden());
        assertTrue(main.getWidget(input.configurations).isVisible());

        input.useMaxReadTime.setValue(true);
        PropertiesDynamicMethodHelper.afterProperty(input, input.useMaxReadTime.getName());
        assertTrue(main.getWidget(input.groupID).isVisible());
        assertTrue(main.getWidget(input.autoOffsetReset).isVisible());
        assertTrue(main.getWidget(input.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(input.maxReadTime).isVisible());
        assertTrue(main.getWidget(input.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(input.maxNumRecords).isHidden());
        assertTrue(main.getWidget(input.configurations).isVisible());

        input.useMaxReadTime.setValue(false);
        PropertiesDynamicMethodHelper.afterProperty(input, input.useMaxReadTime.getName());
        assertTrue(main.getWidget(input.groupID).isVisible());
        assertTrue(main.getWidget(input.autoOffsetReset).isVisible());
        assertTrue(main.getWidget(input.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(input.maxReadTime).isHidden());
        assertTrue(main.getWidget(input.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(input.maxNumRecords).isHidden());
        assertTrue(main.getWidget(input.configurations).isVisible());

        input.useMaxNumRecords.setValue(true);
        PropertiesDynamicMethodHelper.afterProperty(input, input.useMaxNumRecords.getName());
        assertTrue(main.getWidget(input.groupID).isVisible());
        assertTrue(main.getWidget(input.autoOffsetReset).isVisible());
        assertTrue(main.getWidget(input.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(input.maxReadTime).isHidden());
        assertTrue(main.getWidget(input.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(input.maxNumRecords).isVisible());
        assertTrue(main.getWidget(input.configurations).isVisible());

        input.useMaxNumRecords.setValue(false);
        PropertiesDynamicMethodHelper.afterProperty(input, input.useMaxNumRecords.getName());
        assertTrue(main.getWidget(input.groupID).isVisible());
        assertTrue(main.getWidget(input.autoOffsetReset).isVisible());
        assertTrue(main.getWidget(input.useMaxReadTime).isVisible());
        assertTrue(main.getWidget(input.maxReadTime).isHidden());
        assertTrue(main.getWidget(input.useMaxNumRecords).isVisible());
        assertTrue(main.getWidget(input.maxNumRecords).isHidden());
        assertTrue(main.getWidget(input.configurations).isVisible());
    }

    @Test
    public void testDefaultValue() {
        assertEquals(KafkaInputProperties.OffsetType.LATEST, input.autoOffsetReset.getValue());
        assertEquals("600000", input.maxReadTime.getValue().toString());
        assertEquals("5000", input.maxNumRecords.getValue().toString());
    }

    @Test
    public void testTrigger() {
        Form main = input.getForm(Form.MAIN);
        assertTrue(main.getWidget(input.useMaxReadTime).isCallAfter());
        assertTrue(main.getWidget(input.useMaxNumRecords).isCallAfter());
    }

}
