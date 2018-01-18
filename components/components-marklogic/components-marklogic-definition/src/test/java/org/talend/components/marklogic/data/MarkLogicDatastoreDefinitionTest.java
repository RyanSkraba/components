package org.talend.components.marklogic.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputDefinition;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputDefinition;
import org.talend.daikon.definition.DefinitionImageType;

public class MarkLogicDatastoreDefinitionTest {

    private MarkLogicDatastoreDefinition definition;

    @Before
    public void setup() {
        definition = new MarkLogicDatastoreDefinition();
    }

    @Test
    public void testCreateDatasetProperties() {
        MarkLogicConnectionProperties datastoreProperties = new MarkLogicConnectionProperties("datastore");
        DatasetProperties<MarkLogicConnectionProperties> dataset = definition.createDatasetProperties(datastoreProperties);

        Assert.assertEquals(datastoreProperties, dataset.getDatastoreProperties());
    }

    @Test
    public void testGetRuntimeInfo() {
        Assert.assertEquals(MarkLogicDatastoreDefinition.DATASTORE_RUNTIME,
                definition.getRuntimeInfo(null).getRuntimeClassName());
    }

    @Test
    public void getInputCompDefinition() {
        Assert.assertEquals(MarkLogicInputDefinition.COMPONENT_NAME, definition.getInputCompDefinitionName());
    }

    @Test
    public void getOutputCompDefinition() {
        Assert.assertEquals(MarkLogicOutputDefinition.COMPONENT_NAME, definition.getOutputCompDefinitionName());
    }

    @Test
    public void testGetPropertiesClass() {
        Assert.assertEquals(MarkLogicConnectionProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testGetImagePath() {
        Assert.assertTrue(definition.getImagePath().startsWith(MarkLogicDatastoreDefinition.COMPONENT_NAME));
        Assert.assertTrue(
                definition.getImagePath(DefinitionImageType.PALETTE_ICON_32X32).startsWith(MarkLogicDatastoreDefinition.COMPONENT_NAME));
        Assert.assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }

    @Test
    public void testGetIconKey() {
        Assert.assertNull(definition.getIconKey());
    }
}
