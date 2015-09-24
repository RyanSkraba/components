package org.talend.components.salesforce;

import org.talend.components.api.Constants;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;

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
    public String getPngImagePath(WizardImageType imageType) {
        switch (imageType) {
        case TREE_ICON_16X16:
            return "connectionWizardIcon.png"; //$NON-NLS-1$
        case WIZARD_BANNER_75X66:
            return "salesforceWizardBanner.png"; //$NON-NLS-1$

        default:
            // will return null
        }
        return null;
    }

    @Override
    protected String getI18NBaseName() {
        return "org.talend.components.salesforce.message"; //$NON-NLS-1$
    }
}
