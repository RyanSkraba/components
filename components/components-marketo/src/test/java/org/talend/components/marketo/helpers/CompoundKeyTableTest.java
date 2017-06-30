package org.talend.components.marketo.helpers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class CompoundKeyTableTest {

    CompoundKeyTable ckt;

    @Before
    public void setUp() throws Exception {
        ckt = new CompoundKeyTable("test");
    }

    @Test
    public void testGetKeyValuesAsJson() throws Exception {
        ckt.keyName.setValue(Arrays.asList("customerId", "VIN"));
        ckt.keyValue.setValue(Arrays.asList("4137181", "WBA4R7C30HK896061"));// WBA4R7C55HK895912
        String result = "[{\"customerId\":\"4137181\",\"VIN\":\"WBA4R7C30HK896061\"}]";
        assertEquals(result, ckt.getKeyValuesAsJson().toString());
        ckt.keyName.setValue(Arrays.asList("customerId"));
        ckt.keyValue.setValue(Arrays.asList((String) null));
        result = "[{\"customerId\":\"\"}]";
        assertEquals(result, ckt.getKeyValuesAsJson().toString());
        ckt.keyName.setValue(null);
        ckt.keyValue.setValue(null);
        assertEquals("[]", ckt.getKeyValuesAsJson().toString());
        ckt.keyName.setValue(new ArrayList<String>());
        ckt.keyValue.setValue(null);
        assertEquals("[]", ckt.getKeyValuesAsJson().toString());
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(0, ckt.size());
        ckt.keyName.setValue(Arrays.asList("customerId", "VIN"));
        ckt.keyValue.setValue(Arrays.asList("4137181", "WBA4R7C30HK896061"));// WBA4R7C55HK895912
        assertEquals(2, ckt.size());
    }

}
