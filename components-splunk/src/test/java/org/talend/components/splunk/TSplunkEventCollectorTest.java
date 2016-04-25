package org.talend.components.splunk;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.SimpleComponentRegistry;

@SuppressWarnings("nls")
public class TSplunkEventCollectorTest {

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
            testComponentRegistry.addComponent(TSplunkEventCollectorDefinition.COMPONENT_NAME, new TSplunkEventCollectorDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Ignore
    @Test
    public void testtSplunkEventCollectorRuntime() throws Exception {
    }

    @Ignore("To revisit.  The spec should be validated by the time it gets to the runtime.")
    @Test
    public void testtSplunkEventCollectorRuntimeException() {
    }

}
