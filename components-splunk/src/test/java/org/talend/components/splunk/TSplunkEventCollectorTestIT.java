package org.talend.components.splunk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SpringApp;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class TSplunkEventCollectorTestIT extends TSplunkEventCollectorTestBase {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testGetProps() {
        ComponentProperties props = new TSplunkEventCollectorDefinition().createProperties();
        Form f = props.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        Form advancedF = props.getForm(Form.ADVANCED);
        System.out.println(f);
        System.out.println(advancedF);
        System.out.println(props);
        assertEquals(Form.MAIN, f.getName());
        assertEquals(Form.ADVANCED, advancedF.getName());
    }

    @Test
    public void testChangeExtendedOutput() {
        TSplunkEventCollectorProperties props = (TSplunkEventCollectorProperties) new TSplunkEventCollectorDefinition()
                .createProperties();
        // Test returned batch size on extended output true.
        props.extendedOutput.setValue(true);
        props.eventsBatchSize.setValue(100);
        assertEquals("Events batch size returned by the method should be 100", 100, props.getBatchSize());
        // Test returned batch size on extended output false.
        props.extendedOutput.setValue(false);
        assertEquals("Events batch size returned by the method should be 1", 1, props.getBatchSize());
    }

    @Test
    public void testAfterExtendedOutput() throws Throwable {
        ComponentProperties props;

        props = new TSplunkEventCollectorDefinition().createProperties();
        ComponentTestUtils.checkSerialize(props, errorCollector);
        Property extendedOutput = (Property) props.getProperty("extendedOutput");
        assertEquals(true, extendedOutput.getValue());
        Form advForm = props.getForm(Form.ADVANCED);
        assertTrue(advForm.getWidget("eventsBatchSize").isVisible());

        extendedOutput.setValue(false);
        props = checkAndAfter(advForm, "extendedOutput", props);
        advForm = props.getForm(Form.ADVANCED);
        assertTrue(advForm.isRefreshUI());

        assertFalse(advForm.getWidget("eventsBatchSize").isVisible());
    }

    @Test
    // this is an integration test to check that the dependencies file is properly generated.
    public void testDependencies() {
        ComponentTestUtils.testAllDesignDependenciesPresent(getComponentService(), errorCollector);
    }

    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(getComponentService(), errorCollector);
    }

    @Test
    public void testAllImagePath() {
        ComponentTestUtils.testAllImages(getComponentService());
    }

    @Test
    public void testAllRuntimes() {
        ComponentTestUtils.testAllRuntimeAvaialble(getComponentService());
    }

}
