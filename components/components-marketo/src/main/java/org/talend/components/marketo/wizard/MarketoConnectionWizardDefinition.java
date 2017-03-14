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
package org.talend.components.marketo.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class MarketoConnectionWizardDefinition extends AbstractComponentWizardDefintion {

    public static final String COMPONENT_WIZARD_NAME = "marketo";

    @Override
    public ComponentWizard createWizard(String location) {
        return new MarketoConnectionWizard(this, location);
    }

    @Override
    public ComponentWizard createWizard(ComponentProperties properties, String location) {
        MarketoConnectionWizard wizard = (MarketoConnectionWizard) createWizard(location);
        wizard.setupProperties((TMarketoConnectionProperties) properties);

        return wizard;
    }

    @Override
    public boolean supportsProperties(Class<? extends ComponentProperties> propertiesClass) {
        return propertiesClass.isAssignableFrom(TMarketoConnectionProperties.class);
    }

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case TREE_ICON_16X16:
            return "connectionWizardIcon.png";
        case WIZARD_BANNER_75X66:
            return "marketoWizardBanner.png";
        default:
        }
        return null;
    }

    @Override
    public String getPngImagePath(WizardImageType imageType) {
        return getImagePath(DefinitionImageType.TREE_ICON_16X16);
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
