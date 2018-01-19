package org.talend.components.google.drive.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.AbstractComponentWizardDefintion;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class GoogleDriveConnectionWizardDefinition extends AbstractComponentWizardDefintion {

    public static final String COMPONENT_WIZARD_NAME = "GoogleDrive";

    public static final String CONNECTION_WIZARD_ICON_PNG = "connectionWizardIcon.png";

    public static final String WIZARD_BANNER_PNG = "GoogleDriveWizardBanner.png";

    @Override
    public ComponentWizard createWizard(String location) {
        return new GoogleDriveConnectionWizard(this, location);
    }

    @Override
    public ComponentWizard createWizard(ComponentProperties properties, String location) {
        GoogleDriveConnectionWizard wizard = (GoogleDriveConnectionWizard) createWizard(location);
        wizard.setupProperties((GoogleDriveConnectionProperties) properties);

        return wizard;
    }

    @Override
    public boolean supportsProperties(Class<? extends ComponentProperties> propertiesClass) {
        return propertiesClass.isAssignableFrom(GoogleDriveConnectionProperties.class);
    }

    public String getPngImagePath(DefinitionImageType imageType) {
        switch (imageType) {
        case TREE_ICON_16X16:
            return CONNECTION_WIZARD_ICON_PNG;
        case WIZARD_BANNER_75X66:
            return WIZARD_BANNER_PNG;
        case SVG_ICON:
            break;
        case PALETTE_ICON_32X32:
            break;
        }
        return null;
    }

    @Override
    public String getPngImagePath(WizardImageType imageType) {
        return getPngImagePath(DefinitionImageType.valueOf(imageType.name()));
    }

    @Override
    public String getImagePath(DefinitionImageType definitionImageType) {
        return getPngImagePath(definitionImageType);
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }
}
