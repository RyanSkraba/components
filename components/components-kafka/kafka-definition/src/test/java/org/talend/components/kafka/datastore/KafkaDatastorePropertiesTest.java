package org.talend.components.kafka.datastore;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;

public class KafkaDatastorePropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    KafkaDatastoreProperties datastore;

    @Before
    public void reset() {
        datastore = new KafkaDatastoreProperties("datastore");
        datastore.init();

    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(datastore, errorCollector);
    }

    @Test
    public void testVisible() {
        Form main = datastore.getForm(Form.MAIN);
        assertTrue(main.getWidget(datastore.version).isVisible());
        assertTrue(main.getWidget(datastore.brokers).isVisible());
        assertTrue(main.getWidget(datastore.ssl).isVisible());
    }

    @Test
    public void testDefaultValue() {
        assertEquals(KafkaDatastoreProperties.KafkaVersion.V_0_9_0_1, datastore.version.getValue());
        assertNull(datastore.brokers.getValue());
    }

    @Test
    public void testTrigger() {
        Form main = datastore.getForm(Form.MAIN);
        assertTrue(main.getWidget(datastore.testConnection).isCallValidate());
    }

}