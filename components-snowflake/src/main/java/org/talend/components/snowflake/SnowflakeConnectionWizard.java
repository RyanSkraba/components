package org.talend.components.snowflake;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.properties.presentation.Form;

/**
 * Handles the creating a connection and creating the modules associated with the connection.
 */
public class SnowflakeConnectionWizard extends ComponentWizard {

    SnowflakeConnectionProperties cProps;

    SnowflakeTableListProperties tProps;

    SnowflakeConnectionWizard(ComponentWizardDefinition def, String repositoryLocation) {
        super(def, repositoryLocation);

        cProps = new SnowflakeConnectionProperties("connection");
        cProps.init();
        addForm(cProps.getForm(SnowflakeConnectionProperties.FORM_WIZARD));

        tProps = new SnowflakeTableListProperties("tProps").setConnection(cProps).setRepositoryLocation(getRepositoryLocation());
        tProps.init();
        addForm(tProps.getForm(Form.MAIN));
    }

    public boolean supportsProperties(ComponentProperties properties) {
        return properties instanceof SnowflakeConnectionProperties;
    }

    public void setupProperties(SnowflakeConnectionProperties cPropsOther) {
        cProps.copyValuesFrom(cPropsOther);
        tProps.setConnection(cProps);
    }

}
