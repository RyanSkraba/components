package org.talend.components.salesforce.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.serialize.SerializerDeserializer;

public class SalesforceConnectionMigration {

    public String getSerializedStr(String fileName) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[1024];
        int count = -1;
        while ((count = reader.read(buf)) != -1) {
            sb.append(buf, 0, count);
        }
        reader.close();
        return sb.toString();
    }

    @Test
    public void testSalesforceConnectionPropertiesMigration() throws IOException {
        SerializerDeserializer.Deserialized<SalesforceConnectionProperties> deser = SerializerDeserializer.fromSerialized(
                getSerializedStr("tSalesforceConnectionProperties_621.json"), SalesforceConnectionProperties.class, null,
                SerializerDeserializer.PERSISTENT);
        assertTrue("should be true, but not", deser.migrated);
        SalesforceConnectionProperties properties = deser.object;
        String apiVersion = properties.apiVersion.getValue();
        assertEquals("\"34.0\"", apiVersion);
    }

    @Test
    public void testSalesforceInputPropertiesMigration() throws IOException {
        SerializerDeserializer.Deserialized<TSalesforceInputProperties> deser = SerializerDeserializer.fromSerialized(
                getSerializedStr("tSalesforceInputProperties_621.json"), TSalesforceInputProperties.class, null,
                SerializerDeserializer.PERSISTENT);
        assertTrue("should be true, but not", deser.migrated);
        TSalesforceInputProperties properties = deser.object;
        String apiVersion = properties.connection.apiVersion.getValue();
        assertEquals("\"34.0\"", apiVersion);
    }
}