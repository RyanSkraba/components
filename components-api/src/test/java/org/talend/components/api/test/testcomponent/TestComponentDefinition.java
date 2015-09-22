package org.talend.components.api.test.testcomponent;

import org.springframework.stereotype.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.properties.AbstractComponentDefinition;
import org.talend.components.api.properties.ComponentConnector;
import org.talend.components.api.properties.ComponentConnector.Type;
import org.talend.components.api.properties.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;

@Component(Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME)
public class TestComponentDefinition extends AbstractComponentDefinition implements ComponentDefinition {

    public static final String COMPONENT_NAME = "TestComponent"; //$NON-NLS-1$

    public TestComponentDefinition() {
        setConnectors(new ComponentConnector(Type.FLOW, 0, 0), new ComponentConnector(Type.ITERATE, 1, 0),
                new ComponentConnector(Type.SUBJOB_OK, 1, 0), new ComponentConnector(Type.SUBJOB_ERROR, 1, 0));
    }

    protected TestComponentProperties properties;

    @Override
    public ComponentProperties createProperties() {
        return new TestComponentProperties(globalContext.i18nMessageProvider);
    }

    @Override
    public ComponentRuntime createRuntime() {
        return null;
    }

    @Override
    public String[] getSupportedFamilies() {
        return new String[] { ComponentDefinition.FAMILY_BUSINESS, ComponentDefinition.FAMILY_CLOUD };
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
    protected String getI18NBaseName() {
        return "org.talend.components.api.test.testcomponent.testMessage"; //$NON-NLS-1$
    }

}
