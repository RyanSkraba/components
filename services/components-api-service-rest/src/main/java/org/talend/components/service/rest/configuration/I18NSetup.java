//==============================================================================
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
//==============================================================================
package org.talend.components.service.rest.configuration;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.talend.daikon.i18n.GlobalI18N;

/**
 * Set the I18N up.
 */
@Configuration
public class I18NSetup extends GlobalI18N {

    private static final Logger LOG = LoggerFactory.getLogger(I18NSetup.class);

    @PostConstruct
    void init() {
        Locale.setDefault(Locale.US);
        GlobalI18N.createI18nMessageProvider(LocaleContextHolder::getLocale);
        LOG.info("Activated i18n messages ({}).", i18nMessageProvider);
    }

}
