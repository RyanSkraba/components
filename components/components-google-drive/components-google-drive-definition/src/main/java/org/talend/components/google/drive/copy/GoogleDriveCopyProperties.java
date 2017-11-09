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
package org.talend.components.google.drive.copy;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.google.drive.GoogleDriveComponentProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class GoogleDriveCopyProperties extends GoogleDriveComponentProperties {

    public enum CopyMode {
        File,
        Folder
    }

    public Property<CopyMode> copyMode = newEnum("copyMode", CopyMode.class);

    public Property<AccessMethod> sourceAccessMethod = newEnum("sourceAccessMethod", AccessMethod.class);

    public Property<String> source = newString("source");

    public Property<AccessMethod> destinationFolderAccessMethod = newEnum("destinationFolderAccessMethod", AccessMethod.class);

    public Property<String> destinationFolder = newString("destinationFolder");

    public Property<Boolean> rename = newBoolean("rename");

    public Property<String> newName = newString("newName");

    public Property<Boolean> deleteSourceFile = newBoolean("deleteSourceFile");

    public GoogleDriveCopyProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        sourceAccessMethod.setPossibleValues(AccessMethod.values());
        sourceAccessMethod.setValue(AccessMethod.Name);
        source.setValue("");
        destinationFolderAccessMethod.setPossibleValues(AccessMethod.values());
        destinationFolderAccessMethod.setValue(AccessMethod.Name);
        destinationFolder.setValue("");

        copyMode.setPossibleValues(CopyMode.values());
        copyMode.setValue(CopyMode.File);

        rename.setValue(false);
        deleteSourceFile.setValue(false);

        Schema s = SchemaBuilder.builder().record(GoogleDriveCopyDefinition.COMPONENT_NAME).fields()//
                .name(GoogleDriveCopyDefinition.RETURN_SOURCE_ID).type().nullable().stringType().noDefault()//
                .name(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID).type().nullable().stringType().noDefault()//
                .endRecord();
        s.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        schemaMain.schema.setValue(s);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(copyMode);
        mainForm.addRow(source);
        mainForm.addColumn(sourceAccessMethod);
        mainForm.addRow(destinationFolder);
        mainForm.addColumn(destinationFolderAccessMethod);
        mainForm.addRow(rename);
        mainForm.addRow(newName);
        mainForm.addRow(deleteSourceFile);
        mainForm.addRow(schemaMain.getForm(Form.REFERENCE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.MAIN.equals(form.getName())) {
            if (CopyMode.File.equals(copyMode.getValue())) {
                form.getWidget(deleteSourceFile.getName()).setVisible(true);
            } else {
                form.getWidget(deleteSourceFile.getName()).setVisible(false);
            }
            form.getWidget(newName.getName()).setVisible(rename.getValue());
        }
    }

    public void afterCopyMode() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterRename() {
        refreshLayout(getForm(Form.MAIN));
    }

}
