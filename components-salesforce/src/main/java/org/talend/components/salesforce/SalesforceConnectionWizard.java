package org.talend.components.salesforce;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;

/**
 * Handles the creating a connection and creating the modules associated with the connection.
 */
public class SalesforceConnectionWizard extends ComponentWizard {

    SalesforceModuleListProperties mProps;

    SalesforceConnectionWizard(ComponentWizardDefinition def, String repositoryLocation, ComponentService compService) {
        super(def, repositoryLocation);

        SalesforceConnectionProperties cProps = new SalesforceConnectionProperties("connection");
        cProps.init();
        addForm(cProps.getForm(SalesforceConnectionProperties.FORM_WIZARD));

        mProps = new SalesforceModuleListProperties("mProps").setConnection(cProps).setRepositoryLocation(getRepositoryLocation())
                .setComponentService(compService);
        mProps.init();
        addForm(mProps.getForm(Form.MAIN));
    }

    public boolean supportsProperties(ComponentProperties properties) {
        if (properties instanceof SalesforceConnectionProperties) {
            return true;
        }
        return false;
    }

    public void setupProperties(SalesforceConnectionProperties cProps) {
        // Update the connection form properties
        getForms().get(0).setProperties(cProps);
        mProps.setConnection(cProps);
    }

}
