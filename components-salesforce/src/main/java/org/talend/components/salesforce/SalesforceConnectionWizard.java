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
        cProps.init();
        addForm(cProps.getForm(SalesforceConnectionProperties.FORM_WIZARD));

        mProps = new SalesforceModuleListProperties(null, cProps, getRepositoryLocation());
        mProps.init();
        addForm(mProps.getForm(Form.MAIN));
    }

}
