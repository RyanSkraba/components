package org.talend.components.jdbc.tjdbcclose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
 * The class <code>TJDBCCloseDefinitionTest</code> contains tests for the class <code>{@link TJDBCCloseDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCCloseDefinitionTest {

    /**
     * Run the TJDBCCloseDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCCloseDefinition() throws Exception {
        TJDBCCloseDefinition result = new TJDBCCloseDefinition();

        assertEquals(true, result.isStartable());
        assertEquals("tJDBCClose", result.getName());
        assertEquals("tJDBCClose_icon32.png", result.getImagePath());
        assertEquals(null, result.getIconKey());
        assertEquals(null, result.getPartitioning());
        assertEquals(false, result.isConditionalInputs());
        assertEquals(false, result.isDataAutoPropagate());
        assertEquals(false, result.isSchemaAutoPropagate());
        assertEquals(false, result.isRejectAfterClose());
        assertEquals("JDBC Close", result.getDisplayName());
        assertEquals("JDBC Close", result.getTitle());
    }

    /**
     * Run the String[] getFamilies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetFamilies() throws Exception {
        TJDBCCloseDefinition fixture = new TJDBCCloseDefinition();

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
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetPropertyClass() throws Exception {
        TJDBCCloseDefinition fixture = new TJDBCCloseDefinition();

        Class<? extends ComponentProperties> result = fixture.getPropertyClass();

        assertNotNull(result);
        assertEquals("org.talend.components.jdbc.tjdbcclose.TJDBCCloseProperties", result.getCanonicalName());
    }

    /**
     * Run the Property[] getReturnProperties() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetReturnProperties() throws Exception {
        TJDBCCloseDefinition fixture = new TJDBCCloseDefinition();

        Property[] result = fixture.getReturnProperties();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertNotNull(result[0]);
        assertEquals("Error Message", result[0].getDisplayName());
        assertEquals("Property: errorMessage", result[0].toString());
        assertEquals("errorMessage", result[0].getName());
        assertEquals(null, result[0].getValue());
        assertEquals("java.lang.String", result[0].getType());
    }

    /**
     * Run the RuntimeInfo getRuntimeInfo(ExecutionEngine,ComponentProperties,ConnectorTopology) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetRuntimeInfo() throws Exception {
        AllSetting allSetting = Mockito.mock(AllSetting.class);
        Mockito.when(allSetting.getDriverClass()).thenReturn("anyDriverClass");
        TJDBCCloseProperties properties = Mockito.mock(TJDBCCloseProperties.class);
        Mockito.when(properties.getRuntimeSetting()).thenReturn(allSetting);
        
        TJDBCCloseDefinition fixture = new TJDBCCloseDefinition();
        ExecutionEngine engine = ExecutionEngine.DI;

        RuntimeInfo result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.NONE);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING);
        assertNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.OUTGOING);
        assertNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        assertNull(result);
    }

    /**
     * Run the Set<ConnectorTopology> getSupportedConnectorTopologies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        TJDBCCloseDefinition fixture = new TJDBCCloseDefinition();

        Set<ConnectorTopology> result = fixture.getSupportedConnectorTopologies();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Run the boolean isStartable() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testIsStartable() throws Exception {
        TJDBCCloseDefinition fixture = new TJDBCCloseDefinition();

        boolean result = fixture.isStartable();

        assertEquals(true, result);
    }

}