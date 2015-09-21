package org.talend.components.salesforce;

import org.talend.components.api.wizard.ComponentWizard;

/**
 *
 */
public class SalesforceConnectionWizard extends ComponentWizard {

    SalesforceConnectionWizard(String userData) {
        super(userData);

        // SalesforceConnectionProperties cProps = new SalesforceConnectionProperties();
        // addForm(cProps.getForm(SalesforceConnectionProperties.MAIN));
        //
        // SalesforceModuleProperties mProps = new SalesforceModuleProperties(cProps);
        // addForm(mProps.getForm(SalesforceModuleProperties.MAIN));
    }
}
