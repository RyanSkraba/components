package org.talend.components.dataprep;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SpringApp;
import org.talend.daikon.properties.presentation.Form;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class TDataSetTestIT {
    @Inject
    private ComponentService componentService;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testTDataSetInputProperties() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.
                getComponentProperties("tDatasetInput");
        Form f = properties.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(properties, errorCollector);
        System.out.println(f);
        System.out.println(properties);
        assertEquals(Form.MAIN, f.getName());
    }

    @Test
    public void testTDataSetOutputProperties() {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) componentService.
                getComponentProperties("tDatasetOutput");
        Form form = properties.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(properties, errorCollector);
        assertEquals(Form.MAIN, form.getName());
        assertEquals("Default limit should be 100", "100", properties.limit.getStringValue());
    }

    @Test
    // this is an integration test to check that the dependencies file is properly generated.
    public void testDependencies() {
        ComponentTestUtils.testAllDesignDependenciesPresent(componentService, errorCollector);
    }

    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(componentService, errorCollector);
    }

    @Test
    public void testAllImagePath() {
        ComponentTestUtils.testAllImages(componentService);
    }

    @Test
    public void testAllRuntimes() {
        ComponentTestUtils.testAllRuntimeAvaialble(componentService);
    }

}
