package org.talend.components.jdbc.dataprep.di;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;

/**
 * The class <code>TDataPrepDBInputPropertiesTest</code> contains tests for the class
 * <code>{@link TDataPrepDBInputProperties}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM1:55
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class TDataPrepDBInputPropertiesTest {

    /**
     * Run the Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM1:55
     */
    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        TDataPrepDBInputProperties fixture = new TDataPrepDBInputProperties("input");

        Set<PropertyPathConnector> result = fixture.getAllSchemaPropertiesConnectors(true);
        assertNotNull(result);
        assertEquals(1, result.size());

        result = fixture.getAllSchemaPropertiesConnectors(false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

}