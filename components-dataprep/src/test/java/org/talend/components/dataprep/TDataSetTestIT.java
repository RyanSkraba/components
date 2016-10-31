package org.talend.components.dataprep;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.service.spring.SpringTestApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class TDataSetTestIT {

    @Inject
    private ComponentService componentService;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(componentService, errorCollector);
    }

    @Test
    public void testAllImagePath() {
        ComponentTestUtils.testAllImages(componentService);
    }

}
