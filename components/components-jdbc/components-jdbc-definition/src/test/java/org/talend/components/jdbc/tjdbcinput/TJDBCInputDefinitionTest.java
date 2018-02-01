package org.talend.components.jdbc.tjdbcinput;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The class <code>TJDBCInputDefinitionTest</code> contains tests for the class
 * <code>{@link TJDBCInputDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCInputDefinitionTest {

    /**
     * Run the TJDBCInputDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Ignore
    @Test
    public void testTJDBCInputDefinition() throws Exception {
        TJDBCInputDefinition result = new TJDBCInputDefinition();

        assertEquals(true, result.isSchemaAutoPropagate());
        assertEquals("tJDBCInput", result.getName());
        assertEquals("tJDBCInput_icon32.png", result.getImagePath());
        assertEquals(true, result.isStartable());
        assertEquals(null, result.getIconKey());
        assertEquals(null, result.getPartitioning());
        assertEquals(false, result.isConditionalInputs());
        assertEquals(false, result.isDataAutoPropagate());
        assertEquals(false, result.isRejectAfterClose());
        assertEquals("JDBC Input", result.getDisplayName());
        assertEquals("JDBC Input", result.getTitle());
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
        TJDBCInputDefinition fixture = new TJDBCInputDefinition();

        String[] result = fixture.getFamilies();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Databases/DB Specifics/JDBC", result[0]);
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
        TJDBCInputDefinition fixture = new TJDBCInputDefinition();

        Class<? extends ComponentProperties> result = fixture.getPropertyClass();

        assertNotNull(result);
        assertEquals("org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties", result.getCanonicalName());
    }

    /**
     * Run the Property[] getReturnProperties() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Ignore
    @Test
    public void testGetReturnProperties() throws Exception {
        TJDBCInputDefinition fixture = new TJDBCInputDefinition();

        Property[] result = fixture.getReturnProperties();

        assertNotNull(result);
        assertEquals(3, result.length);
        assertNotNull(result[0]);
        assertEquals("Error Message", result[0].getDisplayName());
        assertEquals("Property: errorMessage", result[0].toString());
        assertEquals("errorMessage", result[0].getName());
        assertEquals(null, result[0].getValue());
        assertEquals("java.lang.String", result[0].getType());

        assertNotNull(result[1]);
        assertEquals("Number of line", result[1].getDisplayName());
        assertEquals("Property: totalRecordCount", result[1].toString());
        assertEquals("totalRecordCount", result[1].getName());
        assertEquals(null, result[1].getValue());
        assertEquals("java.lang.Integer", result[1].getType());

        assertNotNull(result[2]);
        assertEquals("Query", result[2].getDisplayName());
        assertEquals("Property: query", result[2].toString());
        assertEquals("query", result[2].getName());
        assertEquals(null, result[2].getValue());
        assertEquals("java.lang.String", result[2].getType());
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
        TJDBCInputProperties properties = Mockito.mock(TJDBCInputProperties.class);
        Mockito.when(properties.getRuntimeSetting()).thenReturn(allSetting);

        TJDBCInputDefinition fixture = new TJDBCInputDefinition();
        ExecutionEngine engine = ExecutionEngine.DI;

        RuntimeInfo result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.OUTGOING);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING);
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
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        TJDBCInputDefinition fixture = new TJDBCInputDefinition();

        Set<ConnectorTopology> result = fixture.getSupportedConnectorTopologies();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Run the boolean isSchemaAutoPropagate() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testIsSchemaAutoPropagate() throws Exception {
        TJDBCInputDefinition fixture = new TJDBCInputDefinition();

        boolean result = fixture.isSchemaAutoPropagate();

        assertEquals(true, result);
    }

}
