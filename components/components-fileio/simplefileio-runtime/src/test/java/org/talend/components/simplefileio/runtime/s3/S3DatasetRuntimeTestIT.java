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

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

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
    public void listBuckets() {
        runtime.initialize(null, s3.createS3DatasetProperties());
        Set<String> bucketNames = runtime.listBuckets();
        assertTrue(bucketNames.size() > 0);
        assertThat(bucketNames, hasItems(s3.getBucketName()));
    }

}
