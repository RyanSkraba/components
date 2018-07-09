package org.talend.components.simplefileio;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.daikon.serialize.SerializerDeserializer;

public class MigrationTest {

    private String oldSer;
  
    @Before
    public void before() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("s3dataset_remove_setheaderline_headerline.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[1024];
        int count = -1;
        while ((count = reader.read(buf)) != -1) {
            sb.append(buf, 0, count);
        }
        oldSer = sb.toString();
    }
  
    @Test
    public void testMigration() {
        SerializerDeserializer.Deserialized<S3DatasetProperties> deser = SerializerDeserializer.fromSerialized(oldSer,
            S3DatasetProperties.class, null, SerializerDeserializer.PERSISTENT);
        assertTrue("should be true, but not", deser.migrated);
        S3DatasetProperties properties = deser.object;
        boolean value = properties.setHeaderLine.getValue();
        assertTrue("should be false, but not", !value);
    }
}
