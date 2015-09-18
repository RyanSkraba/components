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

import org.springframework.stereotype.Component;
import org.talend.components.api.i18n.FormatedMessageProvider;

import aQute.bnd.annotation.component.Reference;

/**
 * created by sgandon on 17 sept. 2015
 */
@Component
@aQute.bnd.annotation.component.Component
public class GlobalContext {

    @Inject
    public FormatedMessageProvider resourceBundleProvider;

    @Reference
    public void osgiInjectFormatedMessageProvider(FormatedMessageProvider messageProvider) {
        this.resourceBundleProvider = messageProvider;
    }

}
