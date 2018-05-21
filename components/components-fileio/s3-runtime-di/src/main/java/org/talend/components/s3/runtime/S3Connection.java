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

package org.talend.components.s3.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.components.simplefileio.s3.S3RegionUtil;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;

public class S3Connection {

    public static AmazonS3 createClient(S3OutputProperties properties) {
        S3DatasetProperties data_set = properties.getDatasetProperties();
        S3DatastoreProperties data_store = properties.getDatasetProperties().getDatastoreProperties();

        com.amazonaws.auth.AWSCredentials credentials = new com.amazonaws.auth.BasicAWSCredentials(
                data_store.accessKey.getValue(), data_store.secretKey.getValue());

        Boolean clientSideEnc = data_set.encryptDataInMotion.getValue();

        AmazonS3 conn = null;
        if (clientSideEnc != null && clientSideEnc) {
            //the code below is not called now
            String kms_cmk = data_set.kmsForDataInMotion.getValue();
            KMSEncryptionMaterialsProvider encryptionMaterialsProvider = new KMSEncryptionMaterialsProvider(kms_cmk);
            conn = new AmazonS3EncryptionClient(credentials, encryptionMaterialsProvider,
                    new CryptoConfiguration()/*.withAwsKmsRegion(region)*/);
        } else {
            AWSCredentialsProvider basicCredentialsProvider = new StaticCredentialsProvider(credentials);
            conn = new AmazonS3Client(basicCredentialsProvider);
        }

        //get the correct endpoint by the default region for current bucket : us-east-1
        String endpoint = getEndpoint(conn, data_set.bucket.getValue());
        conn.setEndpoint(endpoint);

        return conn;
    }
    
    //get the correct endpoint
    private static String getEndpoint(AmazonS3 s3client, String bucket) {
        String bucketLocation = null;
        try { 
            bucketLocation = s3client.getBucketLocation(bucket);
        } catch(IllegalArgumentException e) {
            //java.lang.IllegalArgumentException: Cannot create enum from eu-west-2 value!
            String info = e.getMessage();
            if(info == null || info.isEmpty()) {
                throw e;
            }
            Pattern regex = Pattern.compile("[a-zA-Z]+-[a-zA-Z]+-[1-9]");
            Matcher matcher = regex.matcher(info);
            if(matcher.find()) {
                bucketLocation = matcher.group(0);
            } else {
                throw e;
            }
        }
        String region = S3RegionUtil.getBucketRegionFromLocation(bucketLocation);
        return S3RegionUtil.regionToEndpoint(region);
    }

}
