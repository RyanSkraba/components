package org.talend.components.salesforce;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.wizard.ComponentWizard;

/**
 *
 */
public class SalesforceConnectionWizard extends ComponentWizard {

    SalesforceModuleListProperties mProps;

    SalesforceConnectionWizard(String repositoryLocation) {
        super(repositoryLocation);

        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties("SalesforceConnectionProperties");
        addForm(cProps.getForm(SalesforceConnectionProperties.MAIN));

        mProps = new SalesforceModuleListProperties(null, cProps, getRepositoryLocation());
        addForm(mProps.getForm(SalesforceModuleProperties.MAIN));
    }

}
