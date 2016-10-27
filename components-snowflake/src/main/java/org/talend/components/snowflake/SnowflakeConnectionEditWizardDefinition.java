package org.talend.components.snowflake;

public class SnowflakeConnectionEditWizardDefinition extends SnowflakeConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "snowflake.edit"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
