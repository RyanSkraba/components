package org.talend.components.jdbc.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.definition.DefinitionImageType;

/**
 * The class <code>JDBCConnectionWizardDefinitionTest</code> contains tests for the class
 * <code>{@link JDBCConnectionWizardDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM3:31
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class JDBCConnectionWizardDefinitionTest {

    /**
     * Run the ComponentWizard createWizard(String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testCreateWizard() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();
        ComponentWizard result = fixture.createWizard("");
        assertNotNull(result);
    }

    /**
     * Run the ComponentWizard createWizard(ComponentProperties, String) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testCreateWizard2() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();
        JDBCConnectionWizardProperties properties = new JDBCConnectionWizardProperties("wizard");
        ComponentWizard result = fixture.createWizard(properties, "");
        assertNotNull(result);
    }

    /**
     * Run the String getIconKey() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testGetIconKey() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();
        String result = fixture.getIconKey();
        assertEquals(null, result);
    }

    /**
     * Run the String getImagePath(DefinitionImageType) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testGetImagePath() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();

        String result = fixture.getImagePath(DefinitionImageType.TREE_ICON_16X16);
        assertEquals("connectionWizardIcon.png", result);
        result = fixture.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66);
        assertEquals("JDBCWizardBanner.png", result);
        result = fixture.getImagePath(DefinitionImageType.PALETTE_ICON_32X32);
        assertEquals(null, result);
    }

    /**
     * Run the String getName() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testGetName() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();
        String result = fixture.getName();
        assertEquals("JDBC", result);
    }

    /**
     * Run the String getPngImagePath(WizardImageType) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testGetPngImagePath_1() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();

        String result = fixture.getPngImagePath(WizardImageType.TREE_ICON_16X16);
        assertEquals("connectionWizardIcon.png", result);
        result = fixture.getPngImagePath(WizardImageType.WIZARD_BANNER_75X66);
        assertEquals("JDBCWizardBanner.png", result);
    }

    /**
     * Run the boolean isTopLevel() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testIsTopLevel() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();
        boolean result = fixture.isTopLevel();
        assertEquals(true, result);
    }

    /**
     * Run the boolean supportsProperties(Class<? extends ComponentProperties>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testSupportsProperties() throws Exception {
        JDBCConnectionWizardDefinition fixture = new JDBCConnectionWizardDefinition();
        Class<? extends ComponentProperties> propertiesClass = ComponentProperties.class;
        boolean result = fixture.supportsProperties(propertiesClass);
        assertTrue(result);
    }
}