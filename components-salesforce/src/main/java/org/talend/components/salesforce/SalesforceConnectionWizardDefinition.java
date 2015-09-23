package org.talend.components.salesforce;

import org.talend.components.api.Constants;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

@org.springframework.stereotype.Component(Constants.COMPONENT_WIZARD_BEAN_PREFIX
        + SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME)
@aQute.bnd.annotation.component.Component(name = Constants.COMPONENT_WIZARD_BEAN_PREFIX
        + SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME, provide = ComponentWizardDefinition.class)
public class SalesforceConnectionWizardDefinition extends AbstractComponentWizardDefintion {

    public static final String COMPONENT_WIZARD_NAME = "salesforce"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public ComponentWizard createWizard(String userData) {
        return new SalesforceConnectionWizard(userData, globalContext.i18nMessageProvider);
    }

    @Override
    public String getPngImagePath() {
        return "salesforce.png"; //$NON-NLS-1$
    }

    @Override
    protected String getI18NBaseName() {
        return "org.talend.components.salesforce.message"; //$NON-NLS-1$
    }
}
