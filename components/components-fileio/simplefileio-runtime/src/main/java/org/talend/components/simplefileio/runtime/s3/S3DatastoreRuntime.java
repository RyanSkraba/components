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

import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

import com.talend.shaded.com.amazonaws.AmazonServiceException;
import com.talend.shaded.com.amazonaws.services.s3.AmazonS3;
import com.talend.shaded.com.amazonaws.services.s3.internal.Constants;

public class S3DatastoreRuntime implements DatastoreRuntime<S3DatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private S3DatastoreProperties properties = null;

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        try {
            // To check the credentials and network, have to call some real function,
            // connect successful when there is no exception.
            AmazonS3 conn = S3Connection.createClient(properties);
            try {
                conn.getBucketLocation("JUST_FOR_CHECK_CONNECTION");
                // conn.headBucket(new HeadBucketRequest("JUST_FOR_CHECK_CONNECTION"));
            } catch (AmazonServiceException ase) {
                // it means access successfully, so ignore
                if (ase.getStatusCode() != Constants.NO_SUCH_BUCKET_STATUS_CODE) {
                    throw ase;
                }
            }
            return Arrays.asList(ValidationResult.OK);
        } catch (Exception e) {
            return Arrays.asList(new ValidationResult(ValidationResult.Result.ERROR, e.getMessage()));
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, S3DatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }
}
