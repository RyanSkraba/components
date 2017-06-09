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

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3Region;

import com.talend.shaded.com.amazonaws.services.s3.AmazonS3;

/**
 * Unit tests for {@link S3DatasetRuntime}.
 */
@Ignore("DEVOPS-2382")
public class S3DatasetRuntimeTestIT {

    /** Set up credentials for integration tests. */
    @Rule
    public S3TestResource s3 = S3TestResource.of();

    S3DatasetRuntime runtime;

    @Before
    public void reset() {
        runtime = new S3DatasetRuntime();
    }

    @Test
    @Ignore("It's a very slowly test, need 10 more mins")
    public void listBuckets() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String bucketFormat = "tcomp-s3-dataset-test-%s-" + uuid;
        S3DatasetProperties s3DatasetProperties = s3.createS3DatasetProperties();
        runtime.initialize(null, s3DatasetProperties);
        AmazonS3 client = S3Connection.createClient(s3.createS3DatastoreProperties());
        for (S3Region s3Region : getTestableS3Regions()) {
            client.setEndpoint(s3Region.toEndpoint());
            if (s3Region.equals(S3Region.US_EAST_1)) {
                client.createBucket(String.format(bucketFormat, s3Region.getValue()));
            } else {
                client.createBucket(String.format(bucketFormat, s3Region.getValue()), s3Region.getValue());
            }

            s3DatasetProperties.region.setValue(s3Region);
            Set<String> bucketNames = runtime.listBuckets();
            assertTrue(bucketNames.size() > 0);
            assertThat(bucketNames, hasItems(String.format(bucketFormat, s3Region.getValue())));

            client.setEndpoint(s3Region.toEndpoint());
            client.deleteBucket(String.format(bucketFormat, s3Region.getValue()));
        }
    }

    private List<S3Region> getTestableS3Regions() {
        List<S3Region> testableRegions = new ArrayList<>();
        for (S3Region s3Region : S3Region.values()) {
            switch (s3Region) {
            case DEFAULT:
            case OTHER:
            case GovCloud:
            case CN_NORTH_1:
                break;
            default:
                testableRegions.add(s3Region);
                break;
            }
        }
        return testableRegions;
    }

}
