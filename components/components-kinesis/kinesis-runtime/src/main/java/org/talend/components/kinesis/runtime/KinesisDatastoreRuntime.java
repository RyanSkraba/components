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

import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.kinesis.KinesisDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.ListStreamsResult;

public class KinesisDatastoreRuntime implements DatastoreRuntime<KinesisDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private KinesisDatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, KinesisDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        AmazonKinesis amazonKinesis = KinesisClient.create(properties);
        try {
            ListStreamsResult listStreamsResult = amazonKinesis.listStreams();
            return Arrays.asList(ValidationResult.OK);
        } catch (Exception e) {
            return Arrays.asList(new ValidationResult(ValidationResult.Result.ERROR, e.getMessage()));
        }
    }
}
