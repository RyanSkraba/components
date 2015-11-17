package org.talend.components.salesforce;

import org.talend.components.api.Constants;
import org.talend.components.api.wizard.ComponentWizardDefinition;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_WIZARD_BEAN_PREFIX
        + SalesforceConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME, provide = ComponentWizardDefinition.class)
public class SalesforceConnectionEditWizardDefinition extends SalesforceConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "salesforce.edit"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
