package org.talend.components.google.drive.wizard;

public class GoogleDriveConnectionEditWizardDefinition extends GoogleDriveConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "GoogleDrive.edit";

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
