package org.talend.components.kafka.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;

public class KafkaDatasetPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    KafkaDatasetProperties dataset;

    @Before
    public void reset() {
        dataset = new KafkaDatasetProperties("dataset");
        dataset.init();

    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(dataset, errorCollector);
    }

    @Test
    public void testVisible() {
        Form main = dataset.getForm(Form.MAIN);
        assertTrue(main.getWidget(dataset.topic).isVisible());
        assertTrue(main.getWidget(dataset.main).isVisible());
    }

    @Test
    public void testDefaultValue() {
        Schema schema = dataset.main.schema.getValue();
        List<Schema.Field> fields = schema.getFields();
        assertEquals(2, fields.size());
        assertEquals(Schema.Type.BYTES, fields.get(schema.getField("key").pos()).schema().getType());
        assertEquals(Schema.Type.BYTES, fields.get(schema.getField("value").pos()).schema().getType());
    }

    @Test
    public void testTrigger() {
        Form main = dataset.getForm(Form.MAIN);
        assertTrue(main.getWidget(dataset.topic).isCallBeforePresent());
    }
}
