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

package org.talend.components.jms;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class JmsDatastorePropertiesTest {

    /**
     * Checks {@link JmsDatasetProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        JmsDatastoreProperties properties = new JmsDatastoreProperties("test");
        properties.init();
        assertEquals(JmsDatastoreProperties.JmsVersion.V_1_1, properties.version.getValue());
        // assertNull(properties.contextProvider.getValue());
        assertEquals("com.tibco.tibjms.naming.TibjmsInitialContextFactory", properties.contextProvider.getValue());
        assertEquals("tibjmsnaming://localhost:7222", properties.serverUrl.getValue());
        assertEquals("GenericConnectionFactory", properties.connectionFactoryName.getValue());
        assertEquals(false, properties.needUserIdentity.getValue());
        assertEquals("", properties.userPassword.userId.getValue());
        assertEquals("", properties.userPassword.password.getValue());
        assertEquals(false, properties.useHttps.getValue());
        assertNull(properties.httpsSettings.getValue());
        assertEquals("", properties.property.getValue());
        assertEquals("", properties.value.getValue());
    }

    /**
     * Checks {@link JmsDatastoreProperties} sets correctly initial layout properties
     */
    @Test
    public void testSetupLayout() {
        JmsDatastoreProperties properties = new JmsDatastoreProperties("test");
        properties.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(9));
        Widget msgType = main.getWidget("version");
        assertThat(msgType, notNullValue());
        Widget contextProvider = main.getWidget("contextProvider");
        assertThat(contextProvider, notNullValue());
        Widget serverUrl = main.getWidget("serverUrl");
        assertThat(serverUrl, notNullValue());
        Widget connectionFactoryName = main.getWidget("connectionFactoryName");
        assertThat(connectionFactoryName, notNullValue());
        Widget userPassword = main.getWidget("userPassword");
        assertThat(userPassword, notNullValue());
        Widget useHttps = main.getWidget("useHttps");
        assertThat(useHttps, notNullValue());
        Widget httpsSettings = main.getWidget("httpsSettings");
        assertThat(httpsSettings, notNullValue());
        Widget property = main.getWidget("property");
        assertThat(property, notNullValue());
        Widget value = main.getWidget("value");
        assertThat(value, notNullValue());
    }

    /**
     * Checks {@link JmsDatastoreProperties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayout() {
        JmsDatastoreProperties properties = new JmsDatastoreProperties("test");
        properties.init();
        properties.refreshLayout(properties.getForm(Form.MAIN));

        assertFalse(properties.getForm(Form.MAIN).getWidget("version").isHidden());
        assertFalse(properties.getForm(Form.MAIN).getWidget("contextProvider").isHidden());
        assertFalse(properties.getForm(Form.MAIN).getWidget("serverUrl").isHidden());
        assertTrue(properties.getForm(Form.MAIN).getWidget("userPassword").isHidden());
        properties.needUserIdentity.setValue(true);
        properties.refreshLayout(properties.getForm(Form.MAIN));
        assertFalse(properties.getForm(Form.MAIN).getWidget("version").isHidden());
        assertFalse(properties.getForm(Form.MAIN).getWidget("contextProvider").isHidden());
        assertFalse(properties.getForm(Form.MAIN).getWidget("serverUrl").isHidden());
        assertFalse(properties.getForm(Form.MAIN).getWidget("userPassword").isHidden());

    }
}
