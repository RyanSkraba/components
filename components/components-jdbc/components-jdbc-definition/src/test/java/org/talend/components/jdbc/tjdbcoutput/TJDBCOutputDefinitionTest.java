package org.talend.components.jdbc.tjdbcoutput;

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
 * The class <code>TJDBCOutputDefinitionTest</code> contains tests for the class <code>{@link TJDBCOutputDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCOutputDefinitionTest {

    /**
     * Run the TJDBCOutputDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCOutputDefinition() throws Exception {
        TJDBCOutputDefinition result = new TJDBCOutputDefinition();

        assertEquals(true, result.isConditionalInputs());
        assertEquals("tJDBCOutput", result.getName());
        assertEquals("tJDBCOutput_icon32.png", result.getImagePath());
        assertEquals(false, result.isStartable());
        assertEquals(null, result.getIconKey());
        assertEquals(null, result.getPartitioning());
        assertEquals(false, result.isDataAutoPropagate());
        assertEquals(true, result.isSchemaAutoPropagate());
        assertEquals(false, result.isRejectAfterClose());
        assertEquals("JDBC Output", result.getDisplayName());
        assertEquals("JDBC Output", result.getTitle());
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
        TJDBCOutputDefinition fixture = new TJDBCOutputDefinition();

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
        TJDBCOutputDefinition fixture = new TJDBCOutputDefinition();

        Class<? extends ComponentProperties> result = fixture.getPropertyClass();

        assertNotNull(result);
        assertEquals("org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties", result.getCanonicalName());
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
        TJDBCOutputDefinition fixture = new TJDBCOutputDefinition();

        Property[] result = fixture.getReturnProperties();

        assertNotNull(result);
        assertEquals(7, result.length);
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
        assertEquals("Number of inserted", result[2].getDisplayName());
        assertEquals("Property: nbLineInserted", result[2].toString());
        assertEquals("nbLineInserted", result[2].getName());
        assertEquals(null, result[2].getValue());
        assertEquals("java.lang.Integer", result[2].getType());

        assertNotNull(result[3]);
        assertEquals("Number of updated", result[3].getDisplayName());
        assertEquals("Property: nbLineUpdated", result[3].toString());
        assertEquals("nbLineUpdated", result[3].getName());
        assertEquals(null, result[3].getValue());
        assertEquals("java.lang.Integer", result[3].getType());

        assertNotNull(result[4]);
        assertEquals("Number of deleted", result[4].getDisplayName());
        assertEquals("Property: nbLineDeleted", result[4].toString());
        assertEquals("nbLineDeleted", result[4].getName());
        assertEquals(null, result[4].getValue());
        assertEquals("java.lang.Integer", result[4].getType());

        assertNotNull(result[5]);
        assertEquals("Number of reject", result[5].getDisplayName());
        assertEquals("Property: nbLineRejected", result[5].toString());
        assertEquals("nbLineRejected", result[5].getName());
        assertEquals(null, result[5].getValue());
        assertEquals("java.lang.Integer", result[5].getType());

        assertNotNull(result[6]);
        assertEquals("Query", result[6].getDisplayName());
        assertEquals("Property: query", result[6].toString());
        assertEquals("query", result[6].getName());
        assertEquals(null, result[6].getValue());
        assertEquals("java.lang.String", result[6].getType());
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
        TJDBCOutputProperties properties = Mockito.mock(TJDBCOutputProperties.class);
        Mockito.when(properties.getRuntimeSetting()).thenReturn(allSetting);
        
        TJDBCOutputDefinition fixture = new TJDBCOutputDefinition();
        ExecutionEngine engine = ExecutionEngine.DI;

        RuntimeInfo result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        assertNotNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.NONE);
        assertNull(result);

        result = fixture.getRuntimeInfo(engine, properties, ConnectorTopology.OUTGOING);
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
        TJDBCOutputDefinition fixture = new TJDBCOutputDefinition();

        Set<ConnectorTopology> result = fixture.getSupportedConnectorTopologies();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

}