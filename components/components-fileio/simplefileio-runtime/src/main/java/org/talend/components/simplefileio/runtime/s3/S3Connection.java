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

package org.talend.components.simplefileio.runtime.s3;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.components.simplefileio.s3.S3RegionUtil;

import com.talend.shaded.com.amazonaws.auth.AWSCredentialsProviderChain;
import com.talend.shaded.com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.talend.shaded.com.amazonaws.services.s3.AmazonS3;
import com.talend.shaded.com.amazonaws.services.s3.AmazonS3Client;
import com.talend.shaded.org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider;
import com.talend.shaded.org.apache.hadoop.fs.s3a.BasicAWSCredentialsProvider;
import com.talend.shaded.org.apache.hadoop.fs.s3a.Constants;
import com.talend.shaded.org.apache.hadoop.fs.s3a.S3AEncryptionMethods;
import com.talend.shaded.org.apache.hadoop.fs.s3a.S3AFileSystem;

public class S3Connection {

    public static AmazonS3 createClient(S3DatastoreProperties properties) {
        AWSCredentialsProviderChain credentials;
        if (properties.specifyCredentials.getValue()) {
            credentials = new AWSCredentialsProviderChain(new BasicAWSCredentialsProvider(properties.accessKey.getValue(),
                    properties.secretKey.getValue()), new DefaultAWSCredentialsProviderChain(),
                    new AnonymousAWSCredentialsProvider());
        } else {
            // do not be polluted by hidden accessKey/secretKey
            credentials = new AWSCredentialsProviderChain(new DefaultAWSCredentialsProviderChain(),
                    new AnonymousAWSCredentialsProvider());
        }
        AmazonS3 conn = new AmazonS3Client(credentials);
        return conn;
    }
    
    //TODO only use for test, should move it to test package
    public static S3AFileSystem createFileSystem(S3DatasetProperties properties) throws IOException {
        Configuration config = new Configuration(true);
        ExtraHadoopConfiguration extraConfig = new ExtraHadoopConfiguration();
        S3Connection.setS3Configuration(extraConfig, properties);
        extraConfig.addTo(config);
        return (S3AFileSystem) FileSystem.get(new Path(Constants.FS_S3A + "://" + properties.bucket.getValue()).toUri(), config);
    }

    public static String getUriPath(S3DatasetProperties properties, String path) {
        // Construct the path using the s3a schema.
        return Constants.FS_S3A + "://" + properties.bucket.getValue() + "/" + path;
    }

    public static String getUriPath(S3DatasetProperties properties) {
        return getUriPath(properties, properties.object.getValue());
    }

    public static void setS3Configuration(ExtraHadoopConfiguration conf, S3DatastoreProperties properties) {
        // Never reuse a filesystem created through this object.
        conf.set(String.format("fs.%s.impl.disable.cache", Constants.FS_S3A), "true");
        if (properties.specifyCredentials.getValue()) {
            // do not be polluted by hidden accessKey/secretKey
            conf.set(Constants.ACCESS_KEY, properties.accessKey.getValue());
            conf.set(Constants.SECRET_KEY, properties.secretKey.getValue());
        }
    }

    public static void setS3Configuration(ExtraHadoopConfiguration conf, S3DatasetProperties properties) {
        String endpoint = getEndpoint(properties);
        conf.set(Constants.ENDPOINT, endpoint);
        //need to it?
        //conf.set("fs.s3a.experimental.input.fadvise", "random");
      
        if (properties.encryptDataAtRest.getValue()) {
            conf.set(Constants.SERVER_SIDE_ENCRYPTION_ALGORITHM, S3AEncryptionMethods.SSE_KMS.getMethod());
            conf.set(Constants.SERVER_SIDE_ENCRYPTION_KEY, properties.kmsForDataAtRest.getValue());
        }
        if (properties.encryptDataInMotion.getValue()) {
            // TODO: these don't exist yet...
            conf.set("fs.s3a.client-side-encryption-algorithm", S3AEncryptionMethods.SSE_KMS.getMethod());
            conf.set("fs.s3a.client-side-encryption-key", properties.kmsForDataInMotion.getValue());
        }
        setS3Configuration(conf, properties.getDatastoreProperties());
    }
    
    //get the correct endpoint
    public static String getEndpoint(S3DatasetProperties properties) {
        String bucket = properties.bucket.getValue();
        S3DatastoreProperties datastore = properties.getDatastoreProperties();
        AmazonS3 s3client = S3Connection.createClient(datastore);
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
