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
package org.talend.components.api.i18n.internal;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.talend.daikon.i18n.LocaleProvider;

/**
 * created by sgandon on 14 sept. 2015
 */
@Component
public class LocaleProviderSpring implements LocaleProvider {

    Locale locale = Locale.FRANCE;// FIXE ME this is just for test

    @Override
    public Locale getLocale() {
        return locale;
    }

}
