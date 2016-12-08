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
package org.talend.components.kafka.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;

public class KafkaDatasetPropertiesTest {

    public static final String USER_SCHEMA = "{" + "\"type\":\"record\"," + "\"name\":\"abc\"," + "\"fields\":["
            + "  { \"name\":\"str1\", \"type\":\"string\" }," + "  { \"name\":\"str2\", \"type\":\"string\" },"
            + "  { \"name\":\"int1\", \"type\":\"int\" }" + "]}";

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
    public void testVisible() throws Throwable {
        Form main = dataset.getForm(Form.MAIN);
        assertTrue(main.getWidget(dataset.topic).isVisible());
        assertTrue(main.getWidget(dataset.main).isVisible());
        assertTrue(main.getWidget(dataset.valueFormat).isVisible());
        assertTrue(main.getWidget(dataset.isHierarchy).isHidden());
        assertTrue(main.getWidget(dataset.avroSchema).isHidden());

        dataset.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.AVRO);
        PropertiesDynamicMethodHelper.afterProperty(dataset, dataset.valueFormat.getName());
        assertTrue(main.getWidget(dataset.topic).isVisible());
        assertTrue(main.getWidget(dataset.main).isVisible());
        assertTrue(main.getWidget(dataset.valueFormat).isVisible());
        assertTrue(main.getWidget(dataset.isHierarchy).isVisible());
        assertTrue(main.getWidget(dataset.avroSchema).isHidden());

        dataset.isHierarchy.setValue(true);
        PropertiesDynamicMethodHelper.afterProperty(dataset, dataset.isHierarchy.getName());
        assertTrue(main.getWidget(dataset.topic).isVisible());
        assertTrue(main.getWidget(dataset.main).isVisible());
        assertTrue(main.getWidget(dataset.valueFormat).isVisible());
        assertTrue(main.getWidget(dataset.isHierarchy).isVisible());
        assertTrue(main.getWidget(dataset.avroSchema).isVisible());

    }

    @Test
    public void testDefaultValue() {
        Schema schema = dataset.main.schema.getValue();
        List<Schema.Field> fields = schema.getFields();
        assertEquals(2, fields.size());
        assertEquals(Schema.Type.BYTES, fields.get(schema.getField("key").pos()).schema().getType());
        assertEquals(Schema.Type.BYTES, fields.get(schema.getField("value").pos()).schema().getType());
        assertEquals(KafkaDatasetProperties.ValueFormat.RAW, dataset.valueFormat.getValue());
        assertFalse(dataset.isHierarchy.getValue());
        assertNull(dataset.avroSchema.getValue());
    }

    @Test
    public void testTrigger() {
        Form main = dataset.getForm(Form.MAIN);
        assertTrue(main.getWidget(dataset.topic).isCallBeforePresent());
        assertTrue(main.getWidget(dataset.valueFormat).isCallAfter());
        assertTrue(main.getWidget(dataset.isHierarchy).isCallAfter());
    }

    @Test
    public void testGetValueAvroSchema() {
        assertNull(dataset.getValueAvroSchema());

        dataset.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.AVRO);
        Schema schema = dataset.getValueAvroSchema();
        assertEquals(1, schema.getFields().size());
        assertEquals("value", schema.getFields().get(0).name());

        dataset.isHierarchy.setValue(true);
        dataset.avroSchema.setValue(USER_SCHEMA);
        schema = dataset.getValueAvroSchema();
        assertEquals(new Schema.Parser().parse(USER_SCHEMA), schema);
    }
}
