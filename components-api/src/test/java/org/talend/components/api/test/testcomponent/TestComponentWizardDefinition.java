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
package org.talend.components.api.test.testcomponent;

import org.springframework.stereotype.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

@Component(Constants.COMPONENT_WIZARD_BEAN_PREFIX + TestComponentWizardDefinition.COMPONENT_WIZARD_NAME)
public class TestComponentWizardDefinition implements ComponentWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "zewizard"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public String getMenuItemName() {
        return "menu item name";
    }

    @Override
    public String getPngImagePath() {
        return "connectionWizardIcon.png";
    }

    @Override
    public ComponentWizard createWizard(String location) {
        return new TestComponentWizard(location);
    }

    @Override
    public String getDisplayName() {
        return " DisplayName";
    }

    @Override
    public String getTitle() {
        return "The title";
    }

}
