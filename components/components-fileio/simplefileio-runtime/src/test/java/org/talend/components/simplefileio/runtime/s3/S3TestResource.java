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

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.components.simplefileio.s3.S3Region;

import com.talend.shaded.com.amazonaws.services.s3.model.ObjectMetadata;
import com.talend.shaded.org.apache.hadoop.fs.s3a.S3AFileSystem;

/**
 * Reusable for creating S3 properties preconfigured from environment variables for integration tests.
 *
 * <pre>
 * <profile>
 *   <id>amazon_credentials</id>
 *   <properties>
 *     <s3.accesskey>ACCESS_KEY</s3.accesskey>
 *     <s3.secretkey>SECRETY_KEY</s3.secretkey>
 *     <s3.region>EU_WEST_1</s3.region>
 *     <s3.bucket>testbucket</s3.bucket>
 *     <s3.ssekmskey>KEY1</s3.ssekmskey>
 *     <s3.csekmskey>KEY2</s3.csekmskey>
 *   </properties>
 *   <activation>
 *     <activeByDefault>true</activeByDefault>
 *   </activation>
 * </profile>
 * </pre>
 */
public class S3TestResource extends ExternalResource {

    /** The currently running test. */
    protected String name = null;

    /** The output path used for the currently running test. */
    protected String path = null;

    private String bucketName;

    private S3TestResource() {
        bucketName = System.getProperty("s3.bucket");
    }

    public static S3TestResource of() {
        return new S3TestResource();
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * @return An S3DatastoreProperties with all of the defaults, but filled with security credentials from the system
     * environment.
     */
    public S3DatastoreProperties createS3DatastoreProperties() {
        S3DatastoreProperties properties = new S3DatastoreProperties(null);
        properties.init();
        String s3AccessKey = System.getProperty("s3.accesskey");
        String s3SecretKey = System.getProperty("s3.secretkey");
        // If we are running in an environment without specified keys, then don't use them.
        if (StringUtils.isEmpty(s3AccessKey) || StringUtils.isEmpty(s3SecretKey)) {
            properties.specifyCredentials.setValue(false);
        } else {
            properties.accessKey.setValue(System.getProperty("s3.accesskey"));
            properties.secretKey.setValue(System.getProperty("s3.secretkey"));
        }
        return properties;
    }

    /**
     * @return An S3DatasetProperties with credentials in the datastore, not configured for encryption. The region are
     * bucket are taken from the environment, and a unique "object" property is created for this unit test.
     */
    public S3DatasetProperties createS3DatasetProperties() {
        return createS3DatasetProperties(false, false);
    }

    /**
     * Return an S3DatasetProperties potentially configured for encryption.
     * 
     * @param sseKms Whether server-side encryption is used. The KMS key is taken from the system environment.
     * @param sseKms Whether client-side encryption is used. The KMS key is taken from the system environment.
     * @return An S3DatasetProperties with credentials in the datastore, configured for the specified encryption. The
     * region are bucket are taken from the environment, and a unique "object" property is created for this unit test.
     */
    public S3DatasetProperties createS3DatasetProperties(boolean sseKms, boolean cseKms) {
        S3DatasetProperties properties = new S3DatasetProperties(null);
        properties.init();
        properties.region.setValue(S3Region.valueOf(System.getProperty("s3.region")));
        properties.bucket.setValue(bucketName);
        properties.object.setValue(getPath());
        properties.setDatastoreProperties(createS3DatastoreProperties());
        if (sseKms) {
            properties.encryptDataAtRest.setValue(true);
            properties.kmsForDataAtRest.setValue(System.getProperty("s3.ssekmskey"));
        }
        if (cseKms) {
            properties.encryptDataInMotion.setValue(true);
            properties.kmsForDataInMotion.setValue(System.getProperty("s3.csekmskey"));
        }
        return properties;
    }

    /**
     * Get the ObjectMetadata from S3 for the first file found on the path specified by the S3DatasetProperties.
     */
    public ObjectMetadata getObjectMetadata(S3DatasetProperties datasetProps) throws IOException {
        S3AFileSystem fs = S3Connection.createFileSystem(datasetProps);

        // The current path is a directory, so get a file to check the encryption.
        Path path = new Path(S3Connection.getUriPath(datasetProps));
        FileStatus[] files = fs.listStatus(path);
        assertThat(files, arrayWithSize(greaterThan(0)));
        return fs.getObjectMetadata(files[0].getPath());
    }

    /**
     * Return the name of the currently executing test.
     *
     * @return the name of the currently executing test.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the output path for the currently executing test.
     *
     * @return the output path for the currently executing test.
     */
    public String getPath() {
        return path;
    }

    @Override
    public Statement apply(Statement base, Description desc) {
        name = desc.getMethodName();
        path = "integration-test/" + getName() + "_" + UUID.randomUUID();
        return super.apply(base, desc);
    }

    /**
     * Remove any resources created on the S3 bucket.
     */
    @Override
    protected void after() {
        try {
            S3DatasetProperties datasetProps = createS3DatasetProperties();
            S3AFileSystem fs = S3Connection.createFileSystem(datasetProps);
            Path path = new Path(S3Connection.getUriPath(datasetProps));
            if (fs.exists(path)) {
                fs.delete(path, true);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
