package org.talend.components.kinesis.runtime;

import com.amazonaws.regions.Regions;
import org.junit.Assert;
import org.junit.Test;

public class KinesisClientUtilsTest {

    @Test
    public void computeAwsRegionTest() {
        Regions region = KinesisClientUtils.computeAwsRegion("us-east-1", "");
        Assert.assertEquals(Regions.US_EAST_1, region);

        region = KinesisClientUtils.computeAwsRegion("other", "DOES_NOT_EXIST");
        Assert.assertEquals(Regions.DEFAULT_REGION, region);

        region = KinesisClientUtils.computeAwsRegion("other", "us-east-1");
        Assert.assertEquals(Regions.US_EAST_1, region);
    }

}
