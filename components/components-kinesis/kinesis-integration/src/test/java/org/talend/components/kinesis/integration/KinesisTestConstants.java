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

package org.talend.components.kinesis.integration;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.KinesisDatastoreProperties;
import org.talend.components.kinesis.KinesisRegion;

public class KinesisTestConstants {

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

}
