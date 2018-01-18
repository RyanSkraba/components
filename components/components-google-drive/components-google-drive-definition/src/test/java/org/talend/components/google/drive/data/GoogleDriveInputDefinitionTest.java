package org.talend.components.google.drive.data;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveInputDefinitionTest {

    GoogleDriveInputDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new GoogleDriveInputDefinition();
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertEquals(GoogleDriveInputProperties.class, def.getPropertyClass());
    }

    @Test
    public void testGetReturnProperties() throws Exception {
        assertEquals(new Property[0], def.getReturnProperties());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        RuntimeInfo rt = def.getRuntimeInfo(ExecutionEngine.DI, null, null);
        assertNotNull(rt);
        assertEquals(GoogleDriveComponentDefinition.DATA_SOURCE_CLASS, rt.getRuntimeClassName());
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertEquals(EnumSet.of(ConnectorTopology.OUTGOING), def.getSupportedConnectorTopologies());
    }

    @Test
    public void testGetSupportedProducts() throws Exception {
        assertEquals(2, def.getSupportedProducts().size());
        assertThat(def.getSupportedProducts(), contains(SupportedProduct.DATAPREP, SupportedProduct.DATASTREAMS));
    }
}
