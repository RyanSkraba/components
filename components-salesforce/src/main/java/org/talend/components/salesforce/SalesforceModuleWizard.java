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
package org.talend.components.salesforce;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.properties.presentation.Form;

/**
 * A single-page wizard that just handles the selection of modules. This must always be created with the connection
 * properties.
 */
public class SalesforceModuleWizard extends ComponentWizard {

    SalesforceModuleListProperties mProps;

    SalesforceModuleWizard(ComponentWizardDefinition def, String repositoryLocation) {
        super(def, repositoryLocation);

        mProps = new SalesforceModuleListProperties("mProps").setRepositoryLocation(getRepositoryLocation());
        mProps.init();
        addForm(mProps.getForm(Form.MAIN));
    }

    public boolean supportsProperties(ComponentProperties properties) {
        if (properties instanceof SalesforceConnectionProperties) {
            return true;
        }
        return false;
    }

    public void setupProperties(SalesforceConnectionProperties cProps) {
        mProps.setConnection(cProps);
    }

}
