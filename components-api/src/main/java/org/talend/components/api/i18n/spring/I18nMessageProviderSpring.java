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
package org.talend.components.api.i18n.spring;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.talend.daikon.i18n.I18nMessageProvider;
import org.talend.daikon.i18n.LocaleProvider;

/**
 * I18nMessageProvider implementation for Spring container
 */
@Component
public class I18nMessageProviderSpring extends I18nMessageProvider {

    @Override
    protected LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    @Inject
    LocaleProvider localeProvider;
}
