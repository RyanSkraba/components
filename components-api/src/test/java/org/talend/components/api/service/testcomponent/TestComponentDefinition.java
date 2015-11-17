package org.talend.components.api.service.testcomponent;

import java.io.InputStream;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentConnector.Type;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.runtime.ComponentRuntime;

import aQute.bnd.annotation.component.Component;

// @Component(Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME)
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TestComponentDefinition extends AbstractComponentDefinition implements ComponentDefinition {

    public static final String COMPONENT_NAME = "TestComponent"; //$NON-NLS-1$

    public TestComponentDefinition() {
        propertiesClass = TestComponentProperties.class;
        setConnectors(new ComponentConnector(Type.FLOW, 0, 0), new ComponentConnector(Type.ITERATE, 1, 0),
                new ComponentConnector(Type.SUBJOB_OK, 1, 0), new ComponentConnector(Type.SUBJOB_ERROR, 1, 0));
    }

    protected TestComponentProperties properties;

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
    public String getPngImagePath(ComponentImageType imageType) {
        return null;
    }

    @Override
    public InputStream getMavenPom() {
        return this.getClass().getResourceAsStream("pom.xml");
    }

}
