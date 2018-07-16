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

package org.talend.components.kinesis.runtime;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import org.apache.beam.sdk.io.kinesis.TalendKinesisProvider;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.KinesisDatastoreProperties;

public class KinesisClient {

    public static TalendKinesisProvider getProvider(KinesisDatastoreProperties datastore) {
        return new TalendKinesisProvider(datastore.specifyCredentials.getValue(), datastore.accessKey.getValue(),
                datastore.secretKey.getValue(), datastore.specifyEndpoint.getValue(), datastore.endpoint.getValue(),
                Regions.DEFAULT_REGION, datastore.specifySTS.getValue(), datastore.roleArn.getValue(),
                datastore.roleSessionName.getValue(), datastore.specifyRoleExternalId.getValue(),
                datastore.roleExternalId.getValue(), datastore.specifySTSEndpoint.getValue(),
                datastore.stsEndpoint.getValue());
    }

    public static AmazonKinesis create(KinesisDatastoreProperties datastore) {
        return getProvider(datastore).getKinesisClient();
    }

    public static TalendKinesisProvider getProvider(KinesisDatasetProperties dataset) {
        KinesisDatastoreProperties datastore = dataset.getDatastoreProperties();
        Regions awsRegion = KinesisClientUtils.computeAwsRegion(dataset.region.getValue().getValue(), dataset.unknownRegion.getValue());
        return new TalendKinesisProvider(datastore.specifyCredentials.getValue(), datastore.accessKey.getValue(),
                datastore.secretKey.getValue(), datastore.specifyEndpoint.getValue(), datastore.endpoint.getValue(),
                awsRegion, datastore.specifySTS.getValue(), datastore.roleArn.getValue(),
                datastore.roleSessionName.getValue(), datastore.specifyRoleExternalId.getValue(),
                datastore.roleExternalId.getValue(), datastore.specifySTSEndpoint.getValue(),
                datastore.stsEndpoint.getValue());
    }

    public static AmazonKinesis create(KinesisDatasetProperties dataset) {
        return getProvider(dataset).getKinesisClient();
    }
}
