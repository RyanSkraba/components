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
package org.talend.components.service.rest.configuration;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessageProvider;

/**
 * Set the I18N up.
 */
@Configuration
public class I18NSetup extends GlobalI18N {

    private static final Logger LOG = LoggerFactory.getLogger(I18NSetup.class);

    @Autowired
    private ApplicationContext applicationContext;


    @PostConstruct
    void init() {
        i18nMessageProvider = applicationContext.getBean(I18nMessageProvider.class);
        LOG.info("Activated i18n messages ({}).", i18nMessageProvider);
    }

}
