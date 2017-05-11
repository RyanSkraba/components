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
package org.talend.components.azurestorage.blob.tazurestorageput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.azurestorage.blob.AzureStorageBlobProperties;
import org.talend.components.azurestorage.blob.helpers.FileMaskTable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TAzureStoragePutProperties extends AzureStorageBlobProperties {

    private static final long serialVersionUID = -8691101981326936311L;

    public Property<String> localFolder = PropertyFactory.newString("localFolder").setRequired(); //$NON-NLS-1$

    public Property<String> remoteFolder = PropertyFactory.newString("remoteFolder"); //$NON-NLS-1$

    public Property<Boolean> useFileList = PropertyFactory.newBoolean("useFileList"); //$NON-NLS-1$

    public FileMaskTable files = new FileMaskTable("files"); //$NON-NLS-1$

    public TAzureStoragePutProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        localFolder.setValue("");
        remoteFolder.setValue("");
        useFileList.setValue(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(widget(localFolder).setWidgetType(Widget.FILE_WIDGET_TYPE));
        mainForm.addRow(remoteFolder);
        mainForm.addRow(useFileList);
        mainForm.addRow(widget(files).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(dieOnError);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.MAIN.equals(form.getName()) && form.getWidget(files.getName()) != null) {
            form.getWidget(files.getName()).setHidden(!useFileList.getValue());
        }
    }

    public void afterUseFileList() {
        refreshLayout(getForm(Form.MAIN));
    }
}
