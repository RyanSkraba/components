package org.talend.components.filedelimited;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SpringTestApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class SpringFileInputTestIT extends AbstractComponentTest{
    @Inject
    private ComponentService componentService;

    public ComponentService getComponentService(){
        return componentService;
    }
    
    @Ignore
    @Test
    // this is an integration test to check that the dependencies file is properly generated.
    public void testDependencies() {
        ComponentTestUtils.testAllDesignDependenciesPresent(componentService, errorCollector);
    }

    
    
}
