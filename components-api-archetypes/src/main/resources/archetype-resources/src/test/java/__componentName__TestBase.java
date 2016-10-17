package ${package};

import javax.inject.Inject;

import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;

public class ${componentName}TestBase extends AbstractComponentTest {
    @Inject
    private ComponentService componentService;

    public ComponentService getComponentService(){
        return componentService;
    }
    
    @Test
    public void componentHasBeenRegistered(){
        assertComponentIsRegistered("${componentName}");
    }
}
