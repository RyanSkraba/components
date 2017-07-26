package org.talend.components.jdbc.dataprep.di;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The class <code>TDataPrepDBInputDefinitionTest</code> contains tests for the class
 * <code>{@link TDataPrepDBInputDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM1:54
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TDataPrepDBInputDefinitionTest {

    /**
     * Run the String[] getFamilies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM1:54
     */
    @Test
    public void testGetFamilies() throws Exception {
        TDataPrepDBInputDefinition fixture = new TDataPrepDBInputDefinition();

        String[] result = fixture.getFamilies();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("DataPrep/DB", result[0]);
    }

    /**
     * Run the RuntimeInfo getRuntimeInfo(ExecutionEngine,ComponentProperties,ConnectorTopology) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM1:54
     */
    @Test
    public void testGetRuntimeInfo() throws Exception {
        AllSetting allSetting = Mockito.mock(AllSetting.class);
        Mockito.when(allSetting.getDriverClass()).thenReturn("anyDriverClass");
        TDataPrepDBInputProperties properties = Mockito.mock(TDataPrepDBInputProperties.class);
        Mockito.when(properties.getRuntimeSetting()).thenReturn(allSetting);
        
        TDataPrepDBInputDefinition fixture = new TDataPrepDBInputDefinition();
        ExecutionEngine engine = ExecutionEngine.DI;

        RuntimeInfo result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.OUTGOING);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.NONE);
        Assert.assertNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING);
        Assert.assertNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        Assert.assertNull(result);
    }

    /**
     * Run the Set<ConnectorTopology> getSupportedConnectorTopologies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM1:54
     */
    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        TDataPrepDBInputDefinition fixture = new TDataPrepDBInputDefinition();

        Set<ConnectorTopology> result = fixture.getSupportedConnectorTopologies();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Run the boolean isSchemaAutoPropagate() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM1:54
     */
    @Test
    public void testIsSchemaAutoPropagate() throws Exception {
        TDataPrepDBInputDefinition fixture = new TDataPrepDBInputDefinition();

        boolean result = fixture.isSchemaAutoPropagate();

        assertEquals(true, result);
    }

}