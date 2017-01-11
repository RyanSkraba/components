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
package org.talend.components.api.service.i18n.osgi;

import java.util.Locale;

import org.talend.daikon.i18n.LocaleProvider;

import aQute.bnd.annotation.component.Component;

/**
 * LocaleProvider implementation for OSGI container
 */
@Component
public class LocaleProviderOsgi implements LocaleProvider {

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

}
