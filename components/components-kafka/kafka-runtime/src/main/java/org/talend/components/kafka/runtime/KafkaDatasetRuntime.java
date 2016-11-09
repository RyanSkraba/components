package org.talend.components.kafka.runtime;

import java.util.Set;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaDatasetRuntime implements IKafkaDatasetRuntime {

    private KafkaDatasetProperties dataset;

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaDatasetProperties properties) {
        this.dataset = properties;
        return ValidationResult.OK;
    }

    @Override
    public Set<String> listTopic() {
        return KafkaConnection.createConsumer(dataset.getDatastoreProperties()).listTopics().keySet();
    }
}
