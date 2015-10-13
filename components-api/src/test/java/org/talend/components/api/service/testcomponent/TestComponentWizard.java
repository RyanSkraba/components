package org.talend.components.api.service.testcomponent;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

public class TestComponentWizard extends ComponentWizard {

    public ComponentProperties props;

    public TestComponentWizard(ComponentWizardDefinition def, String location, I18nMessageProvider messageProvider) {
        super(def, location);
    }
}
