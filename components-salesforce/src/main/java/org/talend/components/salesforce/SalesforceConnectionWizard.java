package org.talend.components.salesforce;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.properties.Repository;
import org.talend.components.api.wizard.ComponentWizard;

/**
 *
 */
public class SalesforceConnectionWizard extends ComponentWizard {

    SalesforceModuleListProperties mProps;

    SalesforceConnectionWizard(String repositoryLocation, I18nMessageProvider provider) {
        super(repositoryLocation, provider, "org.talend.components.salesforce.message"); //$NON-NLS-1$

        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties(i18nMessageProvider,
                SalesforceConnectionProperties.INCLUDE_NAME);
        addForm(cProps.getForm(SalesforceConnectionProperties.MAIN));

        // FIXME the Repository will not be here, it will ulimately be a call to the component service.
        mProps = new SalesforceModuleListProperties(i18nMessageProvider, cProps, getRepositoryLocation());
        addForm(mProps.getForm(SalesforceModuleProperties.MAIN));
    }

}
