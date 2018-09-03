package org.talend.components.jdbc.datastore;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The class <code>JDBCDatastoreDefinitionTest</code> contains tests for the class
 * <code>{@link JDBCDatastoreDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM2:14
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class JDBCDatastoreDefinitionTest {

    /**
     * Run the JDBCDatastoreDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM2:14
     */
    @Ignore
    @Test
    public void testJDBCDatastoreDefinition() throws Exception {
        JDBCDatastoreDefinition result = new JDBCDatastoreDefinition();

        assertEquals("JDBCDatastore_icon32.png", result.getImagePath());
        assertEquals("JDBCDatastore_icon32.png", result.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertEquals(null, result.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertEquals(null, result.getImagePath(DefinitionImageType.SVG_ICON));
        assertEquals(null, result.getOutputCompDefinitionName());
        assertEquals("DataPrepDBInput", result.getInputCompDefinitionName());
        assertEquals("db-input", result.getIconKey());
        assertEquals("Database", result.getDisplayName());
        assertEquals("JDBC Connection", result.getTitle());
        assertEquals("JDBCDatastore/Database/JDBC Connection", result.toString());
        assertEquals("JDBCDatastore", result.getName());
    }

    /**
     * Run the DatasetProperties createDatasetProperties(JDBCDatastoreProperties) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM2:14
     */
    @Test
    public void testCreateDatasetProperties() throws Exception {
        JDBCDatastoreDefinition fixture = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties storeProp = new JDBCDatastoreProperties("datastore");

        DatasetProperties result = fixture.createDatasetProperties(storeProp);
        assertNotNull(result);
        assertEquals("properties.dataset.displayName", result.getDisplayName());
        assertEquals("dataset", result.getName());
        assertEquals("dataset", result.getTitle());
    }

    /**
     * Run the RuntimeInfo getRuntimeInfo(JDBCDatastoreProperties) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM2:14
     */
    @Test
    public void testGetRuntimeInfo() throws Exception {
        AllSetting allSetting = Mockito.mock(AllSetting.class);
        Mockito.when(allSetting.getDriverClass()).thenReturn("anyDriverClass");
        JDBCDatastoreProperties properties = Mockito.mock(JDBCDatastoreProperties.class);
        Mockito.when(properties.getRuntimeSetting()).thenReturn(allSetting);

        JDBCDatastoreDefinition fixture = new JDBCDatastoreDefinition();

        RuntimeInfo result = fixture.getRuntimeInfo(properties);
        assertNotNull(result);
    }

}
