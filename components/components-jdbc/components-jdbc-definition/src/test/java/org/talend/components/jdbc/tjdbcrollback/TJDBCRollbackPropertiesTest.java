package org.talend.components.jdbc.tjdbcrollback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * The class <code>TJDBCRollbackPropertiesTest</code> contains tests for the class <code>{@link TJDBCRollbackProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCRollbackPropertiesTest {

    /**
     * Run the TJDBCRollbackProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCRollbackProperties() throws Exception {
        String name = "rollback";

        TJDBCRollbackProperties result = new TJDBCRollbackProperties(name);

        assertEquals("properties.rollback.displayName", result.getDisplayName());
        assertEquals(name, result.getName());
        assertEquals(name, result.getTitle());
    }

    /**
     * Run the AllSetting getRuntimeSetting() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testGetRuntimeSetting() throws Exception {
        TJDBCRollbackProperties fixture = new TJDBCRollbackProperties("");
        AllSetting result = fixture.getRuntimeSetting();
        assertNotNull(result);
    }

    /**
     * Run the void setupLayout() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testSetupLayout() throws Exception {
        TJDBCRollbackProperties fixture = new TJDBCRollbackProperties("");
        fixture.setupLayout();
    }

}