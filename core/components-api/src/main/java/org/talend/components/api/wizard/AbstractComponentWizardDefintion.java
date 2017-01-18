// ============================================================================
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
// ============================================================================
package org.talend.components.api.wizard;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

public abstract class AbstractComponentWizardDefintion extends AbstractTopLevelDefinition implements ComponentWizardDefinition {

    private static final String I18N_MENU_NAME_SUFFIX = ".menu.name"; //$NON-NLS-1$

    /**
     * The class of the {@link ComponentProperties} object that this wizard will service. This is used to find the
     * appropriate wizard given a set of stored properties.
     */
    protected Class<?> propertiesClass;

    @Override
    protected String getI18nPrefix() {
        return "wizard."; //$NON-NLS-1$
    }

    @Override
    public String getMenuItemName() {
        return getI18nMessage(getI18nPrefix() + getName() + I18N_MENU_NAME_SUFFIX);
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

    @Override
    public String getImagePath() {
        return getPngImagePath(WizardImageType.TREE_ICON_16X16);
    }

    @Override
    public Class getPropertiesClass() {
        // this should eventually be used to create the Properties class associated with the wizard.
        return null;
    }

}
