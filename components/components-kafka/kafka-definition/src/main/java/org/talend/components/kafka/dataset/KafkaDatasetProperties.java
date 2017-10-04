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
package org.talend.components.kafka.dataset;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreDefinition;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.runtime.IKafkaDatasetRuntime;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class KafkaDatasetProperties extends PropertiesImpl implements DatasetProperties<KafkaDatastoreProperties> {

    public ReferenceProperties<KafkaDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            KafkaDatastoreDefinition.NAME);

    public Property<String> topic = PropertyFactory.newString("topic").setRequired();

    public SchemaProperties main = new SchemaProperties("main");

    public EnumProperty<ValueFormat> valueFormat = PropertyFactory.newEnum("valueFormat", ValueFormat.class);

    // Property for csv
    public Property<String> fieldDelimiter = PropertyFactory.newString("fieldDelimiter", ";");

    // Property for avro
    public Property<Boolean> isHierarchy = PropertyFactory.newBoolean("isHierarchy", false);

    public Property<String> avroSchema = PropertyFactory.newString("avroSchema");

    public KafkaDatasetProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(widget(topic).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        mainForm.addRow(valueFormat);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(isHierarchy);
        mainForm.addRow(widget(avroSchema).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "json"));
        mainForm.addRow(main.getForm(Form.MAIN));

    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        valueFormat.setValue(ValueFormat.CSV);
    }

    public void afterValueFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterIsHierarchy() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(fieldDelimiter).setVisible(valueFormat.getValue() == ValueFormat.CSV);
            form.getWidget(isHierarchy).setVisible(valueFormat.getValue() == ValueFormat.AVRO);
            form.getWidget(avroSchema).setVisible(valueFormat.getValue() == ValueFormat.AVRO && isHierarchy.getValue());
        }
    }

    public ValidationResult beforeTopic() {
        KafkaDatasetDefinition definition = new KafkaDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IKafkaDatasetRuntime runtime = (IKafkaDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            this.topic.setPossibleValues(new ArrayList(runtime.listTopic()));
            return ValidationResult.OK;
        } catch (Exception e) {
            return new ValidationResult(new ComponentException(e));
        }
    }

    @Override
    public KafkaDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(KafkaDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
    }

    public enum ValueFormat {
        CSV,
        AVRO
    }

}
