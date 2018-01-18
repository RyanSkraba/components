package org.talend.components.marklogic.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

public class MarkLogicDatasetDefinitionTest {

    private MarkLogicDatasetDefinition definition;

    @Before
    public void setup() {
        definition = new MarkLogicDatasetDefinition();
    }

    @Test
    public void testGetPropertiesClass() {
        Assert.assertEquals(MarkLogicDatasetProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testGetImagePath() {
        Assert.assertTrue(definition.getImagePath().startsWith(MarkLogicDatasetDefinition.NAME));
        Assert.assertTrue(
                definition.getImagePath(DefinitionImageType.PALETTE_ICON_32X32).startsWith(MarkLogicDatasetDefinition.NAME));
        Assert.assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }

    @Test
    public void testGetIconKey() {
        Assert.assertNull(definition.getIconKey());
    }

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtime = definition.getRuntimeInfo(null);
        Assert.assertEquals(MarkLogicDatasetDefinition.DATASET_RUNTIME, runtime.getRuntimeClassName());
    }
}
