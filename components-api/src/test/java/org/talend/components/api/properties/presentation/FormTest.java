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
package org.talend.components.api.properties.presentation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;

/**
 * created by sgandon on 14 déc. 2015
 */
public class FormTest {

    @Test
    public void testNameEqualsDisplayNameWithUsualConstructor() {
        Form form = new Form(new ComponentProperties("bar") { //$NON-NLS-1$
        }, "foo"); //$NON-NLS-1$
        assertEquals("foo", form.getName()); //$NON-NLS-1$
        assertEquals("foo", form.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testGetI18NFields() {
        Form form = new Form(new ComponentProperties("bar") { //$NON-NLS-1$
        }, "foo", null, null); //$NON-NLS-1$
        assertEquals("Ze Form DisplayName", form.getDisplayName()); //$NON-NLS-1$
        assertEquals("Ze Form Title", form.getTitle()); //$NON-NLS-1$
        assertEquals("Ze Form SubTitle", form.getSubtitle()); //$NON-NLS-1$
    }

    @Test
    public void testGetI18NFiledsWithDefaultValue() {
        String displayName = "Default Display Name"; //$NON-NLS-1$
        String title = "Default Title"; //$NON-NLS-1$
        String subTitle = "Default SubTitle"; //$NON-NLS-1$
        Form form = new Form(new ComponentProperties("bar") { //$NON-NLS-1$
        }, "foo", displayName, title); //$NON-NLS-1$
        form.setSubtitle(subTitle);
        assertEquals(displayName, form.getDisplayName());
        assertEquals(title, form.getTitle());
        assertEquals(subTitle, form.getSubtitle());
    }

}
