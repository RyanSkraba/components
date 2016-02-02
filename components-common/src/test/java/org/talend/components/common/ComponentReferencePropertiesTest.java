// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.common.ComponentReferenceProperties.ReferenceType;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * created by pbailly on 10 Dec 2015 Detailled comment
 *
 */
public class ComponentReferencePropertiesTest {

    @Test
    public void testComponentReferenceProperties() {
        // basic element
        ComponentReferenceProperties componentReferenceProperties = new ComponentReferenceProperties("testReference");
        assertEquals("testReference", componentReferenceProperties.getName());
        assertEquals(0, componentReferenceProperties.getForms().size());

        // init
        componentReferenceProperties.init();

        // check the automatic getLayer
        assertEquals(1, componentReferenceProperties.getForms().size());

        assertNotNull(componentReferenceProperties.getForm(Form.REFERENCE));
        assertNotNull("Component", componentReferenceProperties.getForm(Form.REFERENCE).getName());

        assertNotNull(componentReferenceProperties.getForm(Form.REFERENCE).getWidget("referenceType"));
        assertEquals(1, componentReferenceProperties.getForm(Form.REFERENCE).getWidget("referenceType").getRow());
        assertEquals(Widget.WidgetType.COMPONENT_REFERENCE,
                componentReferenceProperties.getForm(Form.REFERENCE).getWidget("referenceType").getWidgetType());

        assertNotNull(componentReferenceProperties.getForm(Form.REFERENCE).getWidget("componentType"));
        assertEquals(1, componentReferenceProperties.getForm(Form.REFERENCE).getWidget("componentType").getRow());
        assertEquals(Widget.WidgetType.COMPONENT_REFERENCE,
                componentReferenceProperties.getForm(Form.REFERENCE).getWidget("componentType").getWidgetType());

        assertNotNull(componentReferenceProperties.getForm(Form.REFERENCE).getWidget("componentInstanceId"));
        assertEquals(1, componentReferenceProperties.getForm(Form.REFERENCE).getWidget("componentInstanceId").getRow());
        assertEquals(Widget.WidgetType.COMPONENT_REFERENCE,
                componentReferenceProperties.getForm(Form.REFERENCE).getWidget("componentInstanceId").getWidgetType());
    }

    @Test
    public void testReferenceType() {
        assertEquals(3, ReferenceType.values().length);
        assertArrayEquals(new ReferenceType[] { ReferenceType.THIS_COMPONENT, ReferenceType.COMPONENT_TYPE,
                ReferenceType.COMPONENT_INSTANCE }, ReferenceType.values());
    }
}
