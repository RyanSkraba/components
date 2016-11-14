package org.talend.components.kafka.dataset;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaDatasetDefinition extends SimpleNamedThing implements DatasetDefinition<KafkaDatasetProperties> {

    public static final String NAME = "KafkaDataset";

    public KafkaDatasetDefinition() {
        super(NAME);
    }

    @Override
    public KafkaDatasetProperties createProperties() {
        KafkaDatasetProperties properties = new KafkaDatasetProperties("kafkaDataset");
        properties.init();
        return properties;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(KafkaDatasetProperties properties, Object ctx) {
        return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "kafka-runtime"),
                "org.talend.components.kafka.runtime.KafkaDatasetRuntime");
    }

    @Override
    public String getImagePath() {
        // FIXME add image
        return null;
    }
}
