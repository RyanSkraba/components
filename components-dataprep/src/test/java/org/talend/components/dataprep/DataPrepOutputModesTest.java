package org.talend.components.dataprep;

import org.junit.Assert;
import org.junit.Test;

public class DataPrepOutputModesTest {

    @Test
    public void testToString() {
        Assert.assertEquals("create", DataPrepOutputModes.CREATE.toString());
        Assert.assertEquals("update", DataPrepOutputModes.UPDATE.toString());
        Assert.assertEquals("create&update", DataPrepOutputModes.CREATEANDUPDATE.toString());
        Assert.assertEquals("livedataset", DataPrepOutputModes.LIVEDATASET.toString());
    }

}