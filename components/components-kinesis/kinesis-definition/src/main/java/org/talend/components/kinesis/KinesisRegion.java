// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.kinesis;

/**
 * http://docs.aws.amazon.com/general/latest/gr/rande.html#ak_region
 */
public enum KinesisRegion {

    DEFAULT("us-east-1"),
    AP_SOUTH_1("ap-south-1"),
    AP_SOUTHEAST_1("ap-southeast-1"),
    AP_SOUTHEAST_2("ap-southeast-2"),
    AP_NORTHEAST_1("ap-northeast-1"),
    AP_NORTHEAST_2("ap-northeast-2"),
    // http://docs.amazonaws.cn/en_us/general/latest/gr/rande.html#cnnorth_region
    CN_NORTH_1("cn-north-1"),
    EU_WEST_1("eu-west-1"),
    EU_WEST_2("eu-west-2"),
    EU_CENTRAL_1("eu-central-1"),
    // http://docs.aws.amazon.com/govcloud-us/latest/UserGuide/using-govcloud-endpoints.html
    GovCloud("us-gov-west-1"),
    CA_CENTRAL_1("ca-central-1"),
    SA_EAST_1("sa-east-1"),
    US_EAST_1("us-east-1"),
    US_EAST_2("us-east-2"),
    US_WEST_1("us-west-1"),
    US_WEST_2("us-west-2"),
    OTHER("us-east-1");

    private String value;

    private KinesisRegion(String value) {
        this.value = value;
    }

    public static KinesisRegion fromString(String region) {
        for (KinesisRegion kinesisRegion : KinesisRegion.values()) {
            if (kinesisRegion.value.equalsIgnoreCase(region)) {
                return kinesisRegion;
            }
        }
        return null;
    }

    // TODO copied from s3, may not used
    public static KinesisRegion fromLocation(String location) {
        if (location.equals("US")) { // refer to BucketLocationUnmarshaller
            return KinesisRegion.US_EAST_1;
        } else if (location.equals("EU")) {
            return KinesisRegion.EU_WEST_1;
        } else {
            return fromString(location); // do not need to convert
        }
    }

    // TODO copied from S3, may not used
    public String toEndpoint() {
        switch (this) {
        default:
            return String.format("kinesis.%s.amazonaws.com", value);
        }
    }

    public String getValue() {
        return this.value;
    }
}
