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
package org.talend.components.filedelimited.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.filedelimited.FileDelimitedProperties;

public class FileDelimitedWizard extends ComponentWizard {

    FileDelimitedProperties cProps;

    FileDelimitedWizard(ComponentWizardDefinition def, String repositoryLocation) {
        super(def, repositoryLocation);

        cProps = new FileDelimitedProperties("file").setRepositoryLocation(getRepositoryLocation());
        cProps.init();
        addForm(cProps.getForm(FileDelimitedProperties.FORM_WIZARD));

    }

    public boolean supportsProperties(ComponentProperties properties) {
        return properties instanceof FileDelimitedProperties;
    }

    public void setupProperties(FileDelimitedProperties cPropsOther) {
        cProps.copyValuesFrom(cPropsOther);
    }

}
