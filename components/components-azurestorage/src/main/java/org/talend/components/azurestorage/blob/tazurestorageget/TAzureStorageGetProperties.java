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
package org.talend.components.azurestorage.blob.tazurestorageget;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.azurestorage.blob.AzureStorageBlobProperties;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsGetTable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public class TAzureStorageGetProperties extends AzureStorageBlobProperties implements SerializeSetVersion {

    private static final long serialVersionUID = 7248936721419046950L;

    public Property<String> localFolder = PropertyFactory.newString("localFolder").setRequired(); //$NON-NLS-1$

    public Property<Boolean> keepRemoteDirStructure = PropertyFactory.newBoolean("keepRemoteDirStructure").setRequired(); //$NON-NLS-1$

    public RemoteBlobsGetTable remoteBlobsGet = new RemoteBlobsGetTable("remoteBlobsGet"); //$NON-NLS-1$

    public TAzureStorageGetProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(widget(localFolder).setWidgetType(Widget.DIRECTORY_WIDGET_TYPE));
        mainForm.addRow(keepRemoteDirStructure);
        mainForm.addRow(widget(remoteBlobsGet).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(dieOnError);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        localFolder.setValue("");
        keepRemoteDirStructure.setValue(false);
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated = super.postDeserialize(version, setup, persistent);
        boolean migratedProperties = this.migrateProperties(version);
        return migrated || migratedProperties;
    }

    private boolean migrateProperties(int version) {
        boolean migrated = false;
        if (version < 1) {
            this.keepRemoteDirStructure.setValue(true);
            migrated = true;
        }
        return migrated;
    }
}
