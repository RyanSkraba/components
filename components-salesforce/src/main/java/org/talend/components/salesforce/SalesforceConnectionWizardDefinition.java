package org.talend.components.salesforce;

import org.talend.components.api.Constants;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

@org.springframework.stereotype.Component(Constants.COMPONENT_WIZARD_BEAN_PREFIX
        + SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME)
@aQute.bnd.annotation.component.Component(name = Constants.COMPONENT_WIZARD_BEAN_PREFIX
        + SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME)
public class SalesforceConnectionWizardDefinition implements ComponentWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "salesforce"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public String getMenuItemName() {
        return "Salesforce Connection";
    }

    @Override
    public ComponentWizard createWizard(Object location) {
        return new SalesforceConnectionWizard(location);
    }

    @Override
    public String getPngImagePath() {
        return "connectionWizardIcon.png"; //$NON-NLS-1$
    }

    @Override
    public String getDisplayName() {
        return "Salesforce";
    }
}
