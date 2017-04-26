package org.talend.components.snowflake.tsnowflakeclose;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TSnowflakeClosePropertiesTest {

    TSnowflakeCloseProperties closeProperties;

    @Before
    public void reset() {
        closeProperties = new TSnowflakeCloseProperties("close");
    }

    @Test
    public void testGetReferencedComponentId() {
        String expectedStringValue;
        String referencedComponentId;

        expectedStringValue = "SomeStringValue";
        closeProperties.referencedComponent.componentInstanceId.setValue(expectedStringValue);
        referencedComponentId = closeProperties.getReferencedComponentId();

        assertEquals(referencedComponentId, expectedStringValue);
    }

}
