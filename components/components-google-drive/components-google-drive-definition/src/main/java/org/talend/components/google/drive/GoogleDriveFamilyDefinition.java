// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.google.drive;

import aQute.bnd.annotation.component.Component;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.google.drive.connection.GoogleDriveConnectionDefinition;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;
import org.talend.components.google.drive.create.GoogleDriveCreateDefinition;
import org.talend.components.google.drive.data.GoogleDriveDatasetDefinition;
import org.talend.components.google.drive.data.GoogleDriveDatastoreDefinition;
import org.talend.components.google.drive.data.GoogleDriveInputDefinition;
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.get.GoogleDriveGetDefinition;
import org.talend.components.google.drive.list.GoogleDriveListDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.wizard.GoogleDriveConnectionEditWizardDefinition;
import org.talend.components.google.drive.wizard.GoogleDriveConnectionWizardDefinition;

import com.google.auto.service.AutoService;

@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + GoogleDriveFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class GoogleDriveFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "GoogleDrive";

    public GoogleDriveFamilyDefinition() {
        super(NAME,
                // components
                new GoogleDriveConnectionDefinition(), new GoogleDriveCreateDefinition(), new GoogleDriveDeleteDefinition(),
                new GoogleDriveListDefinition(), new GoogleDriveGetDefinition(), new GoogleDrivePutDefinition(),
                new GoogleDriveCopyDefinition(),
                // wizards
                new GoogleDriveConnectionWizardDefinition(), new GoogleDriveConnectionEditWizardDefinition(),
                // datastore, dataset, component
                new GoogleDriveDatastoreDefinition(), new GoogleDriveDatasetDefinition(), new GoogleDriveInputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
