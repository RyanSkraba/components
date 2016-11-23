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
package org.talend.components.api.service.i18n.osgi;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

/**
 * created by pbailly on 10 Dec 2015 Detailled comment
 *
 */
public class LocaleProviderOsgiTest {

    /**
     * Test method for {@link org.talend.components.api.i18n.osgi.LocaleProviderOsgi#getLocale()}.
     */
    @Test
    public void testGetLocale() {
        LocaleProviderOsgi locale = new LocaleProviderOsgi();
        assertEquals(Locale.getDefault(), locale.getLocale());
    }

}
