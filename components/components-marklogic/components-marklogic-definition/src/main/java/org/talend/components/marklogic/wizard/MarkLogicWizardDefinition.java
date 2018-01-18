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
package org.talend.components.marklogic.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class MarkLogicWizardDefinition extends AbstractComponentWizardDefintion {

    public static final String COMPONENT_WIZARD_NAME = "marklogic"; //$NON-NLS-1$

    @Override
    public ComponentWizard createWizard(String location) {
        return new MarkLogicWizard(this, location);
    }

    @Override
    public ComponentWizard createWizard(ComponentProperties properties, String location) {
        MarkLogicWizard markLogicWizard = new MarkLogicWizard(this, location);

        MarkLogicConnectionProperties connectionProperties = (MarkLogicConnectionProperties) properties;

        markLogicWizard.setupProperties(connectionProperties);
        return markLogicWizard;
    }

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
            return "markLogicWizardBanner.png"; //$NON-NLS-1$
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
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean supportsProperties(Class<? extends ComponentProperties> propertiesClass) {
        return propertiesClass.isAssignableFrom(MarkLogicConnectionProperties.class);
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }
}
