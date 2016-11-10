package org.talend.components.jms.runtime_1_1;

import org.talend.components.jms.JmsDatasetProperties;
import org.talend.components.jms.JmsMessageType;
import org.talend.components.jms.JmsProcessingMode;

public class JmsDatasetRuntime { //implements DatasetRuntime{

    private JmsDatasetProperties properties;

    private JmsMessageType msgType;

    private JmsProcessingMode processingMode;

    /*
    @Override public Schema getEndpointSchema(RuntimeContainer container) throws IOException {
        return null;
    }

    @Override public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (JmsDatasetProperties) properties;
        return ValidationResult.OK;
    }*/
}
