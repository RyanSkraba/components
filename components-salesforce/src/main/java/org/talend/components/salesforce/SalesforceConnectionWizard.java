package org.talend.components.salesforce;

import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.wizard.ComponentWizard;

/**
 *
 */
public class SalesforceConnectionWizard extends ComponentWizard {

    SalesforceModuleListProperties mProps;

    SalesforceConnectionWizard(String repositoryLocation) {
        super(repositoryLocation);

        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties("SalesforceConnectionProperties",
                SalesforceConnectionProperties.INCLUDE_NAME);
        addForm(cProps.getForm(Form.MAIN));

        mProps = new SalesforceModuleListProperties(null, cProps, getRepositoryLocation());
        addForm(mProps.getForm(Form.MAIN));
    }

}
