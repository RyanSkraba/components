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

    public Property<RecordDelimiterType> recordDelimiter = PropertyFactory.newEnum("recordDelimiter", RecordDelimiterType.class)
            .setValue(RecordDelimiterType.LF);

    public Property<String> specificRecordDelimiter = PropertyFactory.newString("specificRecordDelimiter", "\\n");

    public Property<FieldDelimiterType> fieldDelimiter = PropertyFactory.newEnum("fieldDelimiter", FieldDelimiterType.class)
            .setValue(FieldDelimiterType.SEMICOLON);

    public Property<String> specificFieldDelimiter = PropertyFactory.newString("specificFieldDelimiter", ";");

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
        mainForm.addRow(specificRecordDelimiter);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(specificFieldDelimiter);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(recordDelimiter).setVisible(format.getValue() == SimpleFileIoFormat.CSV);
            form.getWidget(specificRecordDelimiter).setVisible(
                    format.getValue() == SimpleFileIoFormat.CSV && recordDelimiter.getValue().equals(RecordDelimiterType.OTHER));

            form.getWidget(fieldDelimiter).setVisible(format.getValue() == SimpleFileIoFormat.CSV);
            form.getWidget(specificFieldDelimiter).setVisible(
                    format.getValue() == SimpleFileIoFormat.CSV && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));

        }
    }

    public void afterFieldDelimiter() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterRecordDelimiter() {
        refreshLayout(getForm(Form.MAIN));
    }

    public String getRecordDelimiter() {
        if (RecordDelimiterType.OTHER.equals(recordDelimiter.getValue())) {
            return specificRecordDelimiter.getValue();
        } else {
            return recordDelimiter.getValue().getDelimiter();
        }
    }

    public String getFieldDelimiter() {
        if (FieldDelimiterType.OTHER.equals(fieldDelimiter.getValue())) {
            return specificFieldDelimiter.getValue();
        } else {
            return fieldDelimiter.getValue().getDelimiter();
        }
    }

    public void afterFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    public enum RecordDelimiterType {
        LF("\n"),
        CR("\r"),
        CRLF("\r\n"),
        OTHER("Other");

        private final String value;

        private RecordDelimiterType(final String value) {
            this.value = value;
        }

        public String getDelimiter() {
            return value;
        }
    }

    public enum FieldDelimiterType {
        SEMICOLON(";"),
        COMMA(","),
        TABULATION("\t"),
        SPACE(" "),
        OTHER("Other");

        private final String value;

        private FieldDelimiterType(final String value) {
            this.value = value;
        }

        public String getDelimiter() {
            return value;
        }
    }
}
