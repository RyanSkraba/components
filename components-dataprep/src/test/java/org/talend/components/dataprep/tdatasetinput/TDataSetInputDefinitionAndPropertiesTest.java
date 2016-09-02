package org.talend.components.dataprep.tdatasetinput;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SpringTestApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringTestApp.class)
public class TDataSetInputDefinitionAndPropertiesTest {

    @Inject
    private ComponentService componentService;

    @Test
    public void testTDataSetInputDefinition() {
        TDataSetInputDefinition inputDefinition = (TDataSetInputDefinition) componentService
                .getComponentDefinition("tDatasetInput");
        Assert.assertArrayEquals(new String[] { "Talend Data Preparation" }, inputDefinition.getFamilies());
        Assert.assertTrue(inputDefinition.isSchemaAutoPropagate());
    }

    @Test
    public void testTDataSetInputProperties() {
        TDataSetInputProperties properties = (TDataSetInputProperties) componentService.getComponentProperties("tDatasetInput");
        PropertyPathConnector connector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

        Assert.assertNotNull(properties.getSchema());
        Assert.assertEquals(Collections.singleton(connector), properties.getAllSchemaPropertiesConnectors(true));
        Assert.assertEquals(Collections.emptySet(), properties.getAllSchemaPropertiesConnectors(false));
    }
}