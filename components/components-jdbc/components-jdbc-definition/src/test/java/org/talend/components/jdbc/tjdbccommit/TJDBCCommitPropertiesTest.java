package org.talend.components.jdbc.tjdbccommit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * The class <code>TJDBCCommitPropertiesTest</code> contains tests for the class <code>{@link TJDBCCommitProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-20 PM3:13
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TJDBCCommitPropertiesTest {

    /**
     * Run the TJDBCCommitProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-20 PM3:13
     */
    @Test
    public void testTJDBCCommitProperties() throws Exception {
        String name = "commit";

        TJDBCCommitProperties result = new TJDBCCommitProperties(name);

        assertEquals("properties.commit.displayName", result.getDisplayName());
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
        TJDBCCommitProperties fixture = new TJDBCCommitProperties("");
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
        TJDBCCommitProperties fixture = new TJDBCCommitProperties("");
        fixture.setupLayout();
    }

}