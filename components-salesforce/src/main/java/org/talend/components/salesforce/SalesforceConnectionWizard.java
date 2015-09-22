package org.talend.components.salesforce;

import org.talend.components.api.wizard.ComponentWizard;

/**
 *
 */
public class SalesforceConnectionWizard extends ComponentWizard {

    SalesforceConnectionWizard(String userData, I18nMessageProvider provider) {
        super(userData, provider, "org.talend.components.salesforce.message"); //$NON-NLS-1$

        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties(i18nMessageProvider);
        addForm(cProps.getForm(SalesforceConnectionProperties.MAIN));

        SalesforceModuleProperties mProps = new SalesforceModuleProperties(i18nMessageProvider, cProps);
        addForm(mProps.getForm(SalesforceModuleProperties.MAIN));
    }
}
