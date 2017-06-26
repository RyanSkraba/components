package org.talend.components.jdbc.dataprep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * The class <code>JDBCInputPropertiesTest</code> contains tests for the class <code>{@link JDBCInputProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-23 AM11:58
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class JDBCInputPropertiesTest {

    /**
     * Run the JDBCInputProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 AM11:58
     */
    @Test
    public void testJDBCInputProperties() throws Exception {
        String name = "input";

        JDBCInputProperties result = new JDBCInputProperties(name);

        assertNotNull(result);
        assertEquals(null, result.getDatasetProperties());
        assertEquals("properties.input.displayName", result.getDisplayName());
    }

    /**
     * Run the Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 AM11:58
     */
    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        JDBCInputProperties fixture = new JDBCInputProperties("input");

        Set<PropertyPathConnector> result = fixture.getAllSchemaPropertiesConnectors(true);
        assertNotNull(result);
        assertEquals(1, result.size());

        result = fixture.getAllSchemaPropertiesConnectors(false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Run the JDBCDatasetProperties getDatasetProperties() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 AM11:58
     */
    @Test
    public void testGetDatasetProperties() throws Exception {
        JDBCInputProperties fixture = new JDBCInputProperties("input");
        fixture.init();

        fixture.setDatasetProperties(new JDBCDatasetProperties("dataset"));
        JDBCDatasetProperties result = fixture.getDatasetProperties();
        assertNotNull(result);
    }

    /**
     * Run the AllSetting getRuntimeSetting() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 AM11:58
     */
    @Test
    public void testGetRuntimeSetting() throws Exception {
        JDBCInputProperties fixture = new JDBCInputProperties("");

        JDBCDatasetProperties dataset = new JDBCDatasetProperties("dataset");
        JDBCDatastoreProperties datastore = new JDBCDatastoreProperties("datastore");
        datastore.init();
        dataset.setDatastoreProperties(datastore);
        fixture.setDatasetProperties(dataset);
        AllSetting result = fixture.getRuntimeSetting();
        assertNotNull(result);
    }

}