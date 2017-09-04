package org.talend.components.salesforce.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.TestUtils;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer.Deserialized;

public class SalesforceConnectionMigrationTest {

    @Test
    public void testSalesforceConnectionPropertiesMigration() throws IOException {

        Deserialized<SalesforceConnectionProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceConnectionProperties_621.json"),
                SalesforceConnectionProperties.class);

        assertTrue("the property should be migrated, the migration returned false instead of true", deser.migrated);
        SalesforceConnectionProperties properties = deser.object;
        String apiVersion = properties.apiVersion.getValue();
        assertEquals("\"34.0\"", apiVersion);
    }

    @Test
    public void testSalesforceInputPropertiesMigration() throws IOException {
        Deserialized<TSalesforceInputProperties> deser = Properties.Helper.fromSerializedPersistent(
                TestUtils.getResourceAsString(getClass(), "tSalesforceInputProperties_621.json"),
                TSalesforceInputProperties.class);

        assertTrue("the property should be migrated, the migration returned false instead of true", deser.migrated);
        TSalesforceInputProperties properties = deser.object;
        String apiVersion = properties.connection.apiVersion.getValue();
        assertEquals("\"34.0\"", apiVersion);
    }
}