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

package org.talend.components.simplefileio;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class SimpleFileIoDatasetProperties extends PropertiesImpl implements DatasetProperties<SimpleFileIoDatastoreProperties> {

    public Property<SimpleFileIoFormat> format = PropertyFactory.newEnum("format", SimpleFileIoFormat.class).setRequired();

    public Property<String> path = PropertyFactory.newString("path", "").setRequired();

    public Property<String> recordDelimiter = PropertyFactory.newString("recordDelimiter", "\n");

    public Property<String> fieldDelimiter = PropertyFactory.newString("fieldDelimiter", ";");

    public final transient ReferenceProperties<SimpleFileIoDatastoreProperties> datastoreRef = new ReferenceProperties<>(
            "datastoreRef", SimpleFileIoDatastoreDefinition.NAME);

    public SimpleFileIoDatasetProperties(String name) {
        super(name);
    }

    @Override
    public SimpleFileIoDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(SimpleFileIoDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        format.setValue(SimpleFileIoFormat.CSV);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(format);
        mainForm.addRow(path);
        mainForm.addRow(recordDelimiter);
        mainForm.addRow(fieldDelimiter);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(recordDelimiter.getName()).setVisible(format.getValue() == SimpleFileIoFormat.CSV);
            form.getWidget(fieldDelimiter.getName()).setVisible(format.getValue() == SimpleFileIoFormat.CSV);
        }
    }

    public void afterFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

}
