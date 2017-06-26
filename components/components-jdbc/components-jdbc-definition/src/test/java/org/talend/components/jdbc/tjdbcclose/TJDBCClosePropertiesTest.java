package org.talend.components.jdbc.tjdbcclose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * The class <code>TJDBCClosePropertiesTest</code> contains tests for the class <code>{@link TJDBCCloseProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:14
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCClosePropertiesTest {

    /**
     * Run the TJDBCCloseProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testTJDBCCloseProperties() throws Exception {
        String name = "close";

        TJDBCCloseProperties result = new TJDBCCloseProperties(name);

        assertEquals("properties.close.displayName", result.getDisplayName());
        assertEquals(name, result.getName());
        assertEquals(name, result.getTitle());
    }

    /**
     * Run the AllSetting getRuntimeSetting() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testGetRuntimeSetting() throws Exception {
        TJDBCCloseProperties fixture = new TJDBCCloseProperties("");
        AllSetting result = fixture.getRuntimeSetting();
        assertNotNull(result);
    }

    /**
     * Run the void setupLayout() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:14
     */
    @Test
    public void testSetupLayout() throws Exception {
        TJDBCCloseProperties fixture = new TJDBCCloseProperties("");
        fixture.setupLayout();
    }

}