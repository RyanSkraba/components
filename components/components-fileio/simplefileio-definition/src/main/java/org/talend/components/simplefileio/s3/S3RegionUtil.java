package org.talend.components.simplefileio.s3;

public class S3RegionUtil {

  
    /**
     * Some region has special endpoint
     * 
     * @param region
     * @return
     */
    public static String regionToEndpoint(String region) {
        S3Region s3Region = fromString(region);
        switch (s3Region) {
            case GovCloud:
                return "s3-us-gov-west-1.amazonaws.com";
            case CN_NORTH_1:
                return "s3.cn-north-1.amazonaws.com.cn";
            case CN_NORTHWEST_1:
              return "s3.cn-northwest-1.amazonaws.com.cn";
            default:
              return String.format("s3.dualstack.%s.amazonaws.com", region);
        }
    }
    
    private static S3Region fromString(String region) {
        for (S3Region s3Region : S3Region.values()) {
            if (s3Region.getValue().equalsIgnoreCase(region)) {
                return s3Region;
            }
        }
        return S3Region.OTHER;
    }
    
    public static String getBucketRegionFromLocation(String bucketLocation) {
        if ("US".equals(bucketLocation)) { // refer to BucketLocationUnmarshaller
            return S3Region.US_EAST_1.getValue();
        } else if ("EU".equals(bucketLocation)) {
            return S3Region.EU_WEST_1.getValue();
        } else {
            return bucketLocation;
        }
    }
    
}
