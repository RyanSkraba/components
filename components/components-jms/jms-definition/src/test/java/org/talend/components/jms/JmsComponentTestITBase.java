package org.talend.components.jms;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.jms.input.JmsInputDefinition;
import org.talend.components.jms.output.JmsOutputDefinition;

public abstract class JmsComponentTestITBase extends AbstractComponentTest {

    @Inject
    ComponentService componentService;

    @Override
    public ComponentService getComponentService() {
        return componentService;
    }

    @Test
    public void assertComponentsAreRegistered() {
        assertNotNull(getComponentService().getComponentDefinition(JmsInputDefinition.COMPONENT_NAME));
        assertNotNull(getComponentService().getComponentDefinition(JmsOutputDefinition.COMPONENT_NAME));
    }
}
