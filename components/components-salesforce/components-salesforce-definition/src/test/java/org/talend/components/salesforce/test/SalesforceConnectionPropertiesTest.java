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

package org.talend.components.salesforce.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;

/**
 *
 */
public class SalesforceConnectionPropertiesTest extends SalesforceTestBase {

    private SalesforceConnectionProperties properties;

    @Before
    public void setUp() {
        properties = spy(new SalesforceConnectionProperties("connection"));
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertEquals(SalesforceConnectionProperties.URL, properties.endpoint.getValue());
        assertEquals(SalesforceConnectionProperties.DEFAULT_API_VERSION, properties.apiVersion.getValue());
        assertEquals(SalesforceConnectionProperties.LoginType.Basic, properties.loginType.getValue());
        assertEquals(Boolean.FALSE, properties.reuseSession.getValue());
        assertEquals(Integer.valueOf(60000), properties.timeout.getValue());
        assertEquals(Boolean.TRUE, properties.httpChunked.getValue());
        assertNull(properties.getReferencedComponentId());
        assertNull(properties.getReferencedConnectionProperties());
    }

    @Test
    public void testLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm);
        assertTrue(mainForm.getWidget(properties.userPassword.getName()).isVisible());
        assertFalse(mainForm.getWidget(properties.oauth.getName()).isVisible());

        Form advForm = properties.getForm(Form.ADVANCED);
        assertNotNull(advForm);

        Form refForm = properties.getForm(Form.REFERENCE);
        assertNotNull(refForm);

        Form wizardForm = properties.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        assertNotNull(wizardForm);
    }

    @Test
    public void testChangeReuseSession() {
        properties.init();

        reset(properties);

        properties.reuseSession.setValue(true);
        properties.afterReuseSession();

        Form advForm = properties.getForm(Form.ADVANCED);

        verify(properties, times(1)).refreshLayout(advForm);

        assertTrue(advForm.getWidget(properties.reuseSession.getName()).isVisible());
    }

    @Test
    public void testChangeLoginType() {
        properties.init();

        reset(properties);

        properties.loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        properties.afterLoginType();

        verify(properties, times(3)).refreshLayout(any(Form.class));

        Form mainForm = properties.getForm(Form.MAIN);
        assertEquals(SalesforceConnectionProperties.OAUTH_URL, properties.endpoint.getValue());
        testLoginTypeWidgets(mainForm);

        Form wizardForm = properties.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        testLoginTypeWidgets(wizardForm);

        // Switch back to Basic auth mode

        properties.loginType.setValue(SalesforceConnectionProperties.LoginType.Basic);
        properties.afterLoginType();

        assertEquals(SalesforceConnectionProperties.URL, properties.endpoint.getValue());
    }

    @Test
    public void testChangeReferencedComponent() {
        properties.init();

        reset(properties);

        SalesforceConnectionProperties referencedProperties = new SalesforceConnectionProperties("reference");

        properties.referencedComponent.componentInstanceId.setValue("tSalesforceConnection_1");
        properties.referencedComponent.setReference(referencedProperties);
        properties.afterReferencedComponent();

        verify(properties, times(3)).refreshLayout(any(Form.class));

        assertEquals("tSalesforceConnection_1", properties.getReferencedComponentId());
        assertEquals(referencedProperties, properties.getReferencedConnectionProperties());

        Form mainForm = properties.getForm(Form.MAIN);
        assertFalse(mainForm.getWidget(properties.loginType.getName()).isVisible());
        assertFalse(mainForm.getWidget(properties.oauth.getName()).isVisible());
        assertFalse(mainForm.getWidget(properties.userPassword.getName()).isVisible());

        Form advForm = properties.getForm(Form.ADVANCED);
        assertFalse(advForm.getWidget(properties.proxy.getName()).isVisible());
        assertFalse(advForm.getWidget(properties.timeout.getName()).isVisible());
        assertFalse(advForm.getWidget(properties.httpChunked.getName()).isVisible());
    }

    @Test
    public void testValidateTestConnection() throws Exception {
        properties.init();

        Form wizardForm = properties.getForm(SalesforceConnectionProperties.FORM_WIZARD);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                equalTo(properties), createDefaultTestDataset())) {
            testFixture.setUp();

            // Valid

            ValidationResult vr1 = properties.validateTestConnection();
            assertEquals(ValidationResult.Result.OK, vr1.getStatus());
            assertTrue(wizardForm.isAllowForward());

            // Not valid

            when(testFixture.runtimeSourceOrSink.validate(any(RuntimeContainer.class)))
                    .thenReturn(new ValidationResultMutable().setStatus(ValidationResult.Result.ERROR).setMessage("Error"));
            ValidationResult vr2 = properties.validateTestConnection();
            assertEquals(ValidationResult.Result.ERROR, vr2.getStatus());
            assertFalse(wizardForm.isAllowForward());
        }
    }

    private void testLoginTypeWidgets(Form form) {
        assertTrue(form.getWidget(properties.oauth.getName()).isVisible());
        assertFalse(form.getWidget(properties.userPassword.getName()).isVisible());

    }
}
