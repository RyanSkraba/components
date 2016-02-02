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
package org.talend.components.api.context.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessageProvider;

/**
 * GlobalContext implementation for Spring container
 */
@Component
public class GlobalContextSpring extends GlobalI18N implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalContextSpring.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        i18nMessageProvider = applicationContext.getBean(I18nMessageProvider.class);
        LOG.info("Activated i18n messages ({}).", i18nMessageProvider);
    }
}
