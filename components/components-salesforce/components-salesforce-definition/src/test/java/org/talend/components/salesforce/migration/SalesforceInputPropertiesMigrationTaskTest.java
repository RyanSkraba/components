package org.talend.components.salesforce.migration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.talend.components.salesforce.TestUtils;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer.Deserialized;

public class SalesforceInputPropertiesMigrationTaskTest {

    @Test
    public void testSafetySwitch() throws IOException {
        //Loading old SalesforceInput properties and check if value was initialized correctly.
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_701.json"),
                TSalesforceInputProperties.class);

        TSalesforceInputProperties properties = deser.object;
        assertTrue(properties.safetySwitch.getValue());
    }
}
