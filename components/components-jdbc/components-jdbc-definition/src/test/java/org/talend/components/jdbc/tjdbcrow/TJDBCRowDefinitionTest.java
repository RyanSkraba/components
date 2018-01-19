package org.talend.components.jdbc.tjdbcrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The class <code>TJDBCRowDefinitionTest</code> contains tests for the class <code>{@link TJDBCRowDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:14
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCRowDefinitionTest {

    /**
     * Run the TJDBCRowDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testTJDBCRowDefinition() throws Exception {
        TJDBCRowDefinition result = new TJDBCRowDefinition();

        assertEquals(true, result.isConditionalInputs());
        assertEquals("tJDBCRow", result.getName());
        assertEquals("tJDBCRow_icon32.png", result.getImagePath());
        assertEquals(true, result.isStartable());
        assertEquals(null, result.getIconKey());
        assertEquals(null, result.getPartitioning());
        assertEquals(false, result.isDataAutoPropagate());
        assertEquals(false, result.isSchemaAutoPropagate());
        assertEquals(false, result.isRejectAfterClose());
        assertEquals("JDBC Row", result.getDisplayName());
        assertEquals("JDBC Row", result.getTitle());
    }

    /**
     * Run the String[] getFamilies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testGetFamilies() throws Exception {
        TJDBCRowDefinition fixture = new TJDBCRowDefinition();

        String[] result = fixture.getFamilies();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Databases/DB_JDBC", result[0]);
    }

    /**
     * Run the Class<? extends ComponentProperties> getPropertyClass() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testGetPropertyClass() throws Exception {
        TJDBCRowDefinition fixture = new TJDBCRowDefinition();

        Class<? extends ComponentProperties> result = fixture.getPropertyClass();

        assertNotNull(result);
        assertEquals("org.talend.components.jdbc.tjdbcrow.TJDBCRowProperties", result.getCanonicalName());
    }

    /**
     * Run the Property[] getReturnProperties() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testGetReturnProperties() throws Exception {
        TJDBCRowDefinition fixture = new TJDBCRowDefinition();

        Property[] result = fixture.getReturnProperties();

        assertNotNull(result);
        assertEquals(2, result.length);

        assertNotNull(result[0]);
        assertEquals("Error Message", result[0].getDisplayName());
        assertEquals("Property: errorMessage", result[0].toString());
        assertEquals("errorMessage", result[0].getName());
        assertEquals(null, result[0].getValue());
        assertEquals("java.lang.String", result[0].getType());

        assertNotNull(result[1]);
        assertEquals("Query", result[1].getDisplayName());
        assertEquals("Property: query", result[1].toString());
        assertEquals("query", result[1].getName());
        assertEquals(null, result[1].getValue());
        assertEquals("java.lang.String", result[1].getType());
    }

    /**
     * Run the RuntimeInfo getRuntimeInfo(ExecutionEngine,ComponentProperties,ConnectorTopology) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testGetRuntimeInfo() throws Exception {
        AllSetting allSetting = Mockito.mock(AllSetting.class);
        Mockito.when(allSetting.getDriverClass()).thenReturn("anyDriverClass");
        TJDBCRowProperties properties = Mockito.mock(TJDBCRowProperties.class);
        Mockito.when(properties.getRuntimeSetting()).thenReturn(allSetting);
        
        TJDBCRowDefinition fixture = new TJDBCRowDefinition();
        ExecutionEngine engine = ExecutionEngine.DI;

        RuntimeInfo result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.OUTGOING);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.NONE);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        assertNotNull(result);
    }

    /**
     * Run the Set<ConnectorTopology> getSupportedConnectorTopologies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        TJDBCRowDefinition fixture = new TJDBCRowDefinition();

        Set<ConnectorTopology> result = fixture.getSupportedConnectorTopologies();

        assertNotNull(result);
        assertEquals(4, result.size());
    }

}