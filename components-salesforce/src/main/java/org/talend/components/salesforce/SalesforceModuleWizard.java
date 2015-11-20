package org.talend.components.salesforce;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

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
        if (properties instanceof SalesforceConnectionProperties)
            return true;
        return false;
    }

    public void setupProperties(SalesforceConnectionProperties cProps) {
        mProps.setConnection(cProps);
    }

}
