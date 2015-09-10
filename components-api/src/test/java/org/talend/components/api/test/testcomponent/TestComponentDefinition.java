package org.talend.components.api.test.testcomponent;

import org.springframework.stereotype.Component;
import org.talend.components.api.ComponentConnector;
import org.talend.components.api.ComponentConnector.Type;
import org.talend.components.api.Constants;
import org.talend.components.api.properties.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;

@Component(Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME)
public class TestComponentDefinition implements ComponentDefinition {

    public static final String        COMPONENT_NAME = "TestComponent"; //$NON-NLS-1$

    protected ComponentConnector[]    connectors     = { new ComponentConnector(Type.FLOW, 0, 0),
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

    @Override
    public String getPngImagePath() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

}
