package org.talend.components.multiruntime;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentRegistry;
import org.talend.components.api.service.common.ComponentServiceImpl;

@SuppressWarnings("nls")
public class MultiRuntimeComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test.
    public ComponentService getComponentService() {
        if (componentService == null) {
            ComponentRegistry testComponentRegistry = new ComponentRegistry();
            testComponentRegistry.registerDefinition(Arrays.asList(new MultiRuntimeComponentDefinition()));
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

}
