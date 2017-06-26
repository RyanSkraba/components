package org.talend.components.jdbc.tjdbcconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The class <code>TJDBCConnectionDefinitionTest</code> contains tests for the class
 * <code>{@link TJDBCConnectionDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:16
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCConnectionDefinitionTest {

    /**
     * Run the TJDBCConnectionDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testTJDBCConnectionDefinition() throws Exception {
        TJDBCConnectionDefinition result = new TJDBCConnectionDefinition();

        assertEquals(true, result.isStartable());
        assertEquals("tJDBCConnectionNew", result.getName());
        assertEquals("tJDBCConnectionNew_icon32.png", result.getImagePath());
        assertEquals(null, result.getIconKey());
        assertEquals(null, result.getPartitioning());
        assertEquals(false, result.isConditionalInputs());
        assertEquals(false, result.isDataAutoPropagate());
        assertEquals(false, result.isSchemaAutoPropagate());
        assertEquals(false, result.isRejectAfterClose());
        assertEquals("JDBC Connection", result.getDisplayName());
        assertEquals("JDBC Connection", result.getTitle());
    }

    /**
     * Run the String[] getFamilies() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testGetFamilies() throws Exception {
        TJDBCConnectionDefinition fixture = new TJDBCConnectionDefinition();

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
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testGetPropertyClass() throws Exception {
        TJDBCConnectionDefinition fixture = new TJDBCConnectionDefinition();

        Class<? extends ComponentProperties> result = fixture.getPropertyClass();

        assertNotNull(result);
        assertEquals("org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties", result.getCanonicalName());
    }

    /**
     * Run the Property[] getReturnProperties() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testGetReturnProperties() throws Exception {
        TJDBCConnectionDefinition fixture = new TJDBCConnectionDefinition();

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
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testGetRuntimeInfo() throws Exception {
        TJDBCConnectionDefinition fixture = new TJDBCConnectionDefinition();
        ExecutionEngine engine = ExecutionEngine.DI;
        ComponentProperties properties = new TJDBCConnectionProperties("");

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
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        TJDBCConnectionDefinition fixture = new TJDBCConnectionDefinition();

        Set<ConnectorTopology> result = fixture.getSupportedConnectorTopologies();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Run the boolean isStartable() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:16
     */
    @Test
    public void testIsStartable() throws Exception {
        TJDBCConnectionDefinition fixture = new TJDBCConnectionDefinition();

        boolean result = fixture.isStartable();

        assertEquals(true, result);
    }

}