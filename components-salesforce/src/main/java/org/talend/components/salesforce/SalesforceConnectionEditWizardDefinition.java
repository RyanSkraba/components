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
package org.talend.components.salesforce;

import org.talend.components.api.Constants;
import org.talend.components.api.wizard.ComponentWizardDefinition;

import aQute.bnd.annotation.component.Component;

public class SalesforceConnectionEditWizardDefinition extends SalesforceConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "salesforce.edit"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
