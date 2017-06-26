package org.talend.components.jdbc.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The class <code>JDBCDatasetDefinitionTest</code> contains tests for the class <code>{@link JDBCDatasetDefinition}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM2:33
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class JDBCDatasetDefinitionTest {
    /**
     * Run the JDBCDatasetDefinition() constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM2:33
     */
    @Test
    public void testJDBCDatasetDefinition()
        throws Exception {
        JDBCDatasetDefinition result = new JDBCDatasetDefinition();

        assertEquals("JDBCDataset_icon32.png", result.getImagePath());
        assertEquals("JDBCDataset_icon32.png", result.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertEquals(null, result.getImagePath(DefinitionImageType.SVG_ICON));
        assertEquals(null, result.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertEquals(null, result.getIconKey());
        assertEquals("Database", result.getDisplayName());
        assertEquals("Database", result.getTitle());
        assertEquals("JDBCDataset/Database/Database", result.toString());
        assertEquals("JDBCDataset", result.getName());
    }

    /**
     * Run the RuntimeInfo getRuntimeInfo(JDBCDatasetProperties) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM2:33
     */
    @Test
    public void testGetRuntimeInfo()
        throws Exception {
        JDBCDatasetDefinition fixture = new JDBCDatasetDefinition();
        JDBCDatasetProperties properties = new JDBCDatasetProperties("dataset");

        RuntimeInfo result = fixture.getRuntimeInfo(properties);
        assertNotNull(result);
    }
}