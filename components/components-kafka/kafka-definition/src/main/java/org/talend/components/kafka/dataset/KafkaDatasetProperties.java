// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.talend.daikon.properties.presentation.Widget.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreDefinition;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.runtime.IKafkaDatasetRuntime;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class KafkaDatasetProperties extends PropertiesImpl implements DatasetProperties<KafkaDatastoreProperties> {

    public transient ReferenceProperties<KafkaDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            KafkaDatastoreDefinition.NAME);

    public Property<String> topic = PropertyFactory.newString("topic");

    public SchemaProperties main = new SchemaProperties("main");

    public KafkaDatasetProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(widget(topic).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        mainForm.addRow(main.getForm(Form.MAIN));

    }

    @Override
    public void setupProperties() {
        Schema schema = SchemaBuilder.record("row").namespace("kafka").fields() //
                .name("key").type(Schema.create(Schema.Type.BYTES)).noDefault() //
                .name("value").type(Schema.create(Schema.Type.BYTES)).noDefault() //
                .endRecord();
        main.schema.setValue(schema);
        setDatastoreProperties((KafkaDatastoreProperties) new KafkaDatastoreProperties("").init());
    }

    public ValidationResult beforeTopic() {
        KafkaDatasetDefinition definition = new KafkaDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this, null);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IKafkaDatasetRuntime runtime = (IKafkaDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            List<NamedThing> topics = new ArrayList<>();
            for (String topic : runtime.listTopic()) {
                topics.add(new SimpleNamedThing(topic, topic));
            }
            this.topic.setPossibleValues(topics);
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

}
