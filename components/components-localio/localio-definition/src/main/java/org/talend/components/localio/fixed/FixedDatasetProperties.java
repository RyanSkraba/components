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

package org.talend.components.localio.fixed;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * The fixed dataset contains a set of fixed records, and must be completely configured with its contents.
 *
 * Three types of fixed data are suppored:
 *
 * <ol>
 * <li>{@link RecordFormat#CSV} for comma delimited data. The record and field delimiter can be configured, and the schema is
 * optionally provided.</li>
 * <li>{@link RecordFormat#JSON} for data in JSON format. No schema is necessary and is inferred by the JSON data itself.</li>
 * <li>{@link RecordFormat#AVRO} (technical) for data directly in Avro JSON format. The Avro schema in JSON format must be
 * supplied, and the values must match the schema and the specification for Avro JSON-serialized data
 * ((https://avro.apache.org/docs/1.8.1/spec.html#json_encoding).</li>
 * </ol>
 *
 * If the Avro format is used but not record data is specified, the dataset will be completed using random values that match the
 * schema.
 */
public class FixedDatasetProperties extends PropertiesImpl implements DatasetProperties<FixedDatastoreProperties> {

    public Property<RecordFormat> format = PropertyFactory.newEnum("format", RecordFormat.class).setRequired();

    public Property<RecordDelimiterType> recordDelimiter = PropertyFactory.newEnum("recordDelimiter", RecordDelimiterType.class)
            .setValue(RecordDelimiterType.LF);

    public Property<String> specificRecordDelimiter = PropertyFactory.newString("specificRecordDelimiter", "\\n");

    public Property<FieldDelimiterType> fieldDelimiter = PropertyFactory.newEnum("fieldDelimiter", FieldDelimiterType.class)
            .setValue(FieldDelimiterType.SEMICOLON);

    public Property<String> specificFieldDelimiter = PropertyFactory.newString("specificFieldDelimiter", ";");

    public Property<String> schema = PropertyFactory.newString("schema", "").setRequired();

    public Property<String> csvSchema = PropertyFactory.newString("csvSchema", "");

    public Property<String> values = PropertyFactory.newString("values", "");

    public final transient ReferenceProperties<FixedDatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            FixedDatastoreDefinition.NAME);

    public FixedDatasetProperties(String name) {
        super(name);
    }

    @Override
    public FixedDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(FixedDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        format.setValue(RecordFormat.CSV);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(format);
        mainForm.addRow(recordDelimiter);
        mainForm.addRow(specificRecordDelimiter);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(specificFieldDelimiter);
        mainForm.addRow(widget(schema).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "json"));
        mainForm.addRow(csvSchema);
        mainForm.addRow(widget(values).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "json"));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(recordDelimiter).setVisible(format.getValue() == RecordFormat.CSV);
            form.getWidget(specificRecordDelimiter).setVisible(
                    format.getValue() == RecordFormat.CSV && recordDelimiter.getValue().equals(RecordDelimiterType.OTHER));

            form.getWidget(fieldDelimiter).setVisible(format.getValue() == RecordFormat.CSV);
            form.getWidget(specificFieldDelimiter).setVisible(
                    format.getValue() == RecordFormat.CSV && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));

            form.getWidget(csvSchema).setVisible(format.getValue() == RecordFormat.CSV);
            form.getWidget(schema).setVisible(format.getValue() == RecordFormat.AVRO);
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

    /**
     * Possible record formats.
     */
    public enum RecordFormat {
        CSV,
        JSON,
        AVRO
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
