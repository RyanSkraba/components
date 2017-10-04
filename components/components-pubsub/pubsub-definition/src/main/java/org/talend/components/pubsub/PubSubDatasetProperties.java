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

package org.talend.components.pubsub;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.pubsub.runtime.IPubSubDatasetRuntime;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
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

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.List;

public class PubSubDatasetProperties extends PropertiesImpl implements DatasetProperties<PubSubDatastoreProperties> {

    public final ReferenceProperties<PubSubDatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            PubSubDatastoreDefinition.NAME);

    public Property<String> topic = PropertyFactory.newString("topic");

    public Property<String> subscription = PropertyFactory.newString("subscription");

    // public SchemaProperties main = new SchemaProperties("main");

    public EnumProperty<ValueFormat> valueFormat = PropertyFactory.newEnum("valueFormat", ValueFormat.class);

    // Property for csv
    public Property<String> fieldDelimiter = PropertyFactory.newString("fieldDelimiter", ";");

    // Property for avro
    public Property<String> avroSchema = PropertyFactory.newString("avroSchema");

    public PubSubDatasetProperties(String name) {
        super(name);
    }

    // public Property<Boolean> enableAttributes = PropertyFactory.newBoolean("enableAttributes");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(topic);
        mainForm.addRow(subscription);
        mainForm.addRow(valueFormat);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(widget(avroSchema).setWidgetType(Widget.CODE_WIDGET_TYPE)
                .setConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF, "json"));
        // mainForm.addRow(main.getForm(Form.MAIN));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        valueFormat.setValue(ValueFormat.CSV);
    }

    public void afterValueFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(fieldDelimiter).setVisible(valueFormat.getValue() == ValueFormat.CSV);
            form.getWidget(avroSchema).setVisible(valueFormat.getValue() == ValueFormat.AVRO);
        }
    }

    public ValidationResult beforeTopic() {
        PubSubDatasetDefinition definition = new PubSubDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IPubSubDatasetRuntime runtime = (IPubSubDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            List<NamedThing> topics = new ArrayList<>();
            for (String topicName : runtime.listTopics()) {
                topics.add(new SimpleNamedThing(topicName, topicName));
            }
            topic.setPossibleValues(topics);
            return ValidationResult.OK;
        } catch (Exception e) {
            return new ValidationResult(new ComponentException(e));
        }
    }

    public ValidationResult beforeSubscription() {
        PubSubDatasetDefinition definition = new PubSubDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IPubSubDatasetRuntime runtime = (IPubSubDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            List<NamedThing> topics = new ArrayList<>();
            for (String topicName : runtime.listSubscriptions()) {
                topics.add(new SimpleNamedThing(topicName, topicName));
            }
            subscription.setPossibleValues(topics);
            return ValidationResult.OK;
        } catch (Exception e) {
            return new ValidationResult(new ComponentException(e));
        }
    }

    @Override
    public PubSubDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(PubSubDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    public enum ValueFormat {
        CSV,
        AVRO
    }

}
