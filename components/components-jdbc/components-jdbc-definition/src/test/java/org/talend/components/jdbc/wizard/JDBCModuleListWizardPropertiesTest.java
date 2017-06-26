package org.talend.components.jdbc.wizard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * The class <code>JDBCModuleListWizardPropertiesTest</code> contains tests for the class
 * <code>{@link JDBCModuleListWizardProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM3:31
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class JDBCModuleListWizardPropertiesTest {

    /**
     * Run the JDBCModuleListWizardProperties(String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testJDBCModuleListWizardProperties() throws Exception {
        String name = "wizard";

        JDBCModuleListWizardProperties result = new JDBCModuleListWizardProperties(name);

        assertEquals(null, result.getRuntimeSetting());
        // FIXME
        assertEquals("", result.getDisplayName());
        assertEquals(null, result.getName());
        assertEquals(null, result.getTitle());
    }

    /**
     * Run the AllSetting getRuntimeSetting() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testGetRuntimeSetting() throws Exception {
        JDBCModuleListWizardProperties fixture = new JDBCModuleListWizardProperties("wizard");
        AllSetting result = fixture.getRuntimeSetting();
        assertEquals(null, result);
    }

    /**
     * Run the JDBCModuleListWizardProperties setConnection(JDBCConnectionModule) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testSetConnection() throws Exception {
        JDBCModuleListWizardProperties fixture = new JDBCModuleListWizardProperties("wizard");
        assertEquals(fixture, fixture.setConnection(null));
    }

    /**
     * Run the JDBCModuleListWizardProperties setName(String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testSetName() throws Exception {
        JDBCModuleListWizardProperties fixture = new JDBCModuleListWizardProperties("wizard");
        assertEquals(fixture, fixture.setName("name"));
    }

    /**
     * Run the void setupLayout() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testSetupLayout() throws Exception {
        JDBCModuleListWizardProperties fixture = new JDBCModuleListWizardProperties("wizard");
        fixture.setupLayout();
    }
}