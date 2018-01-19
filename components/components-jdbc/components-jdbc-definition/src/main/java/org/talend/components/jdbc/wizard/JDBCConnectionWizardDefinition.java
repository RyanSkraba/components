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
package org.talend.components.jdbc.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.daikon.definition.DefinitionImageType;

/**
 * JDBC wizard for creating a new JDBC meta data
 *
 */
public class JDBCConnectionWizardDefinition extends AbstractComponentWizardDefintion {

    public static final String COMPONENT_WIZARD_NAME = "JDBC";

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public ComponentWizard createWizard(String location) {
        return new JDBCConnectionWizard(this, location);
    }

    @Override
    public boolean supportsProperties(Class<? extends ComponentProperties> propertiesClass) {
        return propertiesClass.isAssignableFrom(JDBCConnectionWizardProperties.class)
                || (propertiesClass == JDBCConnectionModule.class);// please see
                                                                   // org.talend.designer.core.generic.model.getWizardDefinition
                                                                   // method, this is the reason why we add the
                                                                   // JDBCConnectionModule.class here, IMHO, the logic is not
                                                                   // good, now only fix it like this

        // after adding the JDBCConnectionModule.class, the Property Type ui can show in the tjdbc components, but can't map it to
        // metadata repository, after wanghong's help, we found it's a bug of TUP part, need they fix it
    }

    @Override
    public ComponentWizard createWizard(ComponentProperties properties, String location) {
        JDBCConnectionWizard wizard = (JDBCConnectionWizard) createWizard(location);
        if (properties instanceof JDBCConnectionWizardProperties) {
            wizard.setupProperties((JDBCConnectionWizardProperties) properties);
        }
        return wizard;
    }

    @Deprecated
    @Override
    public String getPngImagePath(WizardImageType imageType) {
        switch (imageType) {
        case TREE_ICON_16X16:
            return getImagePath(DefinitionImageType.TREE_ICON_16X16);
        case WIZARD_BANNER_75X66:
            return getImagePath(DefinitionImageType.WIZARD_BANNER_75X66);
        default:
            // will return null
        }
        return null;
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case TREE_ICON_16X16:
            return "connectionWizardIcon.png"; //$NON-NLS-1$
        case WIZARD_BANNER_75X66:
            return "JDBCWizardBanner.png"; //$NON-NLS-1$
        default:
            // will return null
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }

}
