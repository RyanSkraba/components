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
package org.talend.components.api.component;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * created by pbailly on 4 Dec 2015 Detailled comment
 *
 */
public class ComponentImageTypeTest {

    @Test
    public void test() {
        assertEquals(1, ComponentImageType.values().length);
        assertEquals(ComponentImageType.PALLETE_ICON_32X32, ComponentImageType.valueOf("PALLETE_ICON_32X32")); //$NON-NLS-1$
    }

}
