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
package org.talend.components.api.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class PropertyPathConnectorTest {

    /**
     * Test method for {@link org.talend.components.api.component.PropertyPathConnector#hashCode()}.
     */
    @Test
    public void testHashCode() {
        PropertyPathConnector fooPC = new PropertyPathConnector("foo", null); //$NON-NLS-1$
        PropertyPathConnector barPC = new PropertyPathConnector("bar", null); //$NON-NLS-1$
        assertNotEquals(fooPC.hashCode(), barPC.hashCode());
        assertEquals(fooPC.hashCode(), fooPC.hashCode());// check that the method returna always the same value
        assertEquals(barPC.hashCode(), barPC.hashCode());// check that the method returna always the same value
    }

    /**
     * Test method for
     * {@link org.talend.components.api.component.PropertyPathConnector#PropertyPathConnector(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testPropertyPathConnector() {
        PropertyPathConnector pathConnector = new PropertyPathConnector("foo", "foo.bar"); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals(pathConnector.getName(), "foo"); //$NON-NLS-1$
        assertEquals(pathConnector.getPropertyPath(), "foo.bar"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.talend.components.api.component.PropertyPathConnector#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        PropertyPathConnector pathConnector = new PropertyPathConnector(Connector.MAIN_NAME, null);
        assertEquals("Main", pathConnector.getDisplayName());
    }

    /**
     * Test method for {@link org.talend.components.api.component.PropertyPathConnector#getPropertyPath()}.
     */
    @Test
    public void testGetPropertyPath() {
        PropertyPathConnector pathConnector = new PropertyPathConnector("foo", "foo.bar"); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals(pathConnector.getPropertyPath(), "foo.bar"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.talend.components.api.component.PropertyPathConnector#equals(java.lang.Object)}.
     */
    @Test
    public void testEqualsObject() {
        PropertyPathConnector fooPC = new PropertyPathConnector("foo", null); //$NON-NLS-1$
        PropertyPathConnector anotherFooPC = new PropertyPathConnector("foo", null); //$NON-NLS-1$
        PropertyPathConnector barPC = new PropertyPathConnector("bar", null); //$NON-NLS-1$
        assertNotEquals(fooPC, barPC);
        assertEquals(fooPC, anotherFooPC);
    }

}
