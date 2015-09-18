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
package org.talend.components.api.i18n;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.talend.daikon.i18n.FormatedMessage;
import org.talend.daikon.i18n.LocaleProvider;

import aQute.bnd.annotation.component.Reference;

/**
 * created by sgandon on 17 sept. 2015
 */
@Component
@aQute.bnd.annotation.component.Component
public class FormatedMessageProvider {

    @Inject // used by spring see below for osgi
    LocaleProvider localeProvider;

    @Reference
    public void osgiInjectLocalProvider(LocaleProvider locProvder) {
        this.localeProvider = locProvder;
    }

    /**
     * Return a FormatedMessage with a resource bundle found at the path related to the classloader
     * 
     * @param classLoader, use to create the underlying resource bundle.
     * @param baseName, used to create the underlying resource bundle, see {@link ResourceBundle#getBundle(String,
     * java.util.Locale, ClassLoader, java.util.ResourceBundle.Control))}
     * @return a FormatedMessage instance to handle i18n using specific platform implementation, if none is provided,
     * the local vm Locale will be used.
     */
    public FormatedMessage getFormatedMessage(ClassLoader classLoader, String baseName) {
        return new FormatedMessage(localeProvider, classLoader, baseName);
    }

}
