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

import org.apache.beam.sdk.repackaged.org.apache.commons.lang3.StringUtils;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.KinesisDatastoreProperties;
import org.talend.components.kinesis.KinesisRegion;
import org.talend.components.kinesis.input.KinesisInputProperties;

public class KinesisTestConstants {

    private static final String LOCALSTACK_KINESIS_ENDPOINT;

    static {
        String localstackKinesisEndpoint = System.getProperty("localstack.kinesis.endpoint");
        LOCALSTACK_KINESIS_ENDPOINT =
                localstackKinesisEndpoint != null ? localstackKinesisEndpoint : "http://localhost:4568";
    }

    // for localstack test
    public static KinesisDatastoreProperties getLocalDatastore() {
        KinesisDatastoreProperties datastore = new KinesisDatastoreProperties("kinesisDatastore");
        datastore.init();
        datastore.specifyCredentials.setValue(true);
        datastore.accessKey.setValue("test");
        datastore.secretKey.setValue("test");
        datastore.specifyEndpoint.setValue(true);
        datastore.endpoint.setValue(LOCALSTACK_KINESIS_ENDPOINT);
        return datastore;
    }

    public static KinesisDatastoreProperties getDatastore() {
        KinesisDatastoreProperties datastore = new KinesisDatastoreProperties("kinesisDatastore");
        datastore.init();
        String awsAccessKey = System.getProperty("aws.accesskey");
        String awsSecretKey = System.getProperty("aws.secretkey");
        if (StringUtils.isEmpty(awsAccessKey) || StringUtils.isEmpty(awsSecretKey)) {
            datastore.specifyCredentials.setValue(false);
        } else {
            datastore.accessKey.setValue(awsAccessKey);
            datastore.secretKey.setValue(awsSecretKey);
        }
        return datastore;
    }

    public static KinesisDatastoreProperties getDatastoreForRoleAssume() {
        KinesisDatastoreProperties datastore = new KinesisDatastoreProperties("kinesisDatastore");
        datastore.init();
        datastore.specifySTS.setValue(true);
        String awsAccessKey = System.getProperty("aws.sts.role.accesskey");
        String awsSecretKey = System.getProperty("aws.sts.role.secretkey");
        if (StringUtils.isEmpty(awsAccessKey) || StringUtils.isEmpty(awsSecretKey)) {
            datastore.specifyCredentials.setValue(false);
        } else {
            datastore.accessKey.setValue(awsAccessKey);
            datastore.secretKey.setValue(awsSecretKey);
        }
        String roleArn = System.getProperty("aws.sts.role.arn");
        String roleSessionName = System.getProperty("aws.sts.role.sessionname");
        String roleExternalId = System.getProperty("aws.sts.role.externalid");
        datastore.roleArn.setValue(roleArn);
        datastore.roleSessionName.setValue(roleSessionName);
        if (!StringUtils.isEmpty(roleExternalId)) {
            datastore.specifyRoleExternalId.setValue(true);
            datastore.roleExternalId.setValue(roleExternalId);
        }
        return datastore;
    }

    public static KinesisDatasetProperties getDatasetForListStreams(KinesisDatastoreProperties datastore,
            KinesisRegion region, String customRegion) {
        KinesisDatasetProperties dataset = new KinesisDatasetProperties("kinesisDataset");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        dataset.region.setValue(region);
        if (KinesisRegion.OTHER.equals(region)) {
            dataset.unknownRegion.setValue(customRegion);
        }
        return dataset;
    }

    public static KinesisDatasetProperties getDatasetForCsv(KinesisDatastoreProperties datastore, String streamName,
            KinesisDatasetProperties.FieldDelimiterType fieldDelimiter) {
        KinesisDatasetProperties dataset = new KinesisDatasetProperties("kinesisDataset");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        dataset.streamName.setValue(streamName);
        dataset.valueFormat.setValue(KinesisDatasetProperties.ValueFormat.CSV);
        dataset.fieldDelimiter.setValue(fieldDelimiter);
        return dataset;
    }

    public static KinesisDatasetProperties getDatasetForAvro(KinesisDatastoreProperties datastore, String streamName,
            String avroSchema) {
        KinesisDatasetProperties dataset = new KinesisDatasetProperties("kinesisDataset");
        dataset.init();
        dataset.setDatastoreProperties(datastore);
        dataset.streamName.setValue(streamName);
        dataset.valueFormat.setValue(KinesisDatasetProperties.ValueFormat.AVRO);
        dataset.avroSchema.setValue(avroSchema);
        return dataset;
    }

    public static KinesisInputProperties getInputFromBeginning(KinesisDatasetProperties dataset, Long maxReadTime,
            Integer maxNumRecords) {
        KinesisInputProperties input = new KinesisInputProperties("kinesisInput");
        input.init();
        input.setDatasetProperties(dataset);
        input.position.setValue(KinesisInputProperties.OffsetType.EARLIEST);
        if (maxReadTime != null) {
            input.useMaxReadTime.setValue(true);
            input.maxReadTime.setValue(maxReadTime);
        }
        if (maxNumRecords != null) {
            input.useMaxNumRecords.setValue(true);
            input.maxNumRecords.setValue(maxNumRecords);
        }
        return input;
    }

}
