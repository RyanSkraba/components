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
        assertEquals(KafkaDatastoreProperties.KafkaVersion.V_0_10_0_1, datastore.version.getValue());
        assertNull(datastore.brokers.getValue());
    }

}
