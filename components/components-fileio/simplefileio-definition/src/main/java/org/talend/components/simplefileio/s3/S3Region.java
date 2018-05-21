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

package org.talend.components.simplefileio.s3;

/**
 * http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region
 */
public enum S3Region {
    DEFAULT("us-east-1"),
    AP_SOUTH_1("ap-south-1"),
    AP_SOUTHEAST_1("ap-southeast-1"),
    AP_SOUTHEAST_2("ap-southeast-2"),
    AP_NORTHEAST_1("ap-northeast-1"),
    AP_NORTHEAST_2("ap-northeast-2"),
    //TODO need to create a new s3 account for this region, not the common account, ignore it now
    //AP_NORTHEAST_3("ap-northeast-3"),
    // http://docs.amazonaws.cn/en_us/general/latest/gr/rande.html#cnnorth_region
    CN_NORTH_1("cn-north-1"),
    //this region's action is the same with cn-north-1
    CN_NORTHWEST_1("cn-northwest-1"),
    EU_WEST_1("eu-west-1"),
    EU_WEST_2("eu-west-2"),
    EU_WEST_3("eu-west-3"),
    EU_CENTRAL_1("eu-central-1"),
    // http://docs.aws.amazon.com/govcloud-us/latest/UserGuide/using-govcloud-endpoints.html
    GovCloud("us-gov-west-1"),
    CA_CENTRAL_1("ca-central-1"),
    SA_EAST_1("sa-east-1"),
    US_EAST_1("us-east-1"),
    US_EAST_2("us-east-2"),
    US_WEST_1("us-west-1"),
    US_WEST_2("us-west-2"),
    OTHER("other-region");

    private String value;

    private S3Region(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
