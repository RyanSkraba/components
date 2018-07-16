package org.talend.components.kinesis.runtime;

import com.amazonaws.regions.Regions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.kinesis.KinesisRegion;

public class KinesisClientUtils {

    private static Logger logger = LoggerFactory.getLogger(KinesisClientUtils.class);

    public static Regions computeAwsRegion(String region, String unknownRegion) {
        Regions awsRegion = Regions.DEFAULT_REGION;
        if (KinesisRegion.OTHER.getValue().equals(region)) {
            try {
                awsRegion = Regions.fromName(unknownRegion);
            } catch (IllegalArgumentException e) {
                logger.warn("The region '" + unknownRegion + "' doesn't exist. Fallback to the default region ('" + Regions.DEFAULT_REGION.getName() + "')");
            }
        } else awsRegion = Regions.fromName(region);

        return awsRegion;
    }
}
