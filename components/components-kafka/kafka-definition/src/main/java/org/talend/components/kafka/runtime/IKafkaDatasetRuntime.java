package org.talend.components.kafka.runtime;

import java.util.Set;

import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;

// FIXME should we have a DatasetRuntime which only implements RuntimableRuntime?
public interface IKafkaDatasetRuntime extends RuntimableRuntime<KafkaDatasetProperties> {

    public Set<String> listTopic();
}
