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
package org.talend.components.api.context;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.talend.components.api.i18n.I18nMessageProvider;

import aQute.bnd.annotation.component.Reference;

/**
 * created by sgandon on 17 sept. 2015
 */
@Component
@aQute.bnd.annotation.component.Component(provide = GlobalContext.class)
public class GlobalContext implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalContext.class);

    @Inject
    public static I18nMessageProvider i18nMessageProvider;

    @Reference
    public void osgiInjectI18nMessagesProvider(I18nMessageProvider messageProvider) {
        i18nMessageProvider = messageProvider;
        LOG.info("Activated i18n messages ({}).", i18nMessageProvider);
    }

    /**
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        i18nMessageProvider = applicationContext.getBean(I18nMessageProvider.class);
        LOG.info("Activated i18n messages ({}).", i18nMessageProvider);
    }
}
