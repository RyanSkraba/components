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
package org.talend.components.kafka.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.common.SslProperties.StoreType;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

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
        assertTrue(main.getWidget(datastore.useSsl).isVisible());

        assertTrue(main.getWidget(datastore.trustStoreType).isHidden());
        assertTrue(main.getWidget(datastore.trustStorePath).isHidden());
        assertTrue(main.getWidget(datastore.trustStorePassword).isHidden());
        assertTrue(main.getWidget(datastore.needClientAuth).isHidden());
        assertTrue(main.getWidget(datastore.keyStoreType).isHidden());
        assertTrue(main.getWidget(datastore.keyStorePath).isHidden());
        assertTrue(main.getWidget(datastore.keyStorePassword).isHidden());
        assertTrue(main.getWidget(datastore.verifyHost).isHidden());
        
        datastore.useSsl.setValue(true);
        datastore.refreshLayout(main);
        assertTrue(main.getWidget(datastore.version).isVisible());
        assertTrue(main.getWidget(datastore.brokers).isVisible());
        assertTrue(main.getWidget(datastore.useSsl).isVisible());
        assertTrue(main.getWidget(datastore.trustStoreType).isVisible());
        assertTrue(main.getWidget(datastore.trustStorePath).isVisible());
        assertTrue(main.getWidget(datastore.trustStorePassword).isVisible());
        assertTrue(main.getWidget(datastore.needClientAuth).isVisible());
        assertTrue(main.getWidget(datastore.keyStoreType).isHidden());
        assertTrue(main.getWidget(datastore.keyStorePath).isHidden());
        assertTrue(main.getWidget(datastore.keyStorePassword).isHidden());
        assertTrue(main.getWidget(datastore.verifyHost).isVisible());

        datastore.needClientAuth.setValue(true);
        datastore.refreshLayout(main);
        assertTrue(main.getWidget(datastore.version).isVisible());
        assertTrue(main.getWidget(datastore.brokers).isVisible());
        assertTrue(main.getWidget(datastore.useSsl).isVisible());
        assertTrue(main.getWidget(datastore.trustStoreType).isVisible());
        assertTrue(main.getWidget(datastore.trustStorePath).isVisible());
        assertTrue(main.getWidget(datastore.trustStorePassword).isVisible());
        assertTrue(main.getWidget(datastore.needClientAuth).isVisible());
        assertTrue(main.getWidget(datastore.keyStoreType).isVisible());
        assertTrue(main.getWidget(datastore.keyStorePath).isVisible());
        assertTrue(main.getWidget(datastore.keyStorePassword).isVisible());
        assertTrue(main.getWidget(datastore.verifyHost).isVisible());
        

        datastore.useSsl.setValue(false);
        datastore.refreshLayout(main);
        assertTrue(main.getWidget(datastore.version).isVisible());
        assertTrue(main.getWidget(datastore.brokers).isVisible());
        assertTrue(main.getWidget(datastore.useSsl).isVisible());
        assertTrue(main.getWidget(datastore.trustStoreType).isHidden());
        assertTrue(main.getWidget(datastore.trustStorePath).isHidden());
        assertTrue(main.getWidget(datastore.trustStorePassword).isHidden());
        assertTrue(main.getWidget(datastore.needClientAuth).isHidden());
        assertTrue(main.getWidget(datastore.keyStoreType).isHidden());
        assertTrue(main.getWidget(datastore.keyStorePath).isHidden());
        assertTrue(main.getWidget(datastore.keyStorePassword).isHidden());
        assertTrue(main.getWidget(datastore.verifyHost).isHidden());
        
    }

    @Test
    public void testDefaultValue() {
        assertEquals(KafkaDatastoreProperties.KafkaVersion.V_0_10_1_0, datastore.version.getValue());
        assertNull(datastore.brokers.getValue());
    }

    // @Test
    // public void testTrigger() {
    // Form main = datastore.getForm(Form.MAIN);
    // assertTrue(main.getWidget(datastore.testConnection).isCallValidate());
    // }

}
