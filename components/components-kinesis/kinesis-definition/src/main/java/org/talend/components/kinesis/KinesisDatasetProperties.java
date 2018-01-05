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

package org.talend.components.kinesis;

import java.util.ArrayList;

import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.kinesis.runtime.IKinesisDatasetRuntime;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class KinesisDatasetProperties extends PropertiesImpl implements DatasetProperties<KinesisDatastoreProperties> {

    public final ReferenceProperties<KinesisDatastoreProperties> datastoreRef =
            new ReferenceProperties<>("datastoreRef", KinesisDatastoreDefinition.NAME);

    public Property<KinesisRegion> region =
            PropertyFactory.newEnum("region", KinesisRegion.class).setValue(KinesisRegion.DEFAULT).setRequired();

    public Property<String> unknownRegion =
            PropertyFactory.newString("unknownRegion", KinesisRegion.DEFAULT.getValue()).setRequired();

    public Property<String> streamName = PropertyFactory.newString("streamName").setRequired();

    public Property<ValueFormat> valueFormat =
            PropertyFactory.newEnum("valueFormat", ValueFormat.class).setValue(ValueFormat.CSV).setRequired();

    // Property for csv
    public Property<FieldDelimiterType> fieldDelimiter =
            PropertyFactory.newEnum("fieldDelimiter", FieldDelimiterType.class).setValue(FieldDelimiterType.SEMICOLON);

    public Property<String> specificFieldDelimiter = PropertyFactory.newString("specificFieldDelimiter", ";");

    // Property for avro
    public Property<String> avroSchema = PropertyFactory.newString("avroSchema");

    public KinesisDatasetProperties(String name) {
        super(name);
    }

    @Override
    public KinesisDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(KinesisDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);

        mainForm.addRow(region);
        mainForm.addRow(unknownRegion);
        mainForm.addRow(Widget.widget(streamName).setWidgetType(Widget.DATALIST_WIDGET_TYPE));

        mainForm.addRow(valueFormat);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(specificFieldDelimiter);
        mainForm.addRow(Widget.widget(avroSchema).setWidgetType(Widget.CODE_WIDGET_TYPE).setConfigurationValue(
                Widget.CODE_SYNTAX_WIDGET_CONF, "json"));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(unknownRegion).setVisible(KinesisRegion.OTHER.equals(region.getValue()));
            form.getWidget(fieldDelimiter).setVisible(ValueFormat.CSV.equals(valueFormat.getValue()));
            fieldDelimiter.setRequired(ValueFormat.CSV.equals(valueFormat.getValue()));
            form.getWidget(specificFieldDelimiter).setVisible(ValueFormat.CSV.equals(valueFormat.getValue())
                    && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));
            specificFieldDelimiter.setRequired(ValueFormat.CSV.equals(valueFormat.getValue())
                    && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));
            form.getWidget(avroSchema).setVisible(ValueFormat.AVRO.equals(valueFormat.getValue()));
            avroSchema.setRequired(ValueFormat.AVRO.equals(valueFormat.getValue()));
        }
    }

    public void afterRegion() {
        refreshLayout(getForm(Form.MAIN));
        KinesisDatasetDefinition definition = new KinesisDatasetDefinition();
        RuntimeInfo ri = definition.getRuntimeInfo(this);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {
            IKinesisDatasetRuntime runtime = (IKinesisDatasetRuntime) si.getInstance();
            runtime.initialize(null, this);
            this.streamName.setPossibleValues(new ArrayList<String>(runtime.listStreams()));
        } catch (Exception e) {
            TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
        }
    }

    public void afterValueFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterFieldDelimiter() {
        refreshLayout(getForm(Form.MAIN));
    }

    public String getFieldDelimiter() {
        if (FieldDelimiterType.OTHER.equals(fieldDelimiter.getValue())) {
            return specificFieldDelimiter.getValue();
        } else {
            return fieldDelimiter.getValue().getDelimiter();
        }
    }


    public enum ValueFormat {
        CSV,
        AVRO
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
