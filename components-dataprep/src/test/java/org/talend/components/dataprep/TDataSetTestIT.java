package org.talend.components.dataprep;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SpringApp;

import javax.inject.Inject;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class TDataSetTestIT {
    @Inject
    private ComponentService componentService;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testTDataSetInputDefinition() {
        TDataSetInputDefinition inputDefinition =
                (TDataSetInputDefinition) componentService.getComponentDefinition("tDatasetInput");
        Assert.assertArrayEquals(new String[] {"Talend Data Preparation"}, inputDefinition.getFamilies());
    }

    @Test
    public void testTDataSetInputProperties() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.
                getComponentProperties("tDatasetInput");
        PropertyPathConnector connector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

        Assert.assertNotNull(properties.getSchema());
        Assert.assertEquals(Collections.singleton(connector), properties.getAllSchemaPropertiesConnectors(true));
    }

    @Test
    public void testTDataSetOutputDefinition() {
        TDataSetOutputDefinition outputDefinition =
                (TDataSetOutputDefinition) componentService.getComponentDefinition("tDatasetOutput");
        Assert.assertArrayEquals(new String[] {"Talend Data Preparation"}, outputDefinition.getFamilies());
        Assert.assertEquals("org.talend.components", outputDefinition.getMavenGroupId());
        Assert.assertEquals("components-dataprep", outputDefinition.getMavenArtifactId());
    }

    @Test
    public void testTDataSetOutputProperties() {
        TDataSetOutputProperties properties = (TDataSetOutputProperties) componentService.
                getComponentProperties("tDatasetOutput");
        PropertyPathConnector connector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

        Assert.assertNotNull(properties.getSchema());
        Assert.assertEquals(Collections.singleton(connector), properties.getAllSchemaPropertiesConnectors(true));
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
