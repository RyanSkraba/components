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
package org.talend.components.api.properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.properties.ComponentReferenceProperties.ReferenceType;
import org.talend.components.api.service.testcomponent.TestComponentProperties;

public class ComponentReferencePropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testComponentReferenceProperties() {
        // basic element
        ComponentReferenceProperties componentReferenceProperties = new ComponentReferenceProperties("testReference", null);
        assertEquals("testReference", componentReferenceProperties.getName());
        assertEquals(0, componentReferenceProperties.getForms().size());

        // init
        componentReferenceProperties.init();
    }

    @Test
    public void testReferenceType() {
        assertEquals(3, ReferenceType.values().length);
        assertArrayEquals(new ReferenceType[] { ReferenceType.THIS_COMPONENT, ReferenceType.COMPONENT_TYPE,
                ReferenceType.COMPONENT_INSTANCE }, ReferenceType.values());
    }

    @Test
    public void testEnclosingProps() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("props").init();
        System.out.println(props);
        assertEquals(3, props.referencedComponent.getForms().size());
    }

}
