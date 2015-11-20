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
package org.talend.components.api.i18n.osgi;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.daikon.i18n.LocaleProvider;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * I18nMessageProvider implementation for OSGI container
 */
@Component(provide = I18nMessageProvider.class)
public class I18nMessageProviderOsgi extends I18nMessageProvider {

    @Override
    protected LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    LocaleProvider localeProvider;

    @Reference
    public void osgiInjectLocalProvider(LocaleProvider locProvder) {
        this.localeProvider = locProvder;
    }

}
