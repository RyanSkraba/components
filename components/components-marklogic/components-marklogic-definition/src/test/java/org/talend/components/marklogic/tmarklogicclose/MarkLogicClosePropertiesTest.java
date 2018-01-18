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
package org.talend.components.marklogic.tmarklogicclose;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;

import static org.junit.Assert.*;

public class MarkLogicClosePropertiesTest {

    MarkLogicCloseProperties closeProperties;

    @Before
    public void setUp() {
        closeProperties = new MarkLogicCloseProperties("close");
    }

    @Test
    public void testSetupLayout() {
        assertEquals(0, closeProperties.getForms().size());
        closeProperties.setupLayout();
        assertEquals(1, closeProperties.getForms().size());
        assertNotNull(closeProperties.getForm(Form.MAIN).getWidget(closeProperties.referencedComponent.getName()));
    }

    @Test
    public void testGetReferencedComponentId() {
        String expectedStringValue = "SomeStringValue";;
        String referencedComponentId;

        closeProperties.referencedComponent.componentInstanceId.setValue(expectedStringValue);
        referencedComponentId = closeProperties.getReferencedComponentId();

        assertEquals(referencedComponentId, expectedStringValue);
    }

}