package org.talend.components.multiruntime;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.SimpleComponentRegistry;

@SuppressWarnings("nls")
public class MultiRuntimeComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test.
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();
            testComponentRegistry.addComponent(MultiRuntimeComponentDefinition.COMPONENT_NAME,
                    new MultiRuntimeComponentDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

}
