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
package org.talend.components.api;

import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.TranslatableImpl;

public abstract class AbstractTopLevelDefinition extends TranslatableImpl implements NamedThing {

    private static final String I18N_DISPLAY_NAME_SUFFIX = ".displayName"; //$NON-NLS-1$

    private static final String I18N_TITLE_SUFFIX = ".title"; //$NON-NLS-1$

    protected GlobalI18N globalContext;

    @Override
    public String getDisplayName() {
        return getI18nMessage(getI18nPrefix() + getName() + I18N_DISPLAY_NAME_SUFFIX);
    }

    @Override
    public String getTitle() {
        return getI18nMessage(getI18nPrefix() + getName() + I18N_TITLE_SUFFIX);
    }

    abstract protected String getI18nPrefix();
}
