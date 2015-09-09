package org.talend.components.salesforce;

import org.talend.components.api.ComponentWizard;
import org.talend.components.api.ComponentWizardDefinition;
import org.talend.components.api.Constants;

/**
 *
 */
@org.springframework.stereotype.Component(Constants.COMPONENT_WIZARD_BEAN_PREFIX + SalesforceConnectionWizardDefinition.NAME)
@aQute.bnd.annotation.component.Component(name = Constants.COMPONENT_WIZARD_BEAN_PREFIX + SalesforceConnectionWizardDefinition.NAME)
public class SalesforceConnectionWizardDefinition implements ComponentWizardDefinition {

    protected static final String NAME = "Salesforce";

    @Override
    public String getName() {
        return "Salesforce";
    }

    @Override
    public String getMenuItemName() {
        return "Salesforce Connection";
    }

    @Override
    public Object getIcon() {
        // FIXME
        return null;
    }

    @Override public ComponentWizard createWizard(Object location) {
        return new SalesforceConnectionWizard(location);
    }
}
