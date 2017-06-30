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
package org.talend.components.marketo.tmarketoconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.DEFAULT_ENDPOINT_REST;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoConnectionPropertiesTest extends MarketoTestBase {

    TMarketoConnectionProperties props;

    @Before
    public void setup() {
        props = new TMarketoConnectionProperties("test");
        props.setupProperties();
        props.setupLayout();
        props.refreshLayout(props.getForm(Form.MAIN));
        props.afterApiMode();
    }

    @Test
    public final void testTMarketoConnectionProperties() {
        assertTrue(props.getName().equals("test"));
        assertEquals(DEFAULT_ENDPOINT_REST, props.endpoint.getValue());
        assertEquals("", props.secretKey.getValue());
        assertEquals("", props.clientAccessId.getValue());
    }

    @Test
    public final void testReferencedComponent() throws Exception {
        assertNull(props.getReferencedComponentId());
        assertNull(props.getReferencedConnectionProperties());
        assertEquals(props, props.getConnectionProperties());

        assertTrue(props.getForm(Form.MAIN).getWidget(props.endpoint.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.clientAccessId.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.secretKey.getName()).isVisible());
        assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).getWidget(props.endpoint.getName()).isVisible());
        assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).getWidget(props.clientAccessId.getName()).isVisible());
        assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).getWidget(props.secretKey.getName()).isVisible());

        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            when(sandboxedInstanceTestFixture.runtimeSourceOrSink
                    .validateConnection(any(MarketoProvideConnectionProperties.class)))
                            .thenReturn(new ValidationResult(Result.ERROR));
            assertEquals(Result.ERROR, props.validateTestConnection().getStatus());
            assertEquals(Result.ERROR, props.afterFormFinishWizard(null).getStatus());
        }

        TMarketoConnectionProperties rProps = new TMarketoConnectionProperties("ref");
        rProps.setupProperties();
        props.referencedComponent.referenceType.setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        props.referencedComponent.componentInstanceId.setValue(TMarketoConnectionDefinition.COMPONENT_NAME);
        // props.referencedComponent.componentProperties = rProps;
        props.referencedComponent.setReference(rProps);
        assertEquals(rProps, props.getConnectionProperties());

        assertNotNull(props.getReferencedComponentId());
        assertNotNull(props.getReferencedConnectionProperties());
        props.afterReferencedComponent();
        assertFalse(props.getForm(Form.MAIN).getWidget(props.endpoint.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.clientAccessId.getName()).isVisible());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.secretKey.getName()).isVisible());

        props.referencedComponent.componentInstanceId.setValue("comp");
        props.afterReferencedComponent();
        assertTrue(props.getForm(Form.MAIN).getWidget(props.endpoint.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.clientAccessId.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.secretKey.getName()).isVisible());
    }

    @Test
    public void testValidateTestConnection() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            props.validateTestConnection();
            assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).isAllowForward());
            assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).isAllowFinish());
        }
    }

    @Test
    public void testAfterFormFinishWizard() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            assertEquals(Result.OK, props.afterFormFinishWizard(repo).getStatus());
        }
    }

}
