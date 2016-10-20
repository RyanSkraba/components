package org.talend.components.dataprep.migration;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.dataprep.runtime.DataPrepOutputModes;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.daikon.serialize.SerializerDeserializer;

public class MigrateTest {

    private String oldSer;

    @Before
    public void before() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("old_tdatasetoutputproperties.json");
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
    public void testfromSerialized() {
        SerializerDeserializer.Deserialized<TDataSetOutputProperties> deser = SerializerDeserializer.fromSerialized(oldSer,
                TDataSetOutputProperties.class, null, SerializerDeserializer.PERSISTENT);
        assertTrue("should be true, but not", deser.migrated);
        TDataSetOutputProperties properties = deser.object;
        List<?> values = properties.mode.getPossibleValues();
        assertTrue("should not be null or empty", values != null && !values.isEmpty());
        assertTrue("miss the new item in the closed list", values.contains(DataPrepOutputModes.CreateOrUpdate));
    }

}
