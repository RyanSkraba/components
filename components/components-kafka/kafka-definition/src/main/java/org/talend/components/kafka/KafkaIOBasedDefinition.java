package org.talend.components.kafka;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.daikon.properties.property.Property;

public abstract class KafkaIOBasedDefinition extends AbstractComponentDefinition {

    public KafkaIOBasedDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

}
