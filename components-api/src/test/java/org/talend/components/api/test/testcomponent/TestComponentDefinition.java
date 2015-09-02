package org.talend.components.api.test.testcomponent;

import org.springframework.stereotype.Component;
import org.talend.components.api.*;
import org.talend.components.api.ComponentConnector.Type;
import org.talend.components.api.ComponentDefinition;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.Constants;

@Component(Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME)
public class TestComponentDefinition extends ComponentDefinition {

    public static final String COMPONENT_NAME = "TestComponent"; //$NON-NLS-1$

    protected ComponentConnector[] connectors = { new ComponentConnector(Type.FLOW, 0, 0),
            new ComponentConnector(Type.ITERATE, 1, 0), new ComponentConnector(Type.SUBJOB_OK, 1, 0),
            new ComponentConnector(Type.SUBJOB_ERROR, 1, 0) };

    protected TestComponentProperties properties;

    @Override
    public ComponentProperties createProperties() {
        return new TestComponentProperties();
    }

    @Override
    public Family[] getSupportedFamilies() {
        return new Family[] { Family.BUSINESS, Family.CLOUD };
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

}
