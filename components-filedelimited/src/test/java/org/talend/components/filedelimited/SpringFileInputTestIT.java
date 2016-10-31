package org.talend.components.filedelimited;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.service.spring.SpringTestApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class SpringFileInputTestIT extends AbstractComponentTest {

    @Inject
    private ComponentService componentService;

    public ComponentService getComponentService() {
        return componentService;
    }

}
