package org.talend.components.kafka.output;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.kafka.KafkaIOBasedDefinition;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaOutputDefinition extends KafkaIOBasedDefinition {

    public static String NAME = "KafkaOutput";

    public KafkaOutputDefinition() {
        super(NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return KafkaOutputProperties.class;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology connectorTopology) {
        return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "kafka-runtime"),
                "org.talend.components.kafka.runtime.KafkaOutputPTransformRuntime");
    }
}
