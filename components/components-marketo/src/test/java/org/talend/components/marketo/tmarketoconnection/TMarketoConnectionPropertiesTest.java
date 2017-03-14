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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.daikon.properties.presentation.Form;

public class TMarketoConnectionPropertiesTest {

    TMarketoConnectionProperties props;

    @Before
    public void setup() {
        props = new TMarketoConnectionProperties("test");
    }

    @Test
    public final void testTMarketoConnectionProperties() {
        assertTrue(props.getName().equals("test"));
        props.setupProperties();
        assertEquals("", props.endpoint.getValue());
        assertEquals("", props.secretKey.getValue());
        assertEquals("", props.clientAccessId.getValue());
    }

    @Test
    public final void testReferencedComponent() {
        assertNull(props.getReferencedComponentId());
        assertNull(props.getReferencedConnectionProperties());

        props.setupProperties();
        props.setupLayout();
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(props.getForm(Form.MAIN).getWidget(props.endpoint.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.clientAccessId.getName()).isVisible());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.secretKey.getName()).isVisible());
        assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).getWidget(props.endpoint.getName()).isVisible());
        assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).getWidget(props.clientAccessId.getName()).isVisible());
        assertTrue(props.getForm(TMarketoConnectionProperties.FORM_WIZARD).getWidget(props.secretKey.getName()).isVisible());

        TMarketoConnectionProperties rProps = new TMarketoConnectionProperties("ref");
        rProps.setupProperties();
        props.referencedComponent.referenceType.setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        props.referencedComponent.componentInstanceId.setValue(TMarketoConnectionDefinition.COMPONENT_NAME);
        // props.referencedComponent.componentProperties = rProps;
        props.referencedComponent.setReference(rProps);

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

}
